package com.ruoyi.system.service.impl;

import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.TaskNodeExecutionMapper;
import com.ruoyi.system.domain.TaskNodeExecution;
import com.ruoyi.system.service.ITaskNodeExecutionService;
import java.util.List;

/**
 * 节点级任务执行记录服务实现
 *
 * @author xgh
 * @date 2026-02-28
 */
@Service
public class TaskNodeExecutionServiceImpl implements ITaskNodeExecutionService
{
    @Autowired
    private TaskNodeExecutionMapper taskNodeExecutionMapper;

    /**
     * 查询节点任务执行记录
     *
     * @param id 主键
     * @return 节点任务执行记录
     */
    @Override
    public TaskNodeExecution selectTaskNodeExecutionById(Long id)
    {
        return taskNodeExecutionMapper.selectTaskNodeExecutionById(id);
    }

    /**
     * 查询节点任务执行记录列表
     *
     * @param taskNodeExecution 节点任务执行记录
     * @return 节点任务执行记录集合
     */
    @Override
    public List<TaskNodeExecution> selectTaskNodeExecutionList(TaskNodeExecution taskNodeExecution)
    {
        return taskNodeExecutionMapper.selectTaskNodeExecutionList(taskNodeExecution);
    }

    /**
     * 新增节点任务执行记录
     *
     * @param taskNodeExecution 节点任务执行记录
     * @return 结果
     */
    @Override
    public int insertTaskNodeExecution(TaskNodeExecution taskNodeExecution)
    {
        taskNodeExecution.setCreateTime(DateUtils.getNowDate());
        return taskNodeExecutionMapper.insertTaskNodeExecution(taskNodeExecution);
    }

    /**
     * 修改节点任务执行记录
     *
     * @param taskNodeExecution 节点任务执行记录
     * @return 结果
     */
    @Override
    public int updateTaskNodeExecution(TaskNodeExecution taskNodeExecution)
    {
        taskNodeExecution.setUpdateTime(DateUtils.getNowDate());
        return taskNodeExecutionMapper.updateTaskNodeExecution(taskNodeExecution);
    }

    /**
     * 删除节点任务执行记录
     *
     * @param id 主键
     * @return 结果
     */
    @Override
    public int deleteTaskNodeExecutionById(Long id)
    {
        return taskNodeExecutionMapper.deleteTaskNodeExecutionById(id);
    }

    /**
     * 批量删除节点任务执行记录
     *
     * @param ids 需要删除的主键集合
     * @return 结果
     */
    @Override
    public int deleteTaskNodeExecutionByIds(Long[] ids)
    {
        return taskNodeExecutionMapper.deleteTaskNodeExecutionByIds(ids);
    }

    /**
     * 根据流程执行记录ID查询所有节点任务
     *
     * @param execRecordId 流程执行记录ID
     * @return 节点任务集合
     */
    @Override
    public List<TaskNodeExecution> selectByExecRecordId(Long execRecordId)
    {
        return taskNodeExecutionMapper.selectByExecRecordId(execRecordId);
    }

    /**
     * 根据任务ID查询节点任务
     *
     * @param taskId 任务ID
     * @return 节点任务执行记录
     */
    @Override
    public TaskNodeExecution selectByTaskId(String taskId)
    {
        return taskNodeExecutionMapper.selectByTaskId(taskId);
    }

    @Override
    public void cancelByExecRecordId(Long execRecordId)
    {
        taskNodeExecutionMapper.cancelByExecRecordId(execRecordId);
    }
}
