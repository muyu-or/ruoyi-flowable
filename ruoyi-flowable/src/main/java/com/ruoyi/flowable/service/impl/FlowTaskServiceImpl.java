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
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.flowable.domain.dto.*;
import com.ruoyi.flowable.domain.vo.FlowQueryVo;
import com.ruoyi.flowable.domain.vo.FlowTaskVo;
import com.ruoyi.flowable.factory.FlowServiceFactory;
import com.ruoyi.flowable.flow.CustomProcessDiagramGenerator;
import com.ruoyi.flowable.flow.FindNextNodeUtil;
import com.ruoyi.flowable.flow.FlowableUtils;
import com.ruoyi.flowable.service.IFlowTaskService;
import com.ruoyi.flowable.service.IFlowTeamService;
import com.ruoyi.flowable.service.IInventoryLinkageService;
import com.ruoyi.flowable.service.IReportLinkageService;
import com.ruoyi.flowable.service.ITaskWarningService;
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

    /**
     * 节点 taskDefinitionKey → 自定义 Vue 组件名 兜底映射。
     * 当 BPMN 模型未通过 extensionElements 绑定 formComponent 时，
     * 用此映射识别自定义组件节点（与前端 TASK_FORM_MAP 对应）。
     */
    private static final Map<String, String> FALLBACK_COMPONENT_MAP;
    static {
        Map<String, String> m = new HashMap<>();
        m.put("Activity_1uqk506", "StockInForm");
        m.put("Activity_01xy3yd", "StockOutForm");
        m.put("Activity_0kzrvj3", "PreprocessForm");
        m.put("Activity_17q9igw", "VacuumForm");
        m.put("Activity_1qot9f7", "BakingForm");
        m.put("Activity_0tn05o0", "TestForm");
        m.put("Activity_1lnd3md", "FinalStockInForm");
        FALLBACK_COMPONENT_MAP = Collections.unmodifiableMap(m);
    }

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

    @Resource
    private IInventoryLinkageService inventoryLinkageService;

    @Resource
    private IReportLinkageService reportLinkageService;

    @Resource
    private IFlowTeamService flowTeamService;

    @Resource
    private com.ruoyi.system.service.ITaskNodeExecutionService taskNodeExecutionService;

    @Resource
    private com.ruoyi.system.mapper.TaskNodeExecutionMapper taskNodeExecutionMapper;

    @Resource
    private ITaskWarningService taskWarningService;

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
            // assignee 设为实际处理人：如果班组成员已提交过表单，记录班组成员为 assignee；否则记录当前操作人
            String actualAssignee = userId.toString();
            try {
                com.ruoyi.system.domain.TaskNodeExecution nodeExec = taskNodeExecutionService.selectByTaskId(taskVo.getTaskId());
                if (nodeExec != null && nodeExec.getClaimUserId() != null
                        && !nodeExec.getClaimUserId().equals(userId)) {
                    actualAssignee = nodeExec.getClaimUserId().toString();
                }
            } catch (Exception e) {
                log.warn("查询节点执行记录失败，taskId={}，使用当前用户作为assignee", taskVo.getTaskId(), e);
            }
            taskService.setAssignee(taskVo.getTaskId(), actualAssignee);
            // 完成任务，传递合并后的所有变量到下一节点
            taskService.complete(taskVo.getTaskId(), allVariables);

            // 库存联动：根据节点 key 自动执行入库/出库操作（失败时事务回滚）
            inventoryLinkageService.handleNodeCompletion(task.getTaskDefinitionKey(), allVariables);

            // 测试报告联动：审批通过后，将表单中暂存的测试报告元数据写入 report_record 表
            try {
                reportLinkageService.handleReportOnCompletion(task.getTaskDefinitionKey(), allVariables);
            } catch (Exception e) {
                log.warn("测试报告写入 report_record 时出错，节点={}", task.getTaskDefinitionKey(), e);
            }

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

            // 任务完成后，自动将该节点的未读预警标为已读
            try {
                String procInstId = task.getProcessInstanceId();
                String nodeKey = task.getTaskDefinitionKey();
                int updated = taskWarningService.markReadByProcInstAndNodeKey(procInstId, nodeKey);
                if (updated > 0) {
                    log.info("任务 {} 完成，已自动标记 {} 条预警为已读，节点: {}", taskVo.getTaskId(), updated, nodeKey);
                }
            } catch (Exception e) {
                log.warn("自动标记预警已读时出错", e);
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

        // ── 退回前：将当前节点的执行记录标记为 "rejected"（退回视为当前节点失败）──
        try {
            Long returnUserId = SecurityUtils.getLoginUser().getUser().getUserId();
            com.ruoyi.common.utils.spring.SpringUtils.getBean(com.ruoyi.flowable.service.IFlowTeamService.class)
                    .onTaskCompleted(task.getId(), "rejected", comment, returnUserId);
            log.info("退回：已将任务 {} 节点执行记录标记为 rejected", task.getId());
        } catch (Exception e) {
            log.warn("退回：更新当前节点执行记录状态失败，taskId={}", task.getId(), e);
        }

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
     * 退回重审：将当前节点重置为待处理状态，由同班组人员重新处理。
     * <p>
     * 与"退回上一节点"不同，本操作不改变流程节点位置，仅：
     * 1. 完成当前任务（以"重审"意见）
     * 2. 通过 changeActivityState 将流程跳回当前节点自身
     * 3. 重新注入班组候选人，使同班组成员可以在待办列表再次看到该任务
     * </p>
     *
     * @param flowTaskVo 请求实体参数（taskId、comment 必填）
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void redoTask(FlowTaskVo flowTaskVo) {
        Task task = taskService.createTaskQuery().taskId(flowTaskVo.getTaskId()).singleResult();
        if (Objects.isNull(task)) {
            throw new CustomException("任务不存在");
        }
        if (task.isSuspended()) {
            throw new CustomException("任务处于挂起状态");
        }

        String procInsId = task.getProcessInstanceId();
        String currentTaskKey = task.getTaskDefinitionKey();
        String comment = StringUtils.isBlank(flowTaskVo.getComment()) ? "退回重审" : flowTaskVo.getComment();

        // 记录退回重审意见
        taskService.addComment(task.getId(), procInsId, FlowComment.REBACK.getType(), comment);

        // 更新当前节点执行记录状态为 "rejected"（退回重审视为本轮未通过）
        try {
            Long userId = SecurityUtils.getLoginUser().getUser().getUserId();
            com.ruoyi.common.utils.spring.SpringUtils.getBean(com.ruoyi.flowable.service.IFlowTeamService.class)
                    .onTaskCompleted(task.getId(), "rejected", comment, userId);
        } catch (Exception e) {
            log.warn("退回重审：更新节点执行记录失败，taskId={}", task.getId(), e);
        }

        // 获取当前所有活跃任务 key（用于 moveActivityIds）
        List<Task> runTaskList = taskService.createTaskQuery().processInstanceId(procInsId).list();
        List<String> currentActivityIds = runTaskList.stream()
                .map(Task::getTaskDefinitionKey)
                .collect(Collectors.toList());
        if (currentActivityIds.isEmpty()) {
            throw new CustomException("当前流程无活跃任务，无法退回重审");
        }

        log.info("退回重审：procInsId={}, nodeKey={}", procInsId, currentTaskKey);

        try {
            // 将当前节点跳回自身（重新激活同一节点，产生新的任务实例）
            runtimeService.createChangeActivityStateBuilder()
                    .processInstanceId(procInsId)
                    .moveActivityIdsToSingleActivityId(currentActivityIds, currentTaskKey)
                    .changeState();
        } catch (FlowableObjectNotFoundException e) {
            throw new CustomException("未找到流程实例，流程可能已发生变化");
        } catch (FlowableException e) {
            log.error("退回重审失败", e);
            throw new CustomException("退回重审失败：" + e.getMessage());
        }

        // 重新注入班组候选人，确保同班组成员在待办列表可见
        try {
            List<Task> reactivatedTasks = taskService.createTaskQuery()
                    .processInstanceId(procInsId)
                    .taskDefinitionKey(currentTaskKey)
                    .active()
                    .list();
            for (Task reactivatedTask : reactivatedTasks) {
                log.info("退回重审：为新任务 {} 注入班组候选人", reactivatedTask.getId());
                try {
                    com.ruoyi.common.utils.spring.SpringUtils.getBean(
                            com.ruoyi.flowable.service.IFlowTeamService.class)
                            .injectTeamCandidates(reactivatedTask.getId(),
                                    procInsId,
                                    reactivatedTask.getTaskDefinitionKey(),
                                    reactivatedTask.getName());
                } catch (Exception e) {
                    log.warn("退回重审：为任务 {} 注入候选人时出错", reactivatedTask.getId(), e);
                }
            }
        } catch (Exception e) {
            log.warn("退回重审：注入候选人失败，procInsId={}", procInsId, e);
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
        // 管理角色（admin 或拥有 flowable:stat:all 权限）可查看所有人发起的流程
        boolean viewAll = SecurityUtils.isAdmin(userId)
                || SecurityUtils.hasPermi("flowable:stat:all");
        HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery()
                .orderByProcessInstanceStartTime()
                .desc();
        if (!viewAll) {
            historicProcessInstanceQuery.startedBy(userId.toString());
        }
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
            // 业务任务名称（发起流程时填写的 taskName 流程变量）
            List<org.flowable.variable.api.history.HistoricVariableInstance> taskNameVars = historyService
                    .createHistoricVariableInstanceQuery()
                    .processInstanceId(hisIns.getId())
                    .variableName("taskName")
                    .list();
            if (CollectionUtils.isNotEmpty(taskNameVars) && taskNameVars.get(0).getValue() != null) {
                flowTask.setBusinessTaskName(taskNameVars.get(0).getValue().toString());
            }
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

        // 将该流程实例下所有未完成的节点执行记录标记为已取消，保证统计数据正确
        try {
            com.ruoyi.system.service.ITaskExecutionRecordService recordService =
                    com.ruoyi.common.utils.spring.SpringUtils.getBean(
                            com.ruoyi.system.service.ITaskExecutionRecordService.class);
            com.ruoyi.system.domain.TaskExecutionRecord execRecord =
                    recordService.selectByProcInstId(flowTaskVo.getInstanceId());
            if (execRecord != null) {
                com.ruoyi.system.service.ITaskNodeExecutionService nodeExecService =
                        com.ruoyi.common.utils.spring.SpringUtils.getBean(
                                com.ruoyi.system.service.ITaskNodeExecutionService.class);
                nodeExecService.cancelByExecRecordId(execRecord.getId());
                log.info("已将流程实例 {} 的节点执行记录标记为已取消", flowTaskVo.getInstanceId());
            }
        } catch (Exception e) {
            log.warn("标记节点执行记录为取消状态时出错，不影响流程取消", e);
        }

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

            // 业务任务名称（发起流程时填写的 taskName 流程变量）
            Map<String, Object> procVars = task.getProcessVariables();
            if (procVars != null && procVars.get("taskName") != null) {
                flowTask.setBusinessTaskName(procVars.get("taskName").toString());
            }

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

            // 查询节点执行状态（submitted=已提交待审批，pending=待处理，claimed=已认领）
            try {
                com.ruoyi.system.domain.TaskNodeExecution nodeExec = taskNodeExecutionService.selectByTaskId(task.getId());
                if (nodeExec != null) {
                    flowTask.setNodeStatus(nodeExec.getStatus());
                }
            } catch (Exception e) {
                log.warn("查询节点执行状态失败，taskId={}", task.getId(), e);
            }

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

        // 权限分层：admin/zongguan 看全部，班组长看班组，普通成员看自己
        boolean viewAll = SecurityUtils.isAdmin(userId)
                || SecurityUtils.hasPermi("flowable:stat:all");

        List<HistoricTaskInstance> mergedList;

        if (viewAll) {
            // ---- 管理角色：查所有人已完成的任务 ----
            mergedList = historyService.createHistoricTaskInstanceQuery()
                    .includeProcessVariables()
                    .finished()
                    .orderByHistoricTaskInstanceEndTime()
                    .desc()
                    .list();
        } else {
            // 查询当前用户是否为班组长（管辖的班组有成员）
            List<Long> teamMemberIds = productionTeamMapper.selectMemberUserIdsByLeaderId(userId);
            boolean isLeader = teamMemberIds != null && !teamMemberIds.isEmpty();

            if (isLeader) {
                // ---- 班组长：查班组所有成员已完成的任务 ----
                Set<String> memberIdStrings = teamMemberIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.toSet());

                // 用 OR 查询每个成员的 assignee 任务
                mergedList = new ArrayList<>();
                for (String memberId : memberIdStrings) {
                    List<HistoricTaskInstance> memberTasks = historyService.createHistoricTaskInstanceQuery()
                            .includeProcessVariables()
                            .finished()
                            .taskAssignee(memberId)
                            .orderByHistoricTaskInstanceEndTime()
                            .desc()
                            .list();
                    mergedList.addAll(memberTasks);
                }
                // 补充 task_node_execution 中班组成员参与过的任务
                for (Long memberId : teamMemberIds) {
                    List<String> extraIds = taskNodeExecutionMapper.selectFinishedTaskIdsByClaimUserId(memberId);
                    if (extraIds != null && !extraIds.isEmpty()) {
                        Set<String> existingIds = mergedList.stream()
                                .map(HistoricTaskInstance::getId)
                                .collect(Collectors.toSet());
                        List<String> newIds = extraIds.stream()
                                .filter(id -> !existingIds.contains(id))
                                .collect(Collectors.toList());
                        if (!newIds.isEmpty()) {
                            List<HistoricTaskInstance> extra = historyService.createHistoricTaskInstanceQuery()
                                    .includeProcessVariables()
                                    .finished()
                                    .taskIds(newIds)
                                    .list();
                            mergedList.addAll(extra);
                        }
                    }
                }
                // 去重（同一个 taskId 可能出现多次）
                Map<String, HistoricTaskInstance> deduped = new LinkedHashMap<>();
                for (HistoricTaskInstance ht : mergedList) {
                    deduped.putIfAbsent(ht.getId(), ht);
                }
                mergedList = new ArrayList<>(deduped.values());
            } else {
                // ---- 普通成员：只看自己的（保持原有逻辑） ----
                // 1. 从 Flowable 查 assignee 为当前用户的已完成任务
                List<HistoricTaskInstance> allAssigneeList = historyService.createHistoricTaskInstanceQuery()
                        .includeProcessVariables()
                        .finished()
                        .taskAssignee(userId.toString())
                        .orderByHistoricTaskInstanceEndTime()
                        .desc()
                        .list();

                // 2. 从 task_node_execution 查当前用户参与过的任务
                List<String> memberTaskIds = taskNodeExecutionMapper.selectFinishedTaskIdsByClaimUserId(userId);

                // 3. 找出额外 task_id（去重）
                Set<String> assigneeTaskIds = new HashSet<>();
                for (HistoricTaskInstance ht : allAssigneeList) {
                    assigneeTaskIds.add(ht.getId());
                }
                List<String> extraTaskIds = new ArrayList<>();
                for (String tid : memberTaskIds) {
                    if (!assigneeTaskIds.contains(tid)) {
                        extraTaskIds.add(tid);
                    }
                }

                // 4. 查出额外 task_id 对应的 HistoricTaskInstance
                mergedList = new ArrayList<>(allAssigneeList);
                if (!extraTaskIds.isEmpty()) {
                    List<HistoricTaskInstance> extraList = historyService.createHistoricTaskInstanceQuery()
                            .includeProcessVariables()
                            .finished()
                            .taskIds(extraTaskIds)
                            .orderByHistoricTaskInstanceEndTime()
                            .desc()
                            .list();
                    mergedList.addAll(extraList);
                }
            }
        }

        // 按 endTime 降序排序
        mergedList.sort((a, b) -> {
            if (a.getEndTime() == null && b.getEndTime() == null) return 0;
            if (a.getEndTime() == null) return 1;
            if (b.getEndTime() == null) return -1;
            return b.getEndTime().compareTo(a.getEndTime());
        });

        // 手动分页
        page.setTotal(mergedList.size());
        int start = queryVo.getPageSize() * (queryVo.getPageNum() - 1);
        int end = Math.min(start + queryVo.getPageSize(), mergedList.size());
        List<HistoricTaskInstance> historicTaskInstanceList = start < mergedList.size()
                ? mergedList.subList(start, end) : new ArrayList<>();

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

            // 业务任务名称（发起流程时填写的 taskName 流程变量）
            Map<String, Object> histProcVars = histTask.getProcessVariables();
            if (histProcVars != null && histProcVars.get("taskName") != null) {
                flowTask.setBusinessTaskName(histProcVars.get("taskName").toString());
            }

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

            // 读取 nodeTimeMap（节点计划时间范围），用于在流转记录中显示
            Map<String, Object> nodeTimeMap = null;
            try {
                List<org.flowable.variable.api.history.HistoricVariableInstance> varList =
                    historyService.createHistoricVariableInstanceQuery()
                        .processInstanceId(procInsId)
                        .variableName("nodeTimeMap")
                        .list();
                if (varList != null && !varList.isEmpty()) {
                    Object ntmObj = varList.get(0).getValue();
                    if (ntmObj instanceof Map) {
                        nodeTimeMap = (Map<String, Object>) ntmObj;
                    } else if (ntmObj != null) {
                        nodeTimeMap = JSON.parseObject(ntmObj.toString(), Map.class);
                    }
                }
            } catch (Exception e) {
                log.warn("flowRecord: 读取 nodeTimeMap 失败, procInsId={}", procInsId, e);
            }

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
                    flowTask.setTaskDefKey(histIns.getActivityId());
                    flowTask.setCreateTime(histIns.getStartTime());
                    flowTask.setFinishTime(histIns.getEndTime());
                    // 填入节点计划起止日期
                    if (nodeTimeMap != null) {
                        Object nodeTime = nodeTimeMap.get(histIns.getActivityId());
                        if (nodeTime instanceof Map) {
                            Map<?, ?> nt = (Map<?, ?>) nodeTime;
                            Object sd = nt.get("startDate");
                            Object ed = nt.get("endDate");
                            if (sd != null) flowTask.setPlanStartDate(sd.toString());
                            if (ed != null) flowTask.setPlanEndDate(ed.toString());
                        }
                    }
                    if (StringUtils.isNotBlank(histIns.getAssignee())) {
                        SysUser sysUser = sysUserService.selectUserById(Long.parseLong(histIns.getAssignee()));
                        flowTask.setAssigneeId(sysUser.getUserId());
                        flowTask.setAssigneeName(sysUser.getNickName());
                        flowTask.setDeptName(Objects.nonNull(sysUser.getDept()) ? sysUser.getDept().getDeptName() : "");
                    }
                    // 展示审批人员
                    List<HistoricIdentityLink> linksForTask = historyService.getHistoricIdentityLinksForTask(histIns.getTaskId());
                    StringBuilder stringBuilder = new StringBuilder();
                    // 获取该任务所属班组ID，用于精确查询候选人在该班组中的职位
                    Long taskTeamId = null;
                    try {
                        com.ruoyi.system.domain.TaskNodeExecution tne = taskNodeExecutionMapper.selectByTaskId(histIns.getTaskId());
                        if (tne != null) {
                            taskTeamId = tne.getAssignedTeamId();
                        }
                    } catch (Exception e) {
                        log.warn("查询节点执行记录获取班组ID失败，taskId={}", histIns.getTaskId(), e);
                    }
                    for (HistoricIdentityLink identityLink : linksForTask) {
                        // 获选人,候选组/角色(多个)
                        if ("candidate".equals(identityLink.getType())) {
                            if (StringUtils.isNotBlank(identityLink.getUserId())) {
                                SysUser sysUser = sysUserService.selectUserById(Long.parseLong(identityLink.getUserId()));
                                String position;
                                if (taskTeamId != null) {
                                    position = productionTeamMapper.selectPositionByUserIdAndTeamId(sysUser.getUserId(), taskTeamId);
                                } else {
                                    position = productionTeamMapper.selectPositionByUserId(sysUser.getUserId());
                                }
                                String positionLabel = StringUtils.isNotBlank(position) ? DictUtils.getDictLabel("team_position", position) : null;
                                if (StringUtils.isNotBlank(positionLabel)) {
                                    stringBuilder.append(sysUser.getNickName()).append("(").append(positionLabel).append(")").append(",");
                                } else {
                                    stringBuilder.append(sysUser.getNickName()).append(",");
                                }
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
        // 第一次申请获取初始化表单（vForm 可选；自定义组件流程无 formId，跳过即可）
        if (StringUtils.isNotBlank(deployId)) {
            SysForm sysForm = sysInstanceFormService.selectSysDeployFormByDeployId(deployId);
            if (Objects.nonNull(sysForm)) {
                map.put("formData", JSONObject.parseObject(sysForm.getFormContent()));
            }
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
            // 节点key → 自定义Vue组件名（从 extensionElements 读取）
            Map<String, String> nodeComponentMap = new HashMap<>();
            // 节点key → 节点名称（用于前端展示）
            Map<String, String> nodeNameMap = new HashMap<>();
            if (Objects.nonNull(hpi)) {
                BpmnModel bpmnModel = repositoryService.getBpmnModel(hpi.getProcessDefinitionId());
                if (Objects.nonNull(bpmnModel)) {
                    bpmnModel.getMainProcess().getFlowElements().forEach(el -> {
                        if (el instanceof UserTask) {
                            UserTask ut = (UserTask) el;
                            nodeNameMap.put(ut.getId(), ut.getName());
                            if (StringUtils.isNotBlank(ut.getFormKey())) {
                                nodeFormKeyMap.put(ut.getId(), ut.getFormKey());
                            }
                            // 读取 extensionElements 中的 formComponent
                            ExtensionElement compExt = FlowableUtils
                                    .getExtensionElementFromFlowElementByName(ut, "formComponent");
                            if (compExt != null) {
                                String compName = compExt.getAttributeValue(null, "value");
                                if (StringUtils.isNotBlank(compName)) {
                                    nodeComponentMap.put(ut.getId(), compName);
                                }
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
            // 自定义Vue组件节点列表：每个元素含 taskDefKey、taskName、formComponent、formData
            List<Map<String, Object>> componentNodesList = new ArrayList<>();

            // 去重：同一 taskDefinitionKey 只取第一次出现（避免退回后重复节点叠加）
            Set<String> seenTaskDefKeys = new LinkedHashSet<>();
            for (HistoricTaskInstance ht : allHistoricTasks) {
                if (!seenTaskDefKeys.add(ht.getTaskDefinitionKey())) {
                    continue;
                }
                // 从 BPMN 模型映射中取 formKey，而非 ht.getFormKey()（历史实例该字段为空）
                String htFormKey = nodeFormKeyMap.get(ht.getTaskDefinitionKey());
                if (StringUtils.isBlank(htFormKey)) {
                    // 没有 vForm 绑定，检查是否绑定了自定义 Vue 组件（优先 extensionElements，兜底硬编码映射）
                    String compName = nodeComponentMap.get(ht.getTaskDefinitionKey());
                    if (StringUtils.isBlank(compName)) {
                        compName = FALLBACK_COMPONENT_MAP.get(ht.getTaskDefinitionKey());
                    }
                    if (StringUtils.isNotBlank(compName)) {
                        String nsKey = ht.getTaskDefinitionKey() + "__formData";
                        Object nsData = parameters.get(nsKey);
                        Map<String, Object> compNode = new HashMap<>();
                        compNode.put("taskDefKey", ht.getTaskDefinitionKey());
                        compNode.put("taskName", nodeNameMap.getOrDefault(ht.getTaskDefinitionKey(), ht.getName()));
                        compNode.put("formComponent", compName);
                        compNode.put("formData", nsData != null ? JSONObject.parseObject(JSON.toJSONString(nsData)) : new JSONObject());
                        componentNodesList.add(compNode);
                    }
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
                    if (htFields != null) {
                        for (JSONObject field : htFields) {
                            JSONObject options = field.getJSONObject("options");
                            if (Objects.nonNull(options)) {
                                options.put("disabled", true);
                            }
                        }
                        mergedWidgetList.addAll(htFields);
                    }
                } catch (NumberFormatException e) {
                    log.warn("节点 {} 的 formKey 不是有效数字: {}", ht.getTaskDefinitionKey(), htFormKey);
                }
            }

            formJson.put("widgetList", mergedWidgetList);
            formJson.put("formConfig", Objects.nonNull(baseFormConfig) ? baseFormConfig : buildDefaultFormConfig());
            // 将自定义组件节点列表也放入 parameters，供前端渲染
            parameters.put("_componentNodes", componentNodesList);

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
                if (fields != null) {
                    for (JSONObject field : fields) {
                        JSONObject options = field.getJSONObject("options");
                        if (Objects.nonNull(options)) {
                            options.put("disabled", true);
                        }
                    }
                    formJson.put("widgetList", fields);
                }
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

        // 读取节点绑定的自定义 Vue 表单组件名（存储在 BPMN extensionElements 中）
        try {
            String procDefId4Comp = Objects.nonNull(task)
                    ? task.getProcessDefinitionId()
                    : (Objects.nonNull(historicTaskInstance) ? historicTaskInstance.getProcessDefinitionId() : null);
            if (StringUtils.isNotBlank(procDefId4Comp) && StringUtils.isNotBlank(taskDefKey)) {
                BpmnModel compBpmnModel = repositoryService.getBpmnModel(procDefId4Comp);
                if (Objects.nonNull(compBpmnModel)) {
                    FlowElement compElem = compBpmnModel.getFlowElement(taskDefKey);
                    if (compElem != null) {
                        ExtensionElement formCompElem =
                                FlowableUtils.getExtensionElementFromFlowElementByName(compElem, "formComponent");
                        if (formCompElem != null) {
                            parameters.put("_formComponent", formCompElem.getAttributeValue(null, "value"));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("读取节点 {} 自定义表单组件名失败", taskDefKey, e);
        }

        // ── 查询最新一条退回意见，仅当当前任务是由退回操作产生时才放入 parameters ──
        // 判断依据：退回评论时间与当前任务创建时间在 10 秒以内（同一事务中先 addComment 再 changeActivityState 创建新任务）
        if (!isFinished && Objects.nonNull(task) && StringUtils.isNotBlank(procInsId4Form)) {
            try {
                List<Comment> allComments = taskService.getProcessInstanceComments(procInsId4Form);
                Comment latestReback = null;
                for (Comment c : allComments) {
                    if (FlowComment.REBACK.getType().equals(c.getType())) {
                        if (latestReback == null || c.getTime().after(latestReback.getTime())) {
                            latestReback = c;
                        }
                    }
                }
                if (latestReback != null) {
                    // 退回评论时间应在当前任务创建时间之前（且间隔不超过 10 秒），
                    // 说明当前任务是由这次退回操作产生的
                    long commentTime = latestReback.getTime().getTime();
                    long taskCreateTime = task.getCreateTime().getTime();
                    long diffMs = taskCreateTime - commentTime;
                    if (diffMs >= 0 && diffMs <= 10000) {
                        Map<String, Object> rebackInfo = new HashMap<>();
                        rebackInfo.put("comment", latestReback.getFullMessage());
                        rebackInfo.put("time", latestReback.getTime());
                        // 获取退回操作人昵称
                        String rebackUserName = "";
                        if (StringUtils.isNotBlank(latestReback.getUserId())) {
                            SysUser rebackUser = sysUserService.selectUserById(Long.parseLong(latestReback.getUserId()));
                            rebackUserName = Objects.nonNull(rebackUser) ? rebackUser.getNickName() : latestReback.getUserId();
                        }
                        rebackInfo.put("userName", rebackUserName);
                        parameters.put("_latestReturnComment", rebackInfo);
                    }
                }
            } catch (Exception e) {
                log.warn("查询退回意见失败，procInsId: {}", procInsId4Form, e);
            }
        }

        log.info("获取任务表单 {}，formKey: {}，nodeKey: {}，已完成: {}", taskId,
                Objects.nonNull(task) ? task.getFormKey() : "(historic)", taskDefKey, isFinished);
        return AjaxResult.success(parameters);
    }

    /**
     * 按流程实例ID聚合所有已完成节点的表单数据（流程进行中也可实时查看已完成节点）
     * 与 flowTaskForm 的区别：直接用 procInsId，且只聚合 endTime != null 的已完成节点
     */
    @Override
    public AjaxResult flowTaskFormByProcInst(String procInsId) throws Exception {
        if (StringUtils.isBlank(procInsId)) {
            return AjaxResult.error("procInsId 不能为空");
        }

        HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(procInsId).singleResult();
        if (Objects.isNull(hpi)) {
            return AjaxResult.error("流程实例不存在");
        }

        // 加载整个流程实例的最终变量快照
        Map<String, Object> parameters = new HashMap<>();
        historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(procInsId)
                .list()
                .forEach(v -> parameters.put(v.getVariableName(), v.getValue()));

        // 从 BPMN 模型建立 taskDefinitionKey → formKey / formComponent / nodeName 映射
        Map<String, String> nodeFormKeyMap = new HashMap<>();
        Map<String, String> nodeComponentMap = new HashMap<>();
        Map<String, String> nodeNameMap = new HashMap<>();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(hpi.getProcessDefinitionId());
        if (Objects.nonNull(bpmnModel)) {
            bpmnModel.getMainProcess().getFlowElements().forEach(el -> {
                if (el instanceof UserTask) {
                    UserTask ut = (UserTask) el;
                    nodeNameMap.put(ut.getId(), ut.getName());
                    if (StringUtils.isNotBlank(ut.getFormKey())) {
                        nodeFormKeyMap.put(ut.getId(), ut.getFormKey());
                    }
                    ExtensionElement compExt = FlowableUtils
                            .getExtensionElementFromFlowElementByName(ut, "formComponent");
                    if (compExt != null) {
                        String compName = compExt.getAttributeValue(null, "value");
                        if (StringUtils.isNotBlank(compName)) {
                            nodeComponentMap.put(ut.getId(), compName);
                        }
                    }
                }
            });
        }

        // 查所有历史任务，只取已完成（endTime != null）的节点，按开始时间升序
        List<HistoricTaskInstance> finishedTasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(procInsId)
                .finished()
                .orderByHistoricTaskInstanceStartTime().asc()
                .list();

        List<JSONObject> mergedWidgetList = new ArrayList<>();
        JSONObject baseFormConfig = null;
        List<Map<String, Object>> componentNodesList = new ArrayList<>();

        // 去重：同一 taskDefinitionKey 只取第一次出现
        Set<String> seenTaskDefKeys = new LinkedHashSet<>();
        for (HistoricTaskInstance ht : finishedTasks) {
            if (!seenTaskDefKeys.add(ht.getTaskDefinitionKey())) {
                continue;
            }
            String htFormKey = nodeFormKeyMap.get(ht.getTaskDefinitionKey());
            if (StringUtils.isBlank(htFormKey)) {
                // 检查是否绑定了自定义 Vue 组件（优先 extensionElements，兜底硬编码映射）
                String compName = nodeComponentMap.get(ht.getTaskDefinitionKey());
                if (StringUtils.isBlank(compName)) {
                    compName = FALLBACK_COMPONENT_MAP.get(ht.getTaskDefinitionKey());
                }
                if (StringUtils.isNotBlank(compName)) {
                    String nsKey = ht.getTaskDefinitionKey() + "__formData";
                    Object nsData = parameters.get(nsKey);
                    Map<String, Object> compNode = new HashMap<>();
                    compNode.put("taskDefKey", ht.getTaskDefinitionKey());
                    compNode.put("taskName", nodeNameMap.getOrDefault(ht.getTaskDefinitionKey(), ht.getName()));
                    compNode.put("formComponent", compName);
                    compNode.put("formData", nsData != null ? JSONObject.parseObject(JSON.toJSONString(nsData)) : new JSONObject());
                    componentNodesList.add(compNode);
                }
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

                // 将该节点命名空间的字段值平铺到 parameters
                String nsKey = ht.getTaskDefinitionKey() + "__formData";
                Object nsData = parameters.get(nsKey);
                if (Objects.nonNull(nsData)) {
                    JSONObject savedData = JSONObject.parseObject(JSON.toJSONString(nsData));
                    savedData.forEach(parameters::put);
                }

                // 所有字段设为只读
                if (htFields != null) {
                    for (JSONObject field : htFields) {
                        JSONObject options = field.getJSONObject("options");
                        if (Objects.nonNull(options)) {
                            options.put("disabled", true);
                        }
                    }
                    mergedWidgetList.addAll(htFields);
                }
            } catch (NumberFormatException e) {
                log.warn("节点 {} 的 formKey 不是有效数字: {}", ht.getTaskDefinitionKey(), htFormKey);
            }
        }

        JSONObject formJson = new JSONObject();
        formJson.put("widgetList", mergedWidgetList);
        formJson.put("formConfig", Objects.nonNull(baseFormConfig) ? baseFormConfig : buildDefaultFormConfig());
        parameters.put("_componentNodes", componentNodesList);
        parameters.put("formJson", formJson);

        log.info("按流程实例聚合表单 procInsId={}，已完成节点数={}，自定义组件节点数={}",
                procInsId, seenTaskDefKeys.size(), componentNodesList.size());
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
        // 获取该任务所属班组ID，用于精确查询候选人在该班组中的职位
        Long taskTeamId2 = null;
        try {
            com.ruoyi.system.domain.TaskNodeExecution tne2 = taskNodeExecutionMapper.selectByTaskId(histIns.getTaskId());
            if (tne2 != null) {
                taskTeamId2 = tne2.getAssignedTeamId();
            }
        } catch (Exception e) {
            log.warn("查询节点执行记录获取班组ID失败，taskId={}", histIns.getTaskId(), e);
        }
        for (HistoricIdentityLink identityLink : linksForTask) {
            // 获选人,候选组/角色(多个)
            if ("candidate".equals(identityLink.getType())) {
                if (StringUtils.isNotBlank(identityLink.getUserId())) {
                    SysUser sysUser = sysUserService.selectUserById(Long.parseLong(identityLink.getUserId()));
                    String position;
                    if (taskTeamId2 != null) {
                        position = productionTeamMapper.selectPositionByUserIdAndTeamId(sysUser.getUserId(), taskTeamId2);
                    } else {
                        position = productionTeamMapper.selectPositionByUserId(sysUser.getUserId());
                    }
                    String positionLabel = StringUtils.isNotBlank(position) ? DictUtils.getDictLabel("team_position", position) : null;
                    if (StringUtils.isNotBlank(positionLabel)) {
                        stringBuilder.append(sysUser.getNickName()).append("(").append(positionLabel).append(")").append(",");
                    } else {
                        stringBuilder.append(sysUser.getNickName()).append(",");
                    }
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

    /**
     * 判断当前登录用户是否为指定任务所属班组的班组长
     *
     * @param taskId 任务ID
     * @return true=班组长，false=普通成员或无班组配置
     */
    @Override
    public boolean isLeaderOfTask(String taskId) {
        try {
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            if (task == null) {
                log.warn("isLeaderOfTask: 任务 {} 不存在", taskId);
                return false;
            }
            Map<String, Object> vars = taskService.getVariables(taskId);
            Long teamId = resolveTeamId(vars, task.getTaskDefinitionKey());
            if (teamId == null) {
                log.warn("isLeaderOfTask: 任务 {} 无班组配置，默认允许审批", taskId);
                // 无班组配置时，默认认为可以审批（不限制）
                return true;
            }
            com.ruoyi.manage.domain.ProductionTeam team = productionTeamMapper.selectProductionTeamById(teamId);
            if (team == null) {
                log.warn("isLeaderOfTask: 班组 {} 不存在", teamId);
                return true;
            }
            Long currentUserId = SecurityUtils.getLoginUser().getUser().getUserId();
            boolean isLeader = currentUserId.equals(team.getLeaderId());
            log.info("isLeaderOfTask: taskId={}, teamId={}, leaderId={}, currentUserId={}, isLeader={}",
                    taskId, teamId, team.getLeaderId(), currentUserId, isLeader);
            return isLeader;
        } catch (Exception e) {
            log.warn("isLeaderOfTask 发生异常，默认返回 true 允许操作", e);
            return true;
        }
    }

    /**
     * 仅保存表单数据到流程变量（不推进流程），供班组成员提交表单使用
     *
     * @param taskVo 包含 taskId、variables（含命名空间表单数据）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTaskFormData(FlowTaskVo taskVo) {
        Task task = taskService.createTaskQuery().taskId(taskVo.getTaskId()).singleResult();
        if (task == null) {
            throw new CustomException("任务不存在，taskId=" + taskVo.getTaskId());
        }
        if (taskVo.getVariables() != null && !taskVo.getVariables().isEmpty()) {
            taskService.setVariables(taskVo.getTaskId(), taskVo.getVariables());
            log.info("班组成员提交表单数据，taskId={}, variables keys={}", taskVo.getTaskId(),
                    taskVo.getVariables().keySet());
        }
        // 标记节点为"已提交待审批"
        flowTeamService.onTaskSubmitted(taskVo.getTaskId());
    }
}
