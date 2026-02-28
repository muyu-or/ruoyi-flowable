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
            // 1. 从流程变量读 nodeTeamMap → 解析 teamId
            Map<String, Object> vars = taskService.getVariables(taskId);
            Long teamId = resolveTeamId(vars, nodeKey);
            if (teamId == null) {
                log.warn("节点{}无班组配置，跳过候选人注入", nodeKey);
                return;
            }

            // 2. 查班组成员 → 逐一 addCandidateUser
            List<SysUser> members = productionTeamMapper.selectUserListByTeamId(teamId);
            if (members == null || members.isEmpty()) {
                log.warn("班组{}无成员，跳过候选人注入", teamId);
                return;
            }

            for (SysUser user : members) {
                taskService.addCandidateUser(taskId, user.getUserId().toString());
                log.debug("已为任务{}添加候选人{}", taskId, user.getUserId());
            }

            // 3. 写 task_node_execution 记录
            TaskExecutionRecord record = taskExecutionRecordService.selectByProcInstId(procInstId);
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
            taskNodeExecutionService.insertTaskNodeExecution(nodeExec);
            log.info("已为任务{}创建节点执行记录，班组ID={}", taskId, teamId);
        } catch (Exception e) {
            log.error("注入班组候选人时出错，taskId={}", taskId, e);
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
}
