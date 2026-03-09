package com.ruoyi.system.service.impl;

import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.TaskExecutionRecordMapper;
import com.ruoyi.system.domain.TaskExecutionRecord;
import com.ruoyi.system.service.ITaskExecutionRecordService;
import java.util.List;

/**
 * 流程执行记录服务实现
 *
 * @author xgh
 * @date 2026-02-28
 */
@Service
public class TaskExecutionRecordServiceImpl implements ITaskExecutionRecordService
{
    @Autowired
    private TaskExecutionRecordMapper taskExecutionRecordMapper;

    /**
     * 查询流程执行记录
     *
     * @param id 主键
     * @return 流程执行记录
     */
    @Override
    public TaskExecutionRecord selectTaskExecutionRecordById(Long id)
    {
        return taskExecutionRecordMapper.selectTaskExecutionRecordById(id);
    }

    /**
     * 查询流程执行记录列表
     *
     * @param taskExecutionRecord 流程执行记录
     * @return 流程执行记录集合
     */
    @Override
    public List<TaskExecutionRecord> selectTaskExecutionRecordList(TaskExecutionRecord taskExecutionRecord)
    {
        return taskExecutionRecordMapper.selectTaskExecutionRecordList(taskExecutionRecord);
    }

    /**
     * 新增流程执行记录
     *
     * @param taskExecutionRecord 流程执行记录
     * @return 结果
     */
    @Override
    public int insertTaskExecutionRecord(TaskExecutionRecord taskExecutionRecord)
    {
        taskExecutionRecord.setCreateTime(DateUtils.getNowDate());
        return taskExecutionRecordMapper.insertTaskExecutionRecord(taskExecutionRecord);
    }

    /**
     * 修改流程执行记录
     *
     * @param taskExecutionRecord 流程执行记录
     * @return 结果
     */
    @Override
    public int updateTaskExecutionRecord(TaskExecutionRecord taskExecutionRecord)
    {
        taskExecutionRecord.setUpdateTime(DateUtils.getNowDate());
        return taskExecutionRecordMapper.updateTaskExecutionRecord(taskExecutionRecord);
    }

    /**
     * 删除流程执行记录
     *
     * @param id 主键
     * @return 结果
     */
    @Override
    public int deleteTaskExecutionRecordById(Long id)
    {
        return taskExecutionRecordMapper.deleteTaskExecutionRecordById(id);
    }

    /**
     * 批量删除流程执行记录
     *
     * @param ids 需要删除的主键集合
     * @return 结果
     */
    @Override
    public int deleteTaskExecutionRecordByIds(Long[] ids)
    {
        return taskExecutionRecordMapper.deleteTaskExecutionRecordByIds(ids);
    }

    /**
     * 根据流程实例ID查询执行记录
     *
     * @param procInstId 流程实例ID
     * @return 流程执行记录
     */
    @Override
    public TaskExecutionRecord selectByProcInstId(String procInstId)
    {
        return taskExecutionRecordMapper.selectByProcInstId(procInstId);
    }
}
