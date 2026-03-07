package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.TaskNodeExecution;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * 节点级任务执行记录Mapper接口
 *
 * @author xgh
 * @date 2026-02-28
 */
@Mapper
public interface TaskNodeExecutionMapper
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
     * 按状态统计个人任务数量（认领人或被分配人）
     *
     * @param userId 用户ID
     * @return 状态→数量的 Map 列表，每行含 status 和 cnt 字段
     */
    List<java.util.Map<String, Object>> countMyStatsByStatus(@org.apache.ibatis.annotations.Param("userId") Long userId);

    /**
     * 按状态统计班组所有成员的任务数量
     *
     * @param teamId 班组ID
     * @return 状态→数量的 Map 列表，每行含 status 和 cnt 字段
     */
    List<java.util.Map<String, Object>> countTeamStatsByStatus(@org.apache.ibatis.annotations.Param("teamId") Long teamId);

    /**
     * 将某流程执行记录下所有未完成的节点任务标记为已取消
     *
     * @param execRecordId 流程执行记录ID
     */
    void cancelByExecRecordId(@org.apache.ibatis.annotations.Param("execRecordId") Long execRecordId);

    /**
     * 按班组统计成员任务状态
     */
    List<java.util.Map<String, Object>> countMemberStatsByTeam(@org.apache.ibatis.annotations.Param("teamId") Long teamId);

    /**
     * 查询最近完成的节点任务（用于首页展示）
     *
     * @param userId 用户ID（普通成员传值，班组长传 null）
     * @param teamId 班组ID（班组长传值，普通成员传 null）
     * @param limit  最大返回条数
     * @return 最近完成任务列表
     */
    List<java.util.Map<String, Object>> selectRecentCompleted(
        @org.apache.ibatis.annotations.Param("userId") Long userId,
        @org.apache.ibatis.annotations.Param("teamId") Long teamId,
        @org.apache.ibatis.annotations.Param("limit") int limit
    );
}
