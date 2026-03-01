package com.ruoyi.flowable.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.flowable.common.constant.ProcessConstants;
import com.ruoyi.flowable.factory.FlowServiceFactory;
import com.ruoyi.flowable.service.IFlowTeamService;
import com.ruoyi.manage.mapper.ProductionTeamMapper;
import com.ruoyi.system.domain.TaskExecutionRecord;
import com.ruoyi.system.domain.TaskNodeExecution;
import com.ruoyi.system.service.ITaskExecutionRecordService;
import com.ruoyi.system.service.ITaskNodeExecutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 流程班组任务分配服务实现
 *
 * @author xgh
 * @date 2026-02-28
 */
@Slf4j
@Service
public class FlowTeamServiceImpl extends FlowServiceFactory implements IFlowTeamService {

    @Resource
    private ProductionTeamMapper productionTeamMapper;

    @Resource
    private ITaskExecutionRecordService taskExecutionRecordService;

    @Resource
    private ITaskNodeExecutionService taskNodeExecutionService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 任务创建时：注入班组成员为候选人 + 写 task_node_execution
     */
    @Override
    public void injectTeamCandidates(String taskId, String procInstId, String nodeKey, String nodeName) {
        try {
            log.info("============ 开始注入班组候选人 ============");
            log.info("taskId={}, procInstId={}, nodeKey={}, nodeName={}", taskId, procInstId, nodeKey, nodeName);

            // 1. 从流程变量读 nodeTeamMap → 解析 teamId
            Map<String, Object> vars = taskService.getVariables(taskId);
            log.info("流程变量: {}", vars);

            Long teamId = resolveTeamId(vars, nodeKey);
            log.info("解析出的teamId: {}", teamId);

            if (teamId == null) {
                log.warn("节点{}无班组配置，跳过候选人注入", nodeKey);
                return;
            }

            // 2. 查班组成员 → 逐一 addCandidateUser + addCandidateGroup
            List<SysUser> members = productionTeamMapper.selectUserListByTeamId(teamId);
            log.info("班组{}的成员数: {}", teamId, members == null ? 0 : members.size());

            if (members == null || members.isEmpty()) {
                log.warn("班组{}无成员，跳过候选人注入", teamId);
                return;
            }

            // ✅ 收集班组成员所有的角色ID（用于添加角色候选人）
            java.util.Set<Long> roleIds = new java.util.HashSet<>();

            // ✅ 添加用户候选人 + 收集角色
            for (SysUser user : members) {
                // 添加用户候选人
                taskService.addCandidateUser(taskId, user.getUserId().toString());
                log.info("已为任务{}添加候选人{} ({})", taskId, user.getUserId(), user.getUserName());

                // 收集用户的所有角色
                if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                    user.getRoles().forEach(role -> {
                        roleIds.add(role.getRoleId());
                        log.debug("班组成员{}拥有角色{} ({})", user.getUserId(), role.getRoleId(), role.getRoleName());
                    });
                }
            }

            // ✅ 添加角色候选人（保证在待办任务中能查到）
            for (Long roleId : roleIds) {
                taskService.addCandidateGroup(taskId, roleId.toString());
                log.info("已为任务{}添加候选角色{}", taskId, roleId);
            }

            // 3. 写 task_node_execution 记录
            TaskExecutionRecord record = taskExecutionRecordService.selectByProcInstId(procInstId);
            log.info("查询到的流程执行记录: {}", record);

            if (record == null) {
                log.warn("无法找到流程执行记录，procInstId={}", procInstId);
                return;
            }

            TaskNodeExecution nodeExec = new TaskNodeExecution();
            nodeExec.setExecRecordId(record.getId());
            nodeExec.setTaskId(taskId);
            nodeExec.setNodeKey(nodeKey);
            nodeExec.setNodeName(nodeName);
            nodeExec.setAssignedTeamId(teamId);
            nodeExec.setStatus("pending");
            nodeExec.setStartTime(LocalDateTime.now().format(DATE_TIME_FORMATTER));

            log.info("即将插入task_node_execution: {}", nodeExec);
            taskNodeExecutionService.insertTaskNodeExecution(nodeExec);
            log.info("✅ 成功为任务{}创建节点执行记录，班组ID={}，角色数量={}", taskId, teamId, roleIds.size());
            log.info("============ 注入班组候选人完成 ============");
        } catch (Exception e) {
            log.error("❌ 注入班组候选人时出错，taskId={}", taskId, e);
            e.printStackTrace();
        }
    }

    /**
     * 从流程变量中解析班组ID
     * 优先读 NODE_TEAM_MAP[nodeKey]，兜底读 MAIN_TEAM_ID
     */
    private Long resolveTeamId(Map<String, Object> vars, String nodeKey) {
        // 首先尝试从 NODE_TEAM_MAP 中读取
        Object mapJson = vars.get(ProcessConstants.NODE_TEAM_MAP_KEY);
        if (mapJson != null) {
            try {
                Map<String, Long> nodeTeamMap = JSON.parseObject(mapJson.toString(),
                    new TypeReference<Map<String, Long>>() {});
                if (nodeTeamMap != null && nodeTeamMap.containsKey(nodeKey)) {
                    Long teamId = nodeTeamMap.get(nodeKey);
                    if (teamId != null && teamId > 0) {
                        log.debug("从NODE_TEAM_MAP中解析到节点{}的班组ID: {}", nodeKey, teamId);
                        return teamId;
                    }
                }
            } catch (Exception e) {
                log.warn("解析NODE_TEAM_MAP失败", e);
            }
        }

        // 兜底：使用主班组ID
        Object mainTeam = vars.get(ProcessConstants.MAIN_TEAM_ID_KEY);
        if (mainTeam != null) {
            try {
                Long teamId = Long.parseLong(mainTeam.toString());
                if (teamId > 0) {
                    log.debug("使用MAIN_TEAM_ID作为节点{}的班组: {}", nodeKey, teamId);
                    return teamId;
                }
            } catch (NumberFormatException e) {
                log.warn("解析MAIN_TEAM_ID失败: {}", mainTeam);
            }
        }

        return null;
    }

    /**
     * 任务认领时：更新 task_node_execution 状态
     */
    @Override
    public void onTaskClaimed(String taskId, Long claimUserId) {
        try {
            TaskNodeExecution nodeExec = taskNodeExecutionService.selectByTaskId(taskId);
            if (nodeExec == null) {
                log.warn("无法找到节点执行记录，taskId={}", taskId);
                return;
            }

            nodeExec.setStatus("claimed");
            nodeExec.setClaimUserId(claimUserId);
            nodeExec.setClaimTime(LocalDateTime.now().format(DATE_TIME_FORMATTER));
            taskNodeExecutionService.updateTaskNodeExecution(nodeExec);
            log.info("已更新节点执行记录为claimed状态，taskId={}，claimUserId={}", taskId, claimUserId);
        } catch (Exception e) {
            log.error("更新任务认领状态时出错，taskId={}", taskId, e);
        }
    }

    /**
     * 任务完成时：更新 task_node_execution 状态
     */
    @Override
    public void onTaskCompleted(String taskId) {
        try {
            TaskNodeExecution nodeExec = taskNodeExecutionService.selectByTaskId(taskId);
            if (nodeExec == null) {
                log.warn("无法找到节点执行记录，taskId={}", taskId);
                return;
            }

            nodeExec.setStatus("completed");
            nodeExec.setCompleteTime(LocalDateTime.now().format(DATE_TIME_FORMATTER));
            taskNodeExecutionService.updateTaskNodeExecution(nodeExec);
            log.info("已更新节点执行记录为completed状态，taskId={}", taskId);
        } catch (Exception e) {
            log.error("更新任务完成状态时出错，taskId={}", taskId, e);
        }
    }

    /**
     * 任务完成时：更新 task_node_execution 状态（新版本，支持结果和意见）
     */
    @Override
    public void onTaskCompleted(String taskId, String result, String comment) {
        try {
            log.info("========== 记录任务完成 ==========");
            log.info("taskId: {}, result: {}, comment: {}", taskId, result, comment);

            TaskNodeExecution nodeExec = taskNodeExecutionService.selectByTaskId(taskId);
            if (nodeExec == null) {
                log.warn("未找到任务对应的节点执行记录，taskId={}", taskId);
                return;
            }

            LocalDateTime now = LocalDateTime.now();
            String nowStr = now.format(DATE_TIME_FORMATTER);

            nodeExec.setStatus("completed");
            nodeExec.setCompleteTime(nowStr);
            nodeExec.setResult(result);
            nodeExec.setApproveComment(comment);

            // 计算处理耗时（从认领到完成）
            if (nodeExec.getClaimTime() != null && !nodeExec.getClaimTime().isEmpty()) {
                try {
                    LocalDateTime claimTime = LocalDateTime.parse(nodeExec.getClaimTime(), DATE_TIME_FORMATTER);
                    long durationSeconds = java.time.temporal.ChronoUnit.SECONDS.between(claimTime, now);
                    nodeExec.setProcessDuration(durationSeconds);
                    log.info("任务 {} 处理耗时: {} 秒", taskId, durationSeconds);
                } catch (Exception e) {
                    log.warn("计算处理耗时失败", e);
                }
            }

            taskNodeExecutionService.updateTaskNodeExecution(nodeExec);
            log.info("✅ 任务 {} 完成记录已保存，结果: {}", taskId, result);

            // 检测流程是否已全部完成（无剩余活跃任务）
            try {
                if (nodeExec.getExecRecordId() != null) {
                    TaskExecutionRecord execRecord = taskExecutionRecordService.selectTaskExecutionRecordById(nodeExec.getExecRecordId());
                    if (execRecord != null) {
                        String procInstId = execRecord.getProcInstId();
                        long remaining = taskService.createTaskQuery()
                                .processInstanceId(procInstId)
                                .active()
                                .count();
                        if (remaining == 0) {
                            log.info("流程 {} 所有节点已完成，更新流程级别状态", procInstId);
                            execRecord.setStatus("completed");
                            execRecord.setCompleteTime(nowStr);
                            if (execRecord.getCreateTime() != null) {
                                long totalSeconds = java.time.temporal.ChronoUnit.SECONDS.between(
                                        execRecord.getCreateTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(), now);
                                execRecord.setTotalDuration(totalSeconds);
                            }
                            taskExecutionRecordService.updateTaskExecutionRecord(execRecord);
                            log.info("✅ 流程 {} 完成状态已更新，总耗时 {} 秒", procInstId, execRecord.getTotalDuration());
                        }
                    }
                }
            } catch (Exception ex) {
                log.warn("更新流程完成状态时出错，taskId={}", taskId, ex);
            }

            log.info("========== 任务完成记录结束 ==========");

        } catch (Exception e) {
            log.error("❌ 更新任务完成状态失败，taskId: {}", taskId, e);
        }
    }

    /**
     * 任务完成时：更新 task_node_execution 状态（含执行人，确保 claim_user_id 有值）
     * 如果节点从未被"认领"（直接完成），也要把执行人写进 claim_user_id，保证统计可查到。
     */
    @Override
    public void onTaskCompleted(String taskId, String result, String comment, Long userId) {
        try {
            TaskNodeExecution nodeExec = taskNodeExecutionService.selectByTaskId(taskId);
            if (nodeExec == null) {
                log.warn("未找到任务对应的节点执行记录，taskId={}", taskId);
                return;
            }

            LocalDateTime now = LocalDateTime.now();
            String nowStr = now.format(DATE_TIME_FORMATTER);

            // 如果之前没有认领（claim_user_id 为 null），用执行人补充
            if (nodeExec.getClaimUserId() == null && userId != null) {
                nodeExec.setClaimUserId(userId);
                nodeExec.setClaimTime(nowStr);
                log.info("任务 {} 未经认领直接完成，补充 claim_user_id={}", taskId, userId);
            }

            // result 为 null 时（来自 Listener）只补充 claim_user_id，
            // status/result 由业务层（complete/taskReject）负责写，避免"不通过"被覆盖成"completed"
            if (result == null) {
                taskNodeExecutionService.updateTaskNodeExecution(nodeExec);
                log.info("任务 {} Listener 触发，仅补充执行人信息", taskId);
                return;
            }

            // 根据 result 决定节点状态：rejected → "rejected"，其余 → "completed"
            String nodeStatus = "rejected".equals(result) ? "rejected" : "completed";
            nodeExec.setStatus(nodeStatus);
            nodeExec.setCompleteTime(nowStr);
            nodeExec.setResult(result);
            nodeExec.setApproveComment(comment);

            // 计算处理耗时
            if (nodeExec.getClaimTime() != null && !nodeExec.getClaimTime().isEmpty()) {
                try {
                    LocalDateTime claimTime = LocalDateTime.parse(nodeExec.getClaimTime(), DATE_TIME_FORMATTER);
                    long durationSeconds = java.time.temporal.ChronoUnit.SECONDS.between(claimTime, now);
                    nodeExec.setProcessDuration(durationSeconds);
                } catch (Exception e) {
                    log.warn("计算处理耗时失败", e);
                }
            }

            taskNodeExecutionService.updateTaskNodeExecution(nodeExec);
            log.info("✅ 任务 {} 完成记录已保存（含执行人 {}），结果: {}, 节点状态: {}", taskId, userId, result, nodeStatus);

            // 检测流程是否已全部完成（rejected 时流程已被 deleteProcessInstance 终止，remaining 为 0）
            try {
                if (nodeExec.getExecRecordId() != null) {
                    TaskExecutionRecord execRecord = taskExecutionRecordService.selectTaskExecutionRecordById(nodeExec.getExecRecordId());
                    if (execRecord != null) {
                        String procInstId = execRecord.getProcInstId();
                        long remaining = taskService.createTaskQuery()
                                .processInstanceId(procInstId)
                                .active()
                                .count();
                        if (remaining == 0) {
                            // 流程级别状态与节点结果保持一致
                            execRecord.setStatus("rejected".equals(result) ? "rejected" : "completed");
                            execRecord.setCompleteTime(nowStr);
                            taskExecutionRecordService.updateTaskExecutionRecord(execRecord);
                        }
                    }
                }
            } catch (Exception ex) {
                log.warn("更新流程完成状态时出错，taskId={}", taskId, ex);
            }

        } catch (Exception e) {
            log.error("❌ 更新任务完成状态失败（含执行人），taskId: {}", taskId, e);
        }
    }
}
