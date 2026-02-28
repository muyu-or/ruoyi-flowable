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
}
