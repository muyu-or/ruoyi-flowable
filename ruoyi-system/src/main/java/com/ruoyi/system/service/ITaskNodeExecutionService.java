package com.ruoyi.system.service;

import com.ruoyi.system.domain.TaskNodeExecution;
import java.util.List;

/**
 * 节点级任务执行记录服务接口
 *
 * @author xgh
 * @date 2026-02-28
 */
public interface ITaskNodeExecutionService
{
    /**
     * 查询节点任务执行记录
     *
     * @param id 主键
     * @return 节点任务执行记录
     */
    public TaskNodeExecution selectTaskNodeExecutionById(Long id);

    /**
     * 查询节点任务执行记录列表
     *
     * @param taskNodeExecution 节点任务执行记录
     * @return 节点任务执行记录集合
     */
    public List<TaskNodeExecution> selectTaskNodeExecutionList(TaskNodeExecution taskNodeExecution);

    /**
     * 新增节点任务执行记录
     *
     * @param taskNodeExecution 节点任务执行记录
     * @return 结果
     */
    public int insertTaskNodeExecution(TaskNodeExecution taskNodeExecution);

    /**
     * 修改节点任务执行记录
     *
     * @param taskNodeExecution 节点任务执行记录
     * @return 结果
     */
    public int updateTaskNodeExecution(TaskNodeExecution taskNodeExecution);

    /**
     * 删除节点任务执行记录
     *
     * @param id 主键
     * @return 结果
     */
    public int deleteTaskNodeExecutionById(Long id);

    /**
     * 批量删除节点任务执行记录
     *
     * @param ids 需要删除的主键集合
     * @return 结果
     */
    public int deleteTaskNodeExecutionByIds(Long[] ids);

    /**
     * 根据流程执行记录ID查询所有节点任务
     *
     * @param execRecordId 流程执行记录ID
     * @return 节点任务集合
     */
    public List<TaskNodeExecution> selectByExecRecordId(Long execRecordId);

    /**
     * 根据任务ID查询节点任务
     *
     * @param taskId 任务ID
     * @return 节点任务执行记录
     */
    public TaskNodeExecution selectByTaskId(String taskId);

    /**
     * 将某流程执行记录下所有未完成的节点任务标记为已取消
     *
     * @param execRecordId 流程执行记录ID
     */
    void cancelByExecRecordId(Long execRecordId);
}
