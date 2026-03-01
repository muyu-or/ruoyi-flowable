package com.ruoyi.flowable.service;

/**
 * 流程班组任务分配服务接口
 *
 * @author xgh
 * @date 2026-02-28
 */
public interface IFlowTeamService {

    /**
     * 任务创建时：注入班组成员为候选人 + 写 task_node_execution
     *
     * @param taskId 任务ID
     * @param procInstId 流程实例ID
     * @param nodeKey 节点key（BPMN定义）
     * @param nodeName 节点名称
     */
    void injectTeamCandidates(String taskId, String procInstId, String nodeKey, String nodeName);

    /**
     * 任务认领时：更新 task_node_execution 状态
     *
     * @param taskId 任务ID
     * @param claimUserId 认领用户ID
     */
    void onTaskClaimed(String taskId, Long claimUserId);

    /**
     * 任务完成时：更新 task_node_execution 状态
     *
     * @param taskId 任务ID
     */
    void onTaskCompleted(String taskId);

    /**
     * 任务完成时：更新 task_node_execution 状态（新版本，支持结果和意见）
     *
     * @param taskId 任务ID
     * @param result 任务结果（completed/rejected/abandoned）
     * @param comment 审批意见
     */
    void onTaskCompleted(String taskId, String result, String comment);

    /**
     * 任务完成时：更新 task_node_execution 状态（含执行人，确保 claim_user_id 有值）
     *
     * @param taskId    任务ID
     * @param result    任务结果（completed/rejected）
     * @param comment   审批意见
     * @param userId    实际执行该任务的用户ID
     */
    void onTaskCompleted(String taskId, String result, String comment, Long userId);
}
