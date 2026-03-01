package com.ruoyi.flowable.service.impl;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.flowable.common.constant.ProcessConstants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.flowable.common.enums.FlowComment;
import com.ruoyi.common.exception.CustomException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.flowable.domain.dto.*;
import com.ruoyi.flowable.domain.vo.FlowQueryVo;
import com.ruoyi.flowable.domain.vo.FlowTaskVo;
import com.ruoyi.flowable.factory.FlowServiceFactory;
import com.ruoyi.flowable.flow.CustomProcessDiagramGenerator;
import com.ruoyi.flowable.flow.FindNextNodeUtil;
import com.ruoyi.flowable.flow.FlowableUtils;
import com.ruoyi.flowable.service.IFlowTaskService;
import com.ruoyi.flowable.service.ISysDeployFormService;
import com.ruoyi.flowable.service.ISysFormService;
import com.ruoyi.system.domain.SysForm;
import com.ruoyi.system.service.ISysRoleService;
import com.ruoyi.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Comment;
import org.flowable.identitylink.api.history.HistoricIdentityLink;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.DelegationState;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.flowable.engine.impl.cmd.AddMultiInstanceExecutionCmd;
import org.flowable.engine.impl.cmd.DeleteMultiInstanceExecutionCmd;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Tony
 * @date 2021-04-03
 **/
@Service
@Slf4j
public class FlowTaskServiceImpl extends FlowServiceFactory implements IFlowTaskService {

    @Resource
    private ISysUserService sysUserService;
    @Resource
    private ISysRoleService sysRoleService;
    @Resource
    private ISysDeployFormService sysInstanceFormService;
    @Resource
    private ISysFormService sysFormService;

    @Resource
    private com.ruoyi.manage.mapper.ProductionTeamMapper productionTeamMapper;

    /**
     * 完成任务
     *
     * 流程变量传递逻辑:
     * 1. 获取当前任务的所有流程变量(保留来自前置节点的数据)
     * 2. 合并新提交的变量数据(当前节点的审批结果)
     * 3. 传递所有变量给下一节点(用于网关条件判断)
     *
     * @param taskVo 请求实体参数，包含任务信息和新增变量数据
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public AjaxResult complete(FlowTaskVo taskVo) {
        Task task = taskService.createTaskQuery().taskId(taskVo.getTaskId()).singleResult();
        if (Objects.isNull(task)) {
            return AjaxResult.error("任务不存在");
        }

        // 获取所有现有的流程变量(来自前置节点)
        Map<String, Object> allVariables = taskService.getVariables(task.getId());

        // 合并新提交的变量(当前节点的新数据，如审批状态、审批意见等)
        if (taskVo.getVariables() != null && !taskVo.getVariables().isEmpty()) {
            allVariables.putAll(taskVo.getVariables());
        }

        // 添加任务完成的元数据
        Long userId = SecurityUtils.getLoginUser().getUser().getUserId();
        allVariables.put("lastCompletedBy", userId.toString());
        allVariables.put("lastCompletedTime", System.currentTimeMillis());

        if (DelegationState.PENDING.equals(task.getDelegationState())) {
            taskService.addComment(taskVo.getTaskId(), taskVo.getInstanceId(), FlowComment.DELEGATE.getType(), taskVo.getComment());
            // 委派任务解除时，传递所有变量
            taskService.resolveTask(taskVo.getTaskId(), allVariables);
        } else {
            taskService.addComment(taskVo.getTaskId(), taskVo.getInstanceId(), FlowComment.NORMAL.getType(), taskVo.getComment());
            taskService.setAssignee(taskVo.getTaskId(), userId.toString());
            // 完成任务，传递合并后的所有变量到下一节点
            taskService.complete(taskVo.getTaskId(), allVariables);

            // 任务完成后，为后续新创建的任务注入班组候选人
            // 这是为了处理任务监听器可能未被触发的情况
            try {
                String procInstId = task.getProcessInstanceId();
                List<Task> nextTasks = taskService.createTaskQuery()
                        .processInstanceId(procInstId)
                        .active()
                        .list();

                log.info("任务 {} 完成后，流程实例 {} 中有 {} 个活跃任务", taskVo.getTaskId(), procInstId, nextTasks.size());

                for (Task nextTask : nextTasks) {
                    // 检查该任务是否已经有候选人
                    List<org.flowable.identitylink.api.IdentityLink> identityLinks =
                            taskService.getIdentityLinksForTask(nextTask.getId());
                    boolean hasCandidates = identityLinks.stream()
                            .anyMatch(link -> "candidate".equals(link.getType()));

                    if (!hasCandidates) {
                        log.info("为后续任务 {} 注入班组候选人", nextTask.getId());
                        try {
                            com.ruoyi.common.utils.spring.SpringUtils.getBean(
                                    com.ruoyi.flowable.service.IFlowTeamService.class)
                                    .injectTeamCandidates(nextTask.getId(), procInstId,
                                            nextTask.getTaskDefinitionKey(), nextTask.getName());
                        } catch (Exception e) {
                            log.warn("为任务 {} 注入班组时出错", nextTask.getId(), e);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("处理后续任务候选人时出错", e);
            }

            // 任务完成后，更新任务执行记录状态（传入执行人userId，确保 claim_user_id 有值）
            try {
                String comment = taskVo.getComment() != null ? taskVo.getComment() : "";
                com.ruoyi.common.utils.spring.SpringUtils.getBean(com.ruoyi.flowable.service.IFlowTeamService.class)
                        .onTaskCompleted(taskVo.getTaskId(), "completed", comment, userId);
            } catch (Exception e) {
                log.warn("更新任务完成状态时出错", e);
            }
        }

        log.info("任务 {} 完成，传递变量: {}", taskVo.getTaskId(), allVariables);
        return AjaxResult.success();
    }

    /**
     * 驳回任务
     *
     * @param flowTaskVo
     */
    /**
     * 不通过任务 — 无论哪个节点不通过，均直接终止整个流程实例（标记为失败）
     * 使用 deleteProcessInstance 并携带 deleteReason="REJECTED:{comment}"，
     * 前端可据此将流程状态显示为"失败"
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void taskReject(FlowTaskVo flowTaskVo) {
        Task task = taskService.createTaskQuery().taskId(flowTaskVo.getTaskId()).singleResult();
        if (Objects.isNull(task)) {
            throw new CustomException("任务不存在");
        }
        if (task.isSuspended()) {
            throw new CustomException("任务处于挂起状态!");
        }

        String comment = StringUtils.isBlank(flowTaskVo.getComment()) ? "不通过" : flowTaskVo.getComment();
        String procInsId = task.getProcessInstanceId();

        // 记录不通过意见
        taskService.addComment(task.getId(), procInsId, FlowComment.REJECT.getType(), comment);

        // 记录任务状态（传入当前用户ID，确保 claim_user_id 有值）
        try {
            Long rejectUserId = SecurityUtils.getLoginUser().getUser().getUserId();
            com.ruoyi.common.utils.spring.SpringUtils.getBean(com.ruoyi.flowable.service.IFlowTeamService.class)
                    .onTaskCompleted(flowTaskVo.getTaskId(), "rejected", comment, rejectUserId);
        } catch (Exception e) {
            log.warn("记录任务不通过状态失败，taskId={}", flowTaskVo.getTaskId(), e);
        }

        // 直接终止流程实例，deleteReason 以 "REJECTED:" 为前缀，供前端判断流程状态为"失败"
        runtimeService.deleteProcessInstance(procInsId, "REJECTED:" + comment);
        log.info("流程实例 {} 因任务 {} 不通过而终止", procInsId, task.getId());
    }

    /**
     * 退回任务
     *
     * @param flowTaskVo 请求实体参数
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void taskReturn(FlowTaskVo flowTaskVo) {
        // 获取当前运行时任务
        Task task = taskService.createTaskQuery().taskId(flowTaskVo.getTaskId()).singleResult();
        if (Objects.isNull(task)) {
            throw new CustomException("任务不存在");
        }
        if (task.isSuspended()) {
            throw new CustomException("任务处于挂起状态");
        }

        String procInsId = task.getProcessInstanceId();
        String currentTaskKey = task.getTaskDefinitionKey();

        // ── 确定退回目标节点 key ──
        // 优先使用前端传入的 targetKey（手动选择节点时）；
        // 未传入时，自动从历史记录中查找上一个已完成的用户任务节点（"返回上一节点"语义）
        String targetKey = flowTaskVo.getTargetKey();
        if (StringUtils.isBlank(targetKey)) {
            // 按任务开始时间降序取最近完成的历史任务，跳过当前节点自身（退回前可能已记录过）
            List<HistoricTaskInstance> hisTaskList = historyService.createHistoricTaskInstanceQuery()
                    .processInstanceId(procInsId)
                    .finished()
                    .orderByHistoricTaskInstanceEndTime().desc()
                    .list();
            for (HistoricTaskInstance ht : hisTaskList) {
                if (!currentTaskKey.equals(ht.getTaskDefinitionKey())) {
                    targetKey = ht.getTaskDefinitionKey();
                    break;
                }
            }
            if (StringUtils.isBlank(targetKey)) {
                throw new CustomException("未找到可退回的上一节点，当前已是第一个节点");
            }
        }

        // 记录退回意见
        String comment = StringUtils.isBlank(flowTaskVo.getComment()) ? "退回上一节点" : flowTaskVo.getComment();
        taskService.addComment(task.getId(), procInsId, FlowComment.REBACK.getType(), comment);

        // 获取当前流程实例所有活跃任务的 definitionKey（用于 moveActivityIds）
        List<Task> runTaskList = taskService.createTaskQuery().processInstanceId(procInsId).list();
        List<String> currentActivityIds = new ArrayList<>();
        for (Task runTask : runTaskList) {
            currentActivityIds.add(runTask.getTaskDefinitionKey());
        }
        if (currentActivityIds.isEmpty()) {
            throw new CustomException("当前流程无活跃任务，无法退回");
        }

        log.info("退回流程：procInsId={}, from={}, to={}", procInsId, currentActivityIds, targetKey);

        try {
            // 将所有当前活跃节点移动到目标节点（支持并行聚合到单节点）
            runtimeService.createChangeActivityStateBuilder()
                    .processInstanceId(procInsId)
                    .moveActivityIdsToSingleActivityId(currentActivityIds, targetKey)
                    .changeState();
        } catch (FlowableObjectNotFoundException e) {
            throw new CustomException("未找到流程实例，流程可能已发生变化");
        } catch (FlowableException e) {
            log.error("退回流程失败", e);
            throw new CustomException("退回失败：" + e.getMessage());
        }

        // 退回后为重新激活的节点注入班组候选人（与 complete 保持一致）
        // 若不注入，退回节点的原审批人将无法在待办列表中看到该任务
        try {
            List<Task> reactivatedTasks = taskService.createTaskQuery()
                    .processInstanceId(procInsId)
                    .taskDefinitionKey(targetKey)
                    .active()
                    .list();
            for (Task reactivatedTask : reactivatedTasks) {
                List<org.flowable.identitylink.api.IdentityLink> links =
                        taskService.getIdentityLinksForTask(reactivatedTask.getId());
                boolean hasCandidates = links.stream()
                        .anyMatch(link -> "candidate".equals(link.getType()));
                if (!hasCandidates) {
                    log.info("退回后为任务 {} 注入班组候选人", reactivatedTask.getId());
                    try {
                        com.ruoyi.common.utils.spring.SpringUtils.getBean(
                                com.ruoyi.flowable.service.IFlowTeamService.class)
                                .injectTeamCandidates(reactivatedTask.getId(),
                                        procInsId,
                                        reactivatedTask.getTaskDefinitionKey(),
                                        reactivatedTask.getName());
                    } catch (Exception e) {
                        log.warn("退回后为任务 {} 注入班组候选人时出错", reactivatedTask.getId(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("退回后注入候选人失败，流程实例: {}", procInsId, e);
        }
    }


    /**
     * 获取可回退的节点列表
     * 根据流程历史，返回当前节点之前已经执行过的、且唯一的用户任务节点（去重后按执行顺序倒序）。
     * 对于顺序流程，"返回上一节点" 的目标是最近一次完成的、与当前节点不同的节点。
     */
    @Override
    public AjaxResult findReturnTaskList(FlowTaskVo flowTaskVo) {
        Task task = taskService.createTaskQuery().taskId(flowTaskVo.getTaskId()).singleResult();
        if (Objects.isNull(task)) {
            return AjaxResult.error("任务不存在");
        }

        String procInsId = task.getProcessInstanceId();
        String currentTaskKey = task.getTaskDefinitionKey();

        // 从历史中按结束时间倒序查已完成任务，去重后收集 taskDefinitionKey（排除当前节点自身）
        List<HistoricTaskInstance> hisTaskList = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(procInsId)
                .finished()
                .orderByHistoricTaskInstanceEndTime().desc()
                .list();

        // 读取 BPMN 模型，将 key 映射为 UserTask 对象（保留 name 等属性供前端显示）
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        Map<String, UserTask> userTaskMap = new LinkedHashMap<>();
        if (bpmnModel != null) {
            bpmnModel.getMainProcess().getFlowElements().forEach(el -> {
                if (el instanceof UserTask) {
                    userTaskMap.put(el.getId(), (UserTask) el);
                }
            });
        }

        // 按历史执行顺序（倒序）收集可退回节点，去重，排除当前节点
        List<UserTask> returnableList = new ArrayList<>();
        Set<String> seenKeys = new LinkedHashSet<>();
        for (HistoricTaskInstance ht : hisTaskList) {
            String key = ht.getTaskDefinitionKey();
            if (currentTaskKey.equals(key)) continue;   // 排除自身
            if (!seenKeys.add(key)) continue;            // 去重
            UserTask ut = userTaskMap.get(key);
            if (ut != null) {
                returnableList.add(ut);
            }
        }

        return AjaxResult.success(returnableList);
    }

    /**
     * 删除任务 (放弃任务 - 标记任务失败，直接删除任务，流程停止)
     *
     * @param flowTaskVo 请求实体参数
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteTask(FlowTaskVo flowTaskVo) {
        // 获取当前任务
        Task task = taskService.createTaskQuery().taskId(flowTaskVo.getTaskId()).singleResult();
        if (Objects.isNull(task)) {
            throw new CustomException("任务不存在");
        }

        // 添加放弃意见评论 - 用于记录任务失败原因
        taskService.addComment(flowTaskVo.getTaskId(), task.getProcessInstanceId(),
                FlowComment.ABANDON.getType(),
                StringUtils.isBlank(flowTaskVo.getComment()) ? "任务已放弃" : flowTaskVo.getComment());

        // 直接删除任务 - 任务不再出现在已发任务列表中
        // 但历史记录仍保留在已办任务中供统计
        taskService.deleteTask(flowTaskVo.getTaskId(),
                StringUtils.isBlank(flowTaskVo.getComment()) ? "任务已放弃" : flowTaskVo.getComment());
    }

    /**
     * 认领/签收任务
     * 认领以后,这个用户就会成为任务的执行人,任务会从其他成员的任务列表中消失
     *
     * @param flowTaskVo 请求实体参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claim(FlowTaskVo flowTaskVo) {
        taskService.claim(flowTaskVo.getTaskId(), flowTaskVo.getUserId());
        // 任务认领后，更新任务执行记录状态
        try {
            Long userId = null;
            try {
                userId = Long.parseLong(flowTaskVo.getUserId());
            } catch (NumberFormatException e) {
                log.warn("用户ID格式错误: {}", flowTaskVo.getUserId());
            }
            if (userId != null) {
                com.ruoyi.common.utils.spring.SpringUtils.getBean(com.ruoyi.flowable.service.IFlowTeamService.class)
                        .onTaskClaimed(flowTaskVo.getTaskId(), userId);
            }
        } catch (Exception e) {
            log.warn("更新任务认领状态时出错", e);
        }
    }

    /**
     * 取消认领/签收任务
     *
     * @param flowTaskVo 请求实体参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unClaim(FlowTaskVo flowTaskVo) {
        taskService.unclaim(flowTaskVo.getTaskId());
    }

    /**
     * 委派任务
     * 任务委派只是委派人将当前的任务交给被委派人进行审批，处理任务后又重新回到委派人身上。
     *
     * @param flowTaskVo 请求实体参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delegateTask(FlowTaskVo flowTaskVo) {
        taskService.delegateTask(flowTaskVo.getTaskId(), flowTaskVo.getAssignee());
    }

    /**
     * 任务归还
     * 被委派人完成任务之后，将任务归还委派人
     *
     * @param flowTaskVo 请求实体参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resolveTask(FlowTaskVo flowTaskVo) {
        taskService.resolveTask(flowTaskVo.getTaskId());
    }


    /**
     * 转办任务
     * 直接将办理人换成别人，这时任务的拥有者不再是转办人
     *
     * @param flowTaskVo 请求实体参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignTask(FlowTaskVo flowTaskVo) {
        // 直接转派就可以覆盖掉之前的
        taskService.setAssignee(flowTaskVo.getTaskId(), flowTaskVo.getAssignee());
//        // 删除指派人重新指派
//        taskService.deleteCandidateUser(flowTaskVo.getTaskId(),flowTaskVo.getAssignee());
//        taskService.addCandidateUser(flowTaskVo.getTaskId(),flowTaskVo.getAssignee());
//        // 如果要查询转给他人处理的任务，可以同时将OWNER进行设置：
//        taskService.setOwner(flowTaskVo.getTaskId(), flowTaskVo.getAssignee());

    }

    /**
     * 多实例加签
     * act_ru_task、act_ru_identitylink各生成一条记录
     *
     * @param flowTaskVo
     */
    @Override
    public void addMultiInstanceExecution(FlowTaskVo flowTaskVo) {
        managementService.executeCommand(new AddMultiInstanceExecutionCmd(flowTaskVo.getDefId(), flowTaskVo.getInstanceId(), flowTaskVo.getVariables()));
    }

    /**
     * 多实例减签
     * act_ru_task减1、act_ru_identitylink不变
     *
     * @param flowTaskVo
     */
    @Override
    public void deleteMultiInstanceExecution(FlowTaskVo flowTaskVo) {
        managementService.executeCommand(new DeleteMultiInstanceExecutionCmd(flowTaskVo.getCurrentChildExecutionId(), flowTaskVo.getFlag()));
    }

    /**
     * 我发起的流程
     *
     * @param queryVo 请求参数
     * @return
     */
    @Override
    public AjaxResult myProcess(FlowQueryVo queryVo) {
        Page<FlowTaskDto> page = new Page<>();
        Long userId = SecurityUtils.getLoginUser().getUser().getUserId();
        HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery()
                .startedBy(userId.toString())
                .orderByProcessInstanceStartTime()
                .desc();
        List<HistoricProcessInstance> historicProcessInstances = historicProcessInstanceQuery.listPage(queryVo.getPageSize() * (queryVo.getPageNum() - 1), queryVo.getPageSize());
        page.setTotal(historicProcessInstanceQuery.count());
        List<FlowTaskDto> flowList = new ArrayList<>();
        for (HistoricProcessInstance hisIns : historicProcessInstances) {
            FlowTaskDto flowTask = new FlowTaskDto();
            flowTask.setCreateTime(hisIns.getStartTime());
            flowTask.setFinishTime(hisIns.getEndTime());
            flowTask.setProcInsId(hisIns.getId());

            // 计算耗时
            if (Objects.nonNull(hisIns.getEndTime())) {
                long time = hisIns.getEndTime().getTime() - hisIns.getStartTime().getTime();
                flowTask.setDuration(getDate(time));
            } else {
                long time = System.currentTimeMillis() - hisIns.getStartTime().getTime();
                flowTask.setDuration(getDate(time));
            }
            // 流程定义信息
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(hisIns.getProcessDefinitionId())
                    .singleResult();
            flowTask.setDeployId(pd.getDeploymentId());
            flowTask.setProcDefName(pd.getName());
            flowTask.setProcDefVersion(pd.getVersion());
            flowTask.setCategory(pd.getCategory());
            flowTask.setProcDefVersion(pd.getVersion());
            // 流程状态：running=进行中, finished=已完成, rejected=失败(不通过), stopped=已取消
            // Flowable 通过 deleteReason 区分正常结束和强制终止
            String deleteReason = hisIns.getDeleteReason();
            String procStatus;
            if (hisIns.getEndTime() == null) {
                procStatus = "running";       // 还有活跃任务 = 进行中
            } else if (StringUtils.isNotBlank(deleteReason) && deleteReason.startsWith("REJECTED:")) {
                procStatus = "rejected";      // 被不通过终止 = 失败
            } else if (StringUtils.isNotBlank(deleteReason) && deleteReason.startsWith("STOPPED:")) {
                procStatus = "stopped";       // 手动取消 = 已取消
            } else {
                procStatus = "finished";      // 正常走到结束节点 = 已完成
            }
            flowTask.setProcStatus(procStatus);
            // 当前所处流程
            List<Task> taskList = taskService.createTaskQuery().processInstanceId(hisIns.getId()).list();
            if (CollectionUtils.isNotEmpty(taskList)) {
                flowTask.setTaskId(taskList.get(0).getId());
                flowTask.setTaskName(taskList.get(0).getName());
                if (StringUtils.isNotBlank(taskList.get(0).getAssignee())) {
                    // 当前任务节点办理人信息
                    SysUser sysUser = sysUserService.selectUserById(Long.parseLong(taskList.get(0).getAssignee()));
                    if (Objects.nonNull(sysUser)) {
                        flowTask.setAssigneeId(sysUser.getUserId());
                        flowTask.setAssigneeName(sysUser.getNickName());
                        flowTask.setAssigneeDeptName(Objects.nonNull(sysUser.getDept()) ? sysUser.getDept().getDeptName() : "");
                    }
                }
            } else {
                List<HistoricTaskInstance> historicTaskInstance = historyService.createHistoricTaskInstanceQuery().processInstanceId(hisIns.getId()).orderByHistoricTaskInstanceEndTime().desc().list();
                flowTask.setTaskId(historicTaskInstance.get(0).getId());
                flowTask.setTaskName(historicTaskInstance.get(0).getName());
                if (StringUtils.isNotBlank(historicTaskInstance.get(0).getAssignee())) {
                    // 当前任务节点办理人信息
                    SysUser sysUser = sysUserService.selectUserById(Long.parseLong(historicTaskInstance.get(0).getAssignee()));
                    if (Objects.nonNull(sysUser)) {
                        flowTask.setAssigneeId(sysUser.getUserId());
                        flowTask.setAssigneeName(sysUser.getNickName());
                        flowTask.setAssigneeDeptName(Objects.nonNull(sysUser.getDept()) ? sysUser.getDept().getDeptName() : "");
                    }
                }
            }
            flowList.add(flowTask);
        }
        page.setRecords(flowList);
        return AjaxResult.success(page);
    }

    /**
     * 取消申请
     * 目前实现方式: 直接将当前流程变更为已完成
     *
     * @param flowTaskVo
     * @return
     */
    @Override
    public AjaxResult stopProcess(FlowTaskVo flowTaskVo) {
        List<Task> task = taskService.createTaskQuery().processInstanceId(flowTaskVo.getInstanceId()).list();
        if (CollectionUtils.isEmpty(task)) {
            throw new CustomException("流程未启动或已执行完成，取消申请失败");
        }
        String comment = StringUtils.isBlank(flowTaskVo.getComment()) ? "取消申请" : flowTaskVo.getComment();
        // 用 deleteProcessInstance 终止并携带 STOPPED: 前缀，供前端区分"已取消"状态
        runtimeService.deleteProcessInstance(flowTaskVo.getInstanceId(), "STOPPED:" + comment);
        return AjaxResult.success();
    }

    /**
     * 撤回流程  目前存在错误
     *
     * @param flowTaskVo
     * @return
     */
    @Override
    public AjaxResult revokeProcess(FlowTaskVo flowTaskVo) {
        Task task = taskService.createTaskQuery()
                .processInstanceId(flowTaskVo.getInstanceId())
                .singleResult();
        if (task == null) {
            throw new CustomException("流程未启动或已执行完成，无法撤回");
        }

        SysUser loginUser = SecurityUtils.getLoginUser().getUser();
        List<HistoricTaskInstance> htiList = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .orderByTaskCreateTime()
                .asc()
                .list();
        String myTaskId = null;
        for (HistoricTaskInstance hti : htiList) {
            if (loginUser.getUserId().toString().equals(hti.getAssignee())) {
                myTaskId = hti.getId();
                break;
            }
        }
        if (null == myTaskId) {
            throw new CustomException("该任务非当前用户提交，无法撤回");
        }
        List<HistoricTaskInstance> historicTaskInstanceList = historyService
                .createHistoricTaskInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .orderByHistoricTaskInstanceStartTime()
                .asc()
                .list();
        Iterator<HistoricTaskInstance> it = historicTaskInstanceList.iterator();
        //循环节点，获取当前节点的上一节点的key
        String tarKey = "";
        while (it.hasNext()) {
            HistoricTaskInstance his = it.next();
            if (!task.getTaskDefinitionKey().equals(his.getTaskDefinitionKey())) {
                tarKey = his.getTaskDefinitionKey();
            }
        }
        // 跳转节点
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(flowTaskVo.getInstanceId())
                .moveActivityIdTo(task.getTaskDefinitionKey(), tarKey)
                .changeState();

        return AjaxResult.success();
    }

    /**
     * 代办任务列表
     *
     * @param queryVo 请求参数
     * @return
     */
    @Override
    public AjaxResult todoList(FlowQueryVo queryVo) {
        Page<FlowTaskDto> page = new Page<>();
        // 只查看自己的数据
        SysUser sysUser = SecurityUtils.getLoginUser().getUser();

        log.info("========== 查询待办任务列表 ==========");
        log.info("当前用户ID: {}, 用户名: {}", sysUser.getUserId(), sysUser.getUserName());

        List<String> roleIds = sysUser.getRoles().stream()
                .map(role -> role.getRoleId().toString())
                .collect(Collectors.toList());
        log.info("当前用户角色IDs: {}", roleIds);

        TaskQuery taskQuery = taskService.createTaskQuery()
                .active()
                .includeProcessVariables()
                .or()
                    .taskCandidateUser(sysUser.getUserId().toString())
                    .taskAssignee(sysUser.getUserId().toString())
                    .taskCandidateGroupIn(roleIds)
                .endOr()
                .orderByTaskCreateTime().desc();

        log.info("查询条件: (taskCandidateUser({}) OR taskAssignee({}) OR taskCandidateGroupIn({}))",
                sysUser.getUserId(), sysUser.getUserId(), roleIds);

//        TODO 传入名称查询不到数据?
        if (StringUtils.isNotBlank(queryVo.getName())) {
            taskQuery.processDefinitionNameLike(queryVo.getName());
            log.info("添加流程名称过滤: {}", queryVo.getName());
        }

        page.setTotal(taskQuery.count());
        log.info("查询结果总数: {}", page.getTotal());

        List<Task> taskList = taskQuery.listPage(queryVo.getPageSize() * (queryVo.getPageNum() - 1), queryVo.getPageSize());
        log.info("当前页面任务数: {}", taskList.size());

        List<FlowTaskDto> flowList = new ArrayList<>();
        for (Task task : taskList) {
            FlowTaskDto flowTask = new FlowTaskDto();
            // 当前流程信息
            flowTask.setTaskId(task.getId());
            flowTask.setTaskDefKey(task.getTaskDefinitionKey());
            flowTask.setCreateTime(task.getCreateTime());
            flowTask.setProcDefId(task.getProcessDefinitionId());
            flowTask.setExecutionId(task.getExecutionId());
            flowTask.setTaskName(task.getName());
            // 流程定义信息
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(task.getProcessDefinitionId())
                    .singleResult();
            flowTask.setDeployId(pd.getDeploymentId());
            flowTask.setProcDefName(pd.getName());
            flowTask.setProcDefVersion(pd.getVersion());
            flowTask.setProcInsId(task.getProcessInstanceId());

            // 流程发起人信息
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .singleResult();
            SysUser startUser = sysUserService.selectUserById(Long.parseLong(historicProcessInstance.getStartUserId()));
            flowTask.setStartUserId(startUser.getUserId().toString());
            flowTask.setStartUserName(startUser.getNickName());
            flowTask.setStartDeptName(Objects.nonNull(startUser.getDept()) ? startUser.getDept().getDeptName() : "");

            log.debug("待办任务: taskId={}, taskName={}, procDefName={}, taskCreateTime={}",
                    task.getId(), task.getName(), pd.getName(), task.getCreateTime());

            flowList.add(flowTask);
        }

        page.setRecords(flowList);
        log.info("========== 待办任务查询完成 ==========");
        return AjaxResult.success(page);
    }


    /**
     * 已办任务列表
     *
     * @param queryVo 请求参数
     * @return
     */
    @Override
    public AjaxResult finishedList(FlowQueryVo queryVo) {
        Page<FlowTaskDto> page = new Page<>();
        Long userId = SecurityUtils.getLoginUser().getUser().getUserId();
        HistoricTaskInstanceQuery taskInstanceQuery = historyService.createHistoricTaskInstanceQuery()
                .includeProcessVariables()
                .finished()
                .taskAssignee(userId.toString())
                .orderByHistoricTaskInstanceEndTime()
                .desc();
        List<HistoricTaskInstance> historicTaskInstanceList = taskInstanceQuery.listPage(queryVo.getPageSize() * (queryVo.getPageNum() - 1), queryVo.getPageSize());
        List<FlowTaskDto> hisTaskList = new ArrayList<>();
        for (HistoricTaskInstance histTask : historicTaskInstanceList) {
            FlowTaskDto flowTask = new FlowTaskDto();
            // 当前流程信息
            flowTask.setTaskId(histTask.getId());
            // 审批人员信息
            flowTask.setCreateTime(histTask.getCreateTime());
            flowTask.setFinishTime(histTask.getEndTime());
            flowTask.setDuration(getDate(histTask.getDurationInMillis()));
            flowTask.setProcDefId(histTask.getProcessDefinitionId());
            flowTask.setTaskDefKey(histTask.getTaskDefinitionKey());
            flowTask.setTaskName(histTask.getName());

            // 流程定义信息
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(histTask.getProcessDefinitionId())
                    .singleResult();
            flowTask.setDeployId(pd.getDeploymentId());
            flowTask.setProcDefName(pd.getName());
            flowTask.setProcDefVersion(pd.getVersion());
            flowTask.setProcInsId(histTask.getProcessInstanceId());
            flowTask.setHisProcInsId(histTask.getProcessInstanceId());

            // 流程发起人信息
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(histTask.getProcessInstanceId())
                    .singleResult();
            SysUser startUser = sysUserService.selectUserById(Long.parseLong(historicProcessInstance.getStartUserId()));
            flowTask.setStartUserId(startUser.getNickName());
            flowTask.setStartUserName(startUser.getNickName());
            flowTask.setStartDeptName(Objects.nonNull(startUser.getDept()) ? startUser.getDept().getDeptName() : "");

            // 判断任务是否成功 - 根据任务评论类型判断
            // 1: 成功, 0: 驳回/失败, 2: 放弃
            int taskSuccess = 1; // 默认成功
            List<Comment> commentList = taskService.getProcessInstanceComments(histTask.getProcessInstanceId());
            for (Comment comment : commentList) {
                if (histTask.getId().equals(comment.getTaskId())) {
                    if (FlowComment.REJECT.getType().equals(comment.getType())) {
                        taskSuccess = 0; // 驳回
                    } else if (FlowComment.REBACK.getType().equals(comment.getType())) {
                        taskSuccess = 0; // 退回
                    } else if (FlowComment.ABANDON.getType().equals(comment.getType())) {
                        taskSuccess = 2; // 放弃
                    }
                    break;
                }
            }
            flowTask.setTaskSuccess(taskSuccess);

            hisTaskList.add(flowTask);
        }
        page.setTotal(taskInstanceQuery.count());
        page.setRecords(hisTaskList);
        return AjaxResult.success(page);
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    /**
     * 流程历史流转记录
     *
     * @param procInsId 流程实例Id
     * @return
     */
    @Override
    public AjaxResult flowRecord(String procInsId, String deployId) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (StringUtils.isNotBlank(procInsId)) {
            List<HistoricActivityInstance> list = historyService
                    .createHistoricActivityInstanceQuery()
                    .processInstanceId(procInsId)
                    .orderByHistoricActivityInstanceStartTime()
                    .desc().list();
            List<FlowTaskDto> hisFlowList = new ArrayList<>();
            for (HistoricActivityInstance histIns : list) {
                // 展示开始节点
//                if ("startEvent".equals(histIns.getActivityType())) {
//                    FlowTaskDto flowTask = new FlowTaskDto();
//                    // 流程发起人信息
//                    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
//                            .processInstanceId(histIns.getProcessInstanceId())
//                            .singleResult();
//                    SysUser startUser = sysUserService.selectUserById(Long.parseLong(historicProcessInstance.getStartUserId()));
//                    flowTask.setTaskName(startUser.getNickName() + "(" + startUser.getDept().getDeptName() + ")发起申请");
//                    flowTask.setFinishTime(histIns.getEndTime());
//                    hisFlowList.add(flowTask);
//                } else if ("endEvent".equals(histIns.getActivityType())) {
//                    FlowTaskDto flowTask = new FlowTaskDto();
//                    flowTask.setTaskName(StringUtils.isNotBlank(histIns.getActivityName()) ? histIns.getActivityName() : "结束");
//                    flowTask.setFinishTime(histIns.getEndTime());
//                    hisFlowList.add(flowTask);
//                } else
                if (StringUtils.isNotBlank(histIns.getTaskId())) {
                    FlowTaskDto flowTask = new FlowTaskDto();
                    flowTask.setTaskId(histIns.getTaskId());
                    flowTask.setTaskName(histIns.getActivityName());
                    flowTask.setCreateTime(histIns.getStartTime());
                    flowTask.setFinishTime(histIns.getEndTime());
                    if (StringUtils.isNotBlank(histIns.getAssignee())) {
                        SysUser sysUser = sysUserService.selectUserById(Long.parseLong(histIns.getAssignee()));
                        flowTask.setAssigneeId(sysUser.getUserId());
                        flowTask.setAssigneeName(sysUser.getNickName());
                        flowTask.setDeptName(Objects.nonNull(sysUser.getDept()) ? sysUser.getDept().getDeptName() : "");
                    }
                    // 展示审批人员
                    List<HistoricIdentityLink> linksForTask = historyService.getHistoricIdentityLinksForTask(histIns.getTaskId());
                    StringBuilder stringBuilder = new StringBuilder();
                    for (HistoricIdentityLink identityLink : linksForTask) {
                        // 获选人,候选组/角色(多个)
                        if ("candidate".equals(identityLink.getType())) {
                            if (StringUtils.isNotBlank(identityLink.getUserId())) {
                                SysUser sysUser = sysUserService.selectUserById(Long.parseLong(identityLink.getUserId()));
                                stringBuilder.append(sysUser.getNickName()).append(",");
                            }
                            if (StringUtils.isNotBlank(identityLink.getGroupId())) {
                                SysRole sysRole = sysRoleService.selectRoleById(Long.parseLong(identityLink.getGroupId()));
                                stringBuilder.append(sysRole.getRoleName()).append(",");
                            }
                        }
                    }
                    if (StringUtils.isNotBlank(stringBuilder)) {
                        flowTask.setCandidate(stringBuilder.substring(0, stringBuilder.length() - 1));
                    }

                    flowTask.setDuration(histIns.getDurationInMillis() == null || histIns.getDurationInMillis() == 0 ? null : getDate(histIns.getDurationInMillis()));
                    // 获取意见评论内容
                    List<Comment> commentList = taskService.getProcessInstanceComments(histIns.getProcessInstanceId());
                    commentList.forEach(comment -> {
                        if (histIns.getTaskId().equals(comment.getTaskId())) {
                            flowTask.setComment(FlowCommentDto.builder().type(comment.getType()).comment(comment.getFullMessage()).build());
                        }
                    });
                    hisFlowList.add(flowTask);
                }
            }
            map.put("flowList", hisFlowList);
        }
        // 第一次申请获取初始化表单
        if (StringUtils.isNotBlank(deployId)) {
            SysForm sysForm = sysInstanceFormService.selectSysDeployFormByDeployId(deployId);
            if (Objects.isNull(sysForm)) {
                return AjaxResult.error("请先配置流程表单");
            }
            map.put("formData", JSONObject.parseObject(sysForm.getFormContent()));
        }
        return AjaxResult.success(map);
    }

    /**
     * 根据任务ID查询挂载的表单信息
     *
     * @param taskId 任务Id
     * @return
     */
    @Override
    public AjaxResult getTaskForm(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        SysForm sysForm = sysFormService.selectSysFormById(Long.parseLong(task.getFormKey()));
        return AjaxResult.success(sysForm.getFormContent());
    }

    /**
     * 获取流程过程图
     *
     * @param processId
     * @return
     */
    @Override
    public InputStream diagram(String processId) {
        String processDefinitionId;
        // 获取当前的流程实例
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();
        // 如果流程已经结束，则得到结束节点
        if (Objects.isNull(processInstance)) {
            HistoricProcessInstance pi = historyService.createHistoricProcessInstanceQuery().processInstanceId(processId).singleResult();

            processDefinitionId = pi.getProcessDefinitionId();
        } else {// 如果流程没有结束，则取当前活动节点
            // 根据流程实例ID获得当前处于活动状态的ActivityId合集
            ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();
            processDefinitionId = pi.getProcessDefinitionId();
        }

        // 获得活动的节点
        List<HistoricActivityInstance> highLightedFlowList = historyService.createHistoricActivityInstanceQuery().processInstanceId(processId).orderByHistoricActivityInstanceStartTime().asc().list();

        List<String> highLightedFlows = new ArrayList<>();
        List<String> highLightedNodes = new ArrayList<>();
        //高亮线
        for (HistoricActivityInstance tempActivity : highLightedFlowList) {
            if ("sequenceFlow".equals(tempActivity.getActivityType())) {
                //高亮线
                highLightedFlows.add(tempActivity.getActivityId());
            } else {
                //高亮节点
                highLightedNodes.add(tempActivity.getActivityId());
            }
        }

        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        ProcessEngineConfiguration configuration = processEngine.getProcessEngineConfiguration();
        //获取自定义图片生成器
        ProcessDiagramGenerator diagramGenerator = new CustomProcessDiagramGenerator();
        InputStream in = diagramGenerator.generateDiagram(bpmnModel, "png", highLightedNodes, highLightedFlows, configuration.getActivityFontName(),
                configuration.getLabelFontName(), configuration.getAnnotationFontName(), configuration.getClassLoader(), 1.0, true);
        return in;

    }

    /**
     * 获取流程执行节点
     *
     * @param procInsId 流程实例id
     * @return
     */
    @Override
    public AjaxResult getFlowViewer(String procInsId, String executionId) {
        List<FlowViewerDto> flowViewerList = new ArrayList<>();
        FlowViewerDto flowViewerDto;
        // 获取任务开始节点(临时处理方式)
        List<HistoricActivityInstance> startNodeList = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(procInsId)
                .orderByHistoricActivityInstanceStartTime()
                .asc().listPage(0, 3);
        for (HistoricActivityInstance startInstance : startNodeList) {
            if (!"sequenceFlow".equals(startInstance.getActivityType())) {
                flowViewerDto = new FlowViewerDto();
                if (!"sequenceFlow".equals(startInstance.getActivityType())) {
                    flowViewerDto.setKey(startInstance.getActivityId());
                    // 根据流程节点处理时间校验改节点是否已完成
                    flowViewerDto.setCompleted(!Objects.isNull(startInstance.getEndTime()));
                    flowViewerList.add(flowViewerDto);
                }
            }
        }
        // 历史节点
        List<HistoricActivityInstance> hisActIns = historyService.createHistoricActivityInstanceQuery()
                .executionId(executionId)
                .orderByHistoricActivityInstanceStartTime()
                .asc().list();
        for (HistoricActivityInstance activityInstance : hisActIns) {
            if (!"sequenceFlow".equals(activityInstance.getActivityType())) {
                flowViewerDto = new FlowViewerDto();
                flowViewerDto.setKey(activityInstance.getActivityId());
                // 根据流程节点处理时间校验改节点是否已完成
                flowViewerDto.setCompleted(!Objects.isNull(activityInstance.getEndTime()));
                flowViewerList.add(flowViewerDto);
            }
        }
        return AjaxResult.success(flowViewerList);
    }

    /**
     * 获取流程变量
     * 同时将各节点命名空间（{taskDefKey}__formData）下的字段值平铺到顶层，
     * 便于前端 setFormData 按字段 id 回填
     *
     * @param taskId
     * @return
     */
    @Override
    public AjaxResult processVariables(String taskId) {
        Map<String, Object> variables;
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery()
                .includeProcessVariables().finished().taskId(taskId).singleResult();
        if (Objects.nonNull(historicTaskInstance)) {
            variables = new HashMap<>(historicTaskInstance.getProcessVariables());
        } else {
            variables = new HashMap<>(taskService.getVariables(taskId));
        }

        // 将所有 {taskDefKey}__formData 命名空间下的字段值平铺到顶层
        // 这样前端 setFormData(res.data) 能按字段 id 正确回填各节点数据
        for (Map.Entry<String, Object> entry : new HashMap<>(variables).entrySet()) {
            if (entry.getKey().endsWith("__formData") && entry.getValue() != null) {
                try {
                    JSONObject nsData = JSONObject.parseObject(JSON.toJSONString(entry.getValue()));
                    nsData.forEach(variables::put);
                } catch (Exception e) {
                    log.warn("展开命名空间 {} 数据失败", entry.getKey(), e);
                }
            }
        }

        return AjaxResult.success(variables);
    }

    /**
     * 审批任务获取下一节点
     *
     * @param flowTaskVo 任务
     * @return
     */
    @Override
    public AjaxResult getNextFlowNode(FlowTaskVo flowTaskVo) {
        // Step 1. 获取当前节点并找到下一步节点
        Task task = taskService.createTaskQuery().taskId(flowTaskVo.getTaskId()).singleResult();
        if (Objects.isNull(task)) {
            return AjaxResult.error("任务不存在或已被审批!");
        }
        // Step 2. 获取当前流程所有流程变量(网关节点时需要校验表达式)
        Map<String, Object> variables = taskService.getVariables(task.getId());
        List<UserTask> nextUserTask = FindNextNodeUtil.getNextUserTasks(repositoryService, task, variables);
        if (CollectionUtils.isEmpty(nextUserTask)) {
            return AjaxResult.success("流程已完结!", null);
        }
        return getFlowAttribute(nextUserTask);
    }

    /**
     * 发起流程获取下一节点
     *
     * @param flowTaskVo 任务
     * @return
     */
    @Override
    public AjaxResult getNextFlowNodeByStart(FlowTaskVo flowTaskVo) {
        // Step 1. 查找流程定义信息
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(flowTaskVo.getDeploymentId()).singleResult();
        if (Objects.isNull(processDefinition)) {
            return AjaxResult.error("流程信息不存在!");
        }
        // Step 2. 获取下一任务节点(网关节点时需要校验表达式)
        List<UserTask> nextUserTask = FindNextNodeUtil.getNextUserTasksByStart(repositoryService, processDefinition, flowTaskVo.getVariables());
        if (CollectionUtils.isEmpty(nextUserTask)) {
            return AjaxResult.error("暂未查找到下一任务,请检查流程设计是否正确!");
        }
        return getFlowAttribute(nextUserTask);
    }


    /**
     * 获取任务节点属性,包含自定义属性等
     *
     * @param nextUserTask
     */
    private AjaxResult getFlowAttribute(List<UserTask> nextUserTask) {
        FlowNextDto flowNextDto = new FlowNextDto();
        for (UserTask userTask : nextUserTask) {
            MultiInstanceLoopCharacteristics multiInstance = userTask.getLoopCharacteristics();
            // 会签节点
            if (Objects.nonNull(multiInstance)) {
                flowNextDto.setVars(multiInstance.getInputDataItem());
                flowNextDto.setType(ProcessConstants.PROCESS_MULTI_INSTANCE);
                flowNextDto.setDataType(ProcessConstants.DYNAMIC);
            } else {
                // 读取自定义节点属性 判断是否是否需要动态指定任务接收人员、组
                String dataType = userTask.getAttributeValue(ProcessConstants.NAMASPASE, ProcessConstants.PROCESS_CUSTOM_DATA_TYPE);
                String userType = userTask.getAttributeValue(ProcessConstants.NAMASPASE, ProcessConstants.PROCESS_CUSTOM_USER_TYPE);
                flowNextDto.setVars(ProcessConstants.PROCESS_APPROVAL);
                flowNextDto.setType(userType);
                flowNextDto.setDataType(dataType);
            }
        }
        return AjaxResult.success(flowNextDto);
    }

    /**
     * 流程初始化表单
     *
     * @param deployId
     * @return
     */
    @Override
    public AjaxResult flowFormData(String deployId) {
        // 第一次申请获取初始化表单
        if (StringUtils.isNotBlank(deployId)) {
            SysForm sysForm = sysInstanceFormService.selectSysDeployFormByDeployId(deployId);
            if (Objects.isNull(sysForm)) {
                return AjaxResult.error("请先配置流程表单!");
            }
            return AjaxResult.success(JSONObject.parseObject(sysForm.getFormContent()));
        } else {
            return AjaxResult.error("参数错误!");
        }
    }

    /**
     * 流程节点信息
     *
     * @param procInsId
     * @return
     */
    @Override
    public AjaxResult flowXmlAndNode(String procInsId, String deployId) {
        try {
            List<FlowViewerDto> flowViewerList = new ArrayList<>();
            // 获取已经完成的节点
            List<HistoricActivityInstance> listFinished = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(procInsId)
                    .finished()
                    .list();

            // 保存已经完成的流程节点编号
            listFinished.forEach(s -> {
                FlowViewerDto flowViewerDto = new FlowViewerDto();
                flowViewerDto.setKey(s.getActivityId());
                flowViewerDto.setCompleted(true);
                // 退回节点不进行展示
                if (StringUtils.isBlank(s.getDeleteReason())) {
                    flowViewerList.add(flowViewerDto);
                }
            });

            // 获取代办节点
            List<HistoricActivityInstance> listUnFinished = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(procInsId)
                    .unfinished()
                    .list();

            // 保存需要代办的节点编号
            listUnFinished.forEach(s -> {
                // 删除已退回节点
                flowViewerList.removeIf(task -> task.getKey().equals(s.getActivityId()));
                FlowViewerDto flowViewerDto = new FlowViewerDto();
                flowViewerDto.setKey(s.getActivityId());
                flowViewerDto.setCompleted(false);
                flowViewerList.add(flowViewerDto);
            });
            Map<String, Object> result = new HashMap();
            // xmlData 数据
            ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().deploymentId(deployId).singleResult();
            InputStream inputStream = repositoryService.getResourceAsStream(definition.getDeploymentId(), definition.getResourceName());
            String xmlData = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            result.put("nodeData", flowViewerList);
            result.put("xmlData", xmlData);
            return AjaxResult.success(result);
        } catch (Exception e) {
            return AjaxResult.error("高亮历史任务失败");
        }
    }

    /**
     * 流程节点表单
     *
     * @param taskId 流程任务编号
     * @return
     */
    @Override
    public AjaxResult flowTaskForm(String taskId) throws Exception {
        // 先查运行时任务（待办），再查历史任务（已办/已完成流程）
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        // 查历史任务（无论是否已完成都需要，用于获取流程变量和判断完成状态）
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery()
                .includeProcessVariables()
                .taskId(taskId)
                .singleResult();

        // 运行时和历史都找不到，则任务不存在
        if (Objects.isNull(task) && Objects.isNull(historicTaskInstance)) {
            return AjaxResult.error("任务不存在");
        }

        // 流程变量：
        // - 已完成流程：用 historicVariableInstance 查整个流程的最终变量（更完整）
        // - 活跃任务：从运行时 taskService 取变量
        Map<String, Object> parameters = new HashMap<>();
        if (Objects.nonNull(historicTaskInstance) && Objects.nonNull(historicTaskInstance.getProcessVariables())) {
            parameters = new HashMap<>(historicTaskInstance.getProcessVariables());
        } else if (Objects.nonNull(task)) {
            parameters = new HashMap<>(taskService.getVariables(taskId));
        }

        // 构建返回给前端的 formJson（表单结构定义）
        JSONObject formJson = new JSONObject();

        // 从运行时或历史实例中取 taskDefinitionKey 和 processInstanceId
        String taskDefKey = Objects.nonNull(task)
                ? task.getTaskDefinitionKey()
                : historicTaskInstance.getTaskDefinitionKey();
        String procInsId4Form = Objects.nonNull(task)
                ? task.getProcessInstanceId()
                : historicTaskInstance.getProcessInstanceId();

        // ── 判断是否为已完成任务（已办任务详情需要展示所有节点的表单） ──
        // 流程已结束（历史流程实例有 endTime）或该任务本身已完成，则进入聚合分支
        boolean isFinished = (Objects.nonNull(historicTaskInstance) && Objects.nonNull(historicTaskInstance.getEndTime()));
        if (!isFinished && Objects.nonNull(procInsId4Form)) {
            HistoricProcessInstance hpiCheck = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(procInsId4Form).singleResult();
            if (Objects.nonNull(hpiCheck) && Objects.nonNull(hpiCheck.getEndTime())) {
                isFinished = true;
            }
        }

        if (isFinished) {
            // ── 已完成任务：聚合该流程实例所有历史节点的表单，按节点顺序合并 widgetList ──
            String procInsId = procInsId4Form;

            // 重新加载整个流程实例的最终变量（historicTaskInstance 的变量快照只含该任务完成时的变量，
            // 后续节点设置的变量不会包含在其中，因此需要用流程级别的历史变量查询）
            final Map<String, Object> paramsFinal = parameters;
            historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(procInsId)
                    .list()
                    .forEach(v -> paramsFinal.put(v.getVariableName(), v.getValue()));

            // 从 BPMN 模型中建立 taskDefinitionKey → formKey 的映射
            // HistoricTaskInstance.getFormKey() 在历史查询中通常返回 null，
            // 必须从流程定义（BPMN）的 UserTask 节点定义中读取 formKey
            HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(procInsId).singleResult();
            Map<String, String> nodeFormKeyMap = new HashMap<>();
            if (Objects.nonNull(hpi)) {
                BpmnModel bpmnModel = repositoryService.getBpmnModel(hpi.getProcessDefinitionId());
                if (Objects.nonNull(bpmnModel)) {
                    bpmnModel.getMainProcess().getFlowElements().forEach(el -> {
                        if (el instanceof UserTask) {
                            UserTask ut = (UserTask) el;
                            if (StringUtils.isNotBlank(ut.getFormKey())) {
                                nodeFormKeyMap.put(ut.getId(), ut.getFormKey());
                            }
                        }
                    });
                }
            }

            // 查所有历史任务（不限 finished，因为 deleteProcessInstance 终止的流程中
            // 被强制结束的任务也需要纳入聚合范围）
            List<HistoricTaskInstance> allHistoricTasks = historyService.createHistoricTaskInstanceQuery()
                    .processInstanceId(procInsId)
                    .orderByHistoricTaskInstanceStartTime().asc()
                    .list();

            List<JSONObject> mergedWidgetList = new ArrayList<>();
            JSONObject baseFormConfig = null;

            // 去重：同一 taskDefinitionKey 只取第一次出现（避免退回后重复节点叠加）
            Set<String> seenTaskDefKeys = new LinkedHashSet<>();
            for (HistoricTaskInstance ht : allHistoricTasks) {
                if (!seenTaskDefKeys.add(ht.getTaskDefinitionKey())) {
                    continue;
                }
                // 从 BPMN 模型映射中取 formKey，而非 ht.getFormKey()（历史实例该字段为空）
                String htFormKey = nodeFormKeyMap.get(ht.getTaskDefinitionKey());
                if (StringUtils.isBlank(htFormKey)) {
                    continue;
                }
                try {
                    SysForm htForm = sysFormService.selectSysFormById(Long.parseLong(htFormKey));
                    if (Objects.isNull(htForm)) continue;

                    JSONObject htFormJson = JSONObject.parseObject(htForm.getFormContent());
                    if (Objects.isNull(baseFormConfig) && Objects.nonNull(htFormJson.get("formConfig"))) {
                        baseFormConfig = htFormJson.getJSONObject("formConfig");
                    }

                    List<JSONObject> htFields = JSON.parseObject(
                            JSON.toJSONString(htFormJson.get("widgetList")),
                            new TypeReference<List<JSONObject>>() {});

                    // 将该节点命名空间的字段值平铺到 parameters，供前端 setFormData 回填
                    String nsKey = ht.getTaskDefinitionKey() + "__formData";
                    Object nsData = parameters.get(nsKey);
                    if (Objects.nonNull(nsData)) {
                        JSONObject savedData = JSONObject.parseObject(JSON.toJSONString(nsData));
                        savedData.forEach(parameters::put);
                    }

                    // 所有字段设为只读（已办任务不可编辑）
                    for (JSONObject field : htFields) {
                        JSONObject options = field.getJSONObject("options");
                        if (Objects.nonNull(options)) {
                            options.put("disabled", true);
                        }
                    }
                    mergedWidgetList.addAll(htFields);
                } catch (NumberFormatException e) {
                    log.warn("节点 {} 的 formKey 不是有效数字: {}", ht.getTaskDefinitionKey(), htFormKey);
                }
            }

            formJson.put("widgetList", mergedWidgetList);
            formJson.put("formConfig", Objects.nonNull(baseFormConfig) ? baseFormConfig : buildDefaultFormConfig());

        } else if (Objects.nonNull(task) && StringUtils.isNotBlank(task.getFormKey())) {
            // ── 待办任务：只显示当前节点绑定的表单，各节点数据互相隔离 ──
            SysForm sysForm = sysFormService.selectSysFormById(Long.parseLong(task.getFormKey()));
            if (Objects.nonNull(sysForm)) {
                formJson = JSONObject.parseObject(sysForm.getFormContent());
                // 从节点命名空间取已保存的字段值
                String nsKey = taskDefKey + "__formData";
                Object nsData = parameters.get(nsKey);
                if (Objects.nonNull(nsData)) {
                    JSONObject savedData = JSONObject.parseObject(JSON.toJSONString(nsData));
                    savedData.forEach(parameters::put);
                } else {
                    // 兼容旧数据：图片上传组件给空列表防止 vform 渲染报错
                    List<JSONObject> fields = JSON.parseObject(
                            JSON.toJSONString(formJson.get("widgetList")),
                            new TypeReference<List<JSONObject>>() {});
                    for (JSONObject field : fields) {
                        String key = field.getString("id");
                        if ("picture-upload".equals(field.getString("type"))
                                && !parameters.containsKey(key)) {
                            parameters.put(key, new ArrayList<>());
                        }
                    }
                }
            }
        } else {
            // ── 当前节点未绑定表单：回退显示流程变量里存储的起始表单 ──
            Object formJsonObj = parameters.get("formJson");
            if (Objects.nonNull(formJsonObj)) {
                formJson = JSONObject.parseObject(JSON.toJSONString(formJsonObj));
                List<JSONObject> fields = JSON.parseObject(
                        JSON.toJSONString(formJson.get("widgetList")),
                        new TypeReference<List<JSONObject>>() {});
                for (JSONObject field : fields) {
                    JSONObject options = field.getJSONObject("options");
                    if (Objects.nonNull(options)) {
                        options.put("disabled", true);
                    }
                }
                formJson.put("widgetList", fields);
            }
        }

        // 兜底：确保 formJson 同时包含 widgetList 和 formConfig
        if (Objects.isNull(formJson.get("widgetList"))) {
            formJson.put("widgetList", new com.alibaba.fastjson2.JSONArray());
        }
        if (Objects.isNull(formJson.get("formConfig"))) {
            formJson.put("formConfig", buildDefaultFormConfig());
        }
        parameters.put("formJson", formJson);
        parameters.put("_taskDefinitionKey", taskDefKey);

        log.info("获取任务表单 {}，formKey: {}，nodeKey: {}，已完成: {}", taskId,
                Objects.nonNull(task) ? task.getFormKey() : "(historic)", taskDefKey, isFinished);
        return AjaxResult.success(parameters);
    }

    /** 构建 vform 默认 formConfig，确保 setFormJson 不报格式错误 */
    private JSONObject buildDefaultFormConfig() {
        JSONObject cfg = new JSONObject();
        cfg.put("modelName", "formData");
        cfg.put("refName", "vForm");
        cfg.put("rulesName", "rules");
        cfg.put("labelWidth", 80);
        cfg.put("labelPosition", "left");
        cfg.put("size", "");
        cfg.put("labelAlign", "label-left-align");
        cfg.put("cssCode", "");
        cfg.put("customClass", new com.alibaba.fastjson2.JSONArray());
        cfg.put("functions", "");
        cfg.put("layoutType", "PC");
        cfg.put("jsonVersion", 3);
        return cfg;
    }

    /**
     * 流程节点信息
     *
     * @param procInsId
     * @param elementId
     * @return
     */
    @Override
    public AjaxResult flowTaskInfo(String procInsId, String elementId) {
        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(procInsId)
                .activityId(elementId)
                .list();
        // 退回任务后有多条数据 只取待办任务进行展示
        list.removeIf(task -> StringUtils.isNotBlank(task.getDeleteReason()));
        if (CollectionUtils.isEmpty(list)) {
            return AjaxResult.success();
        }
        if (list.size() > 1) {
            list.removeIf(task -> Objects.nonNull(task.getEndTime()));
        }
        HistoricActivityInstance histIns = list.get(0);
        FlowTaskDto flowTask = new FlowTaskDto();
        flowTask.setTaskId(histIns.getTaskId());
        flowTask.setTaskName(histIns.getActivityName());
        flowTask.setCreateTime(histIns.getStartTime());
        flowTask.setFinishTime(histIns.getEndTime());
        if (StringUtils.isNotBlank(histIns.getAssignee())) {
            SysUser sysUser = sysUserService.selectUserById(Long.parseLong(histIns.getAssignee()));
            flowTask.setAssigneeId(sysUser.getUserId());
            flowTask.setAssigneeName(sysUser.getNickName());
            flowTask.setDeptName(Objects.nonNull(sysUser.getDept()) ? sysUser.getDept().getDeptName() : "");

        }
        // 流程变量信息
//        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery()
//                .includeProcessVariables().finished().taskId(histIns.getTaskId()).singleResult();
//        flowTask.setVariables(historicTaskInstance.getProcessVariables());

        // 展示审批人员
        List<HistoricIdentityLink> linksForTask = historyService.getHistoricIdentityLinksForTask(histIns.getTaskId());
        StringBuilder stringBuilder = new StringBuilder();
        for (HistoricIdentityLink identityLink : linksForTask) {
            // 获选人,候选组/角色(多个)
            if ("candidate".equals(identityLink.getType())) {
                if (StringUtils.isNotBlank(identityLink.getUserId())) {
                    SysUser sysUser = sysUserService.selectUserById(Long.parseLong(identityLink.getUserId()));
                    stringBuilder.append(sysUser.getNickName()).append(",");
                }
                if (StringUtils.isNotBlank(identityLink.getGroupId())) {
                    SysRole sysRole = sysRoleService.selectRoleById(Long.parseLong(identityLink.getGroupId()));
                    stringBuilder.append(sysRole.getRoleName()).append(",");
                }
            }
        }
        if (StringUtils.isNotBlank(stringBuilder)) {
            flowTask.setCandidate(stringBuilder.substring(0, stringBuilder.length() - 1));
        }

        flowTask.setDuration(histIns.getDurationInMillis() == null || histIns.getDurationInMillis() == 0 ? null : getDate(histIns.getDurationInMillis()));
        // 获取意见评论内容
        List<Comment> commentList = taskService.getProcessInstanceComments(histIns.getProcessInstanceId());
        commentList.forEach(comment -> {
            if (histIns.getTaskId().equals(comment.getTaskId())) {
                flowTask.setComment(FlowCommentDto.builder().type(comment.getType()).comment(comment.getFullMessage()).build());
            }
        });
        return AjaxResult.success(flowTask);
    }

    /**
     * 更新任务候选人（当班组配置改变时调用）
     *
     * @param taskId 任务ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateTaskCandidates(String taskId) {
        try {
            log.info("========== 开始更新任务候选人 ==========");
            log.info("taskId: {}", taskId);

            // 1. 获取任务
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            if (task == null) {
                throw new CustomException("任务不存在");
            }

            // 2. 获取流程变量（最新班组配置）
            Map<String, Object> vars = taskService.getVariables(taskId);
            Long currentTeamId = resolveTeamId(vars, task.getTaskDefinitionKey());

            if (currentTeamId == null) {
                log.warn("无班组配置，跳过更新");
                return;
            }

            log.info("当前班组ID: {}", currentTeamId);

            // 3. 删除旧候选人
            List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
            int deletedUserCount = 0;
            int deletedGroupCount = 0;

            for (IdentityLink link : identityLinks) {
                if ("candidate".equals(link.getType())) {
                    if (link.getUserId() != null) {
                        taskService.deleteCandidateUser(taskId, link.getUserId());
                        deletedUserCount++;
                        log.info("删除候选用户: {}", link.getUserId());
                    }
                    if (link.getGroupId() != null) {
                        taskService.deleteCandidateGroup(taskId, link.getGroupId());
                        deletedGroupCount++;
                        log.info("删除候选角色: {}", link.getGroupId());
                    }
                }
            }

            log.info("删除候选人完成：用户{}个, 角色{}个", deletedUserCount, deletedGroupCount);

            // 4. 查询新班组成员
            List<SysUser> members = productionTeamMapper.selectUserListByTeamId(currentTeamId);
            log.info("班组{}的成员数: {}", currentTeamId, members == null ? 0 : members.size());

            if (members == null || members.isEmpty()) {
                log.warn("班组{}无成员，跳过候选人添加", currentTeamId);
                log.info("========== 任务候选人更新完成 ==========");
                return;
            }

            // 5. 重新添加候选人
            Set<Long> roleIds = new HashSet<>();
            for (SysUser user : members) {
                taskService.addCandidateUser(taskId, user.getUserId().toString());
                log.info("添加候选用户: {} ({})", user.getUserId(), user.getUserName());

                if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                    user.getRoles().forEach(role -> {
                        roleIds.add(role.getRoleId());
                        log.debug("班组成员{}拥有角色{} ({})", user.getUserId(), role.getRoleId(), role.getRoleName());
                    });
                }
            }

            for (Long roleId : roleIds) {
                taskService.addCandidateGroup(taskId, roleId.toString());
                log.info("添加候选角色: {}", roleId);
            }

            log.info("✅ 成功更新任务{}的候选人，新班组ID={}，添加用户{}个，角色{}个",
                    taskId, currentTeamId, members.size(), roleIds.size());
            log.info("========== 任务候选人更新完成 ==========");

        } catch (Exception e) {
            log.error("❌ 更新任务候选人失败，taskId: {}", taskId, e);
            throw new CustomException("更新任务候选人失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新流程实例下的所有任务候选人
     *
     * @param procInstId 流程实例ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateFlowInstanceTasksCandidates(String procInstId) {
        try {
            log.info("========== 开始批量更新流程实例任务候选人 ==========");
            log.info("procInstId: {}", procInstId);

            // 获取该实例下所有活跃任务
            List<Task> tasks = taskService.createTaskQuery()
                    .processInstanceId(procInstId)
                    .active()
                    .list();

            log.info("找到{}个活跃任务", tasks.size());

            if (tasks.isEmpty()) {
                log.warn("该流程实例无活跃任务");
                return;
            }

            // 逐一更新
            int successCount = 0;
            int failCount = 0;

            for (Task task : tasks) {
                try {
                    log.info("正在更新任务: {}", task.getId());
                    updateTaskCandidates(task.getId());
                    successCount++;
                } catch (Exception e) {
                    log.error("更新任务{}失败，继续处理下一个", task.getId(), e);
                    failCount++;
                }
            }

            log.info("✅ 流程实例{}的任务候选人更新完成，成功{}个，失败{}个",
                    procInstId, successCount, failCount);
            log.info("========== 批量更新完成 ==========");

        } catch (Exception e) {
            log.error("❌ 批量更新流程实例任务候选人失败", e);
            throw new CustomException("批量更新失败: " + e.getMessage());
        }
    }

    /**
     * 将Object类型的数据转化成Map<String,Object>
     *
     * @param obj
     * @return
     * @throws Exception
     */
    public Map<String, Object> obj2Map(Object obj) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            map.put(field.getName(), field.get(obj));
        }
        return map;
    }

    /**
     * 流程完成时间处理
     *
     * @param ms
     * @return
     */
    private String getDate(long ms) {

        long day = ms / (24 * 60 * 60 * 1000);
        long hour = (ms / (60 * 60 * 1000) - day * 24);
        long minute = ((ms / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long second = (ms / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - minute * 60);

        if (day > 0) {
            return day + "天" + hour + "小时" + minute + "分钟";
        }
        if (hour > 0) {
            return hour + "小时" + minute + "分钟";
        }
        if (minute > 0) {
            return minute + "分钟";
        }
        if (second > 0) {
            return second + "秒";
        } else {
            return 0 + "秒";
        }
    }

    /**
     * 从流程变量中解析班组ID
     *
     * @param vars 流程变量
     * @param nodeKey 节点key
     * @return 班组ID
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
}
