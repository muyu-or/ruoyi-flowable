package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.TaskExecutionRecord;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * 流程执行记录Mapper接口
 *
 * @author xgh
 * @date 2026-02-28
 */
@Mapper
public interface TaskExecutionRecordMapper
{
    /**
     * 查询流程执行记录
     *
     * @param id 主键
     * @return 流程执行记录
     */
    public TaskExecutionRecord selectTaskExecutionRecordById(Long id);

    /**
     * 查询流程执行记录列表
     *
     * @param taskExecutionRecord 流程执行记录
     * @return 流程执行记录集合
     */
    public List<TaskExecutionRecord> selectTaskExecutionRecordList(TaskExecutionRecord taskExecutionRecord);

    /**
     * 新增流程执行记录
     *
     * @param taskExecutionRecord 流程执行记录
     * @return 结果
     */
    public int insertTaskExecutionRecord(TaskExecutionRecord taskExecutionRecord);

    /**
     * 修改流程执行记录
     *
     * @param taskExecutionRecord 流程执行记录
     * @return 结果
     */
    public int updateTaskExecutionRecord(TaskExecutionRecord taskExecutionRecord);

    /**
     * 删除流程执行记录
     *
     * @param id 主键
     * @return 结果
     */
    public int deleteTaskExecutionRecordById(Long id);

    /**
     * 批量删除流程执行记录
     *
     * @param ids 需要删除的主键集合
     * @return 结果
     */
    public int deleteTaskExecutionRecordByIds(Long[] ids);

    /**
     * 根据流程实例ID查询执行记录
     *
     * @param procInstId 流程实例ID
     * @return 流程执行记录
     */
    public TaskExecutionRecord selectByProcInstId(String procInstId);
}
