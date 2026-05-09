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

    /**
     * 更新超时标记
     *
     * @param id          主键
     * @param timeoutFlag 超时标记: 0=正常,1=已超时
     * @return 结果
     */
    int updateTimeoutFlag(@org.apache.ibatis.annotations.Param("id") Long id,
                          @org.apache.ibatis.annotations.Param("timeoutFlag") int timeoutFlag);

    /**
     * 统计全公司所有成员的任务数量（不区分用户，排除已取消）
     *
     * @return 状态→数量的 Map 列表，每行含 status 和 cnt 字段
     */
    List<java.util.Map<String, Object>> countAllStats();

    /**
     * 全公司个人完成数量 Top N（按 claim_user_id 分组，排除已取消）
     *
     * @param limit 最大返回条数
     * @return 每行含 userId, userName, finished 字段
     */
    List<java.util.Map<String, Object>> countUserFinishedTop(@org.apache.ibatis.annotations.Param("limit") int limit);

    /**
     * 查询日历看板事件（按月份范围 + 角色过滤）
     *
     * @param monthStart     月份起始日期 yyyy-MM-dd
     * @param monthEnd       月份结束日期 yyyy-MM-dd
     * @param userId         当前用户ID
     * @param leaderTeamIds  用户作为组长的班组ID列表（看全部数据）
     * @param memberTeamIds  用户作为成员的班组ID列表（只看自己的数据）
     * @param filterByUser   是否按用户/班组过滤（Admin=false）
     * @return 日历事件列表
     */
    List<java.util.Map<String, Object>> selectCalendarEvents(
        @org.apache.ibatis.annotations.Param("monthStart") String monthStart,
        @org.apache.ibatis.annotations.Param("monthEnd") String monthEnd,
        @org.apache.ibatis.annotations.Param("userId") Long userId,
        @org.apache.ibatis.annotations.Param("leaderTeamIds") java.util.List<Long> leaderTeamIds,
        @org.apache.ibatis.annotations.Param("memberTeamIds") java.util.List<Long> memberTeamIds,
        @org.apache.ibatis.annotations.Param("filterByUser") boolean filterByUser
    );

    /**
     * 查询某用户参与过的已完成任务的 task_id 列表（status 为 submitted 或 completed）
     *
     * @param claimUserId 认领/提交人用户ID
     * @return task_id 列表
     */
    List<String> selectFinishedTaskIdsByClaimUserId(@org.apache.ibatis.annotations.Param("claimUserId") Long claimUserId);

    /**
     * 预警扫描：查所有未完成且截止日期 <= deadlineDate 的节点
     *
     * @param deadlineDate 截止日期阈值（tomorrow），plan_end_date <= 此日期的都需预警
     * @return 节点信息列表
     */
    List<java.util.Map<String, Object>> selectPendingNodesForWarning(
        @org.apache.ibatis.annotations.Param("deadlineDate") String deadlineDate
    );

    /**
     * BI大屏：查询已完成任务的原始耗时数据（按节点分组，用于 Java 计算 P50/P90）
     *
     * @return 每行含 nodeName, duration (秒)
     */
    List<java.util.Map<String, Object>> selectCompletedDurationsByNode();

    /**
     * BI大屏：查询已完成任务的原始耗时数据（按班组分组，用于 Java 计算稳定性）
     *
     * @return 每行含 teamName, duration (秒)
     */
    List<java.util.Map<String, Object>> selectCompletedDurationsByTeam();

    /**
     * BI大屏：按班组统计准时完成率
     *
     * @return 每行含 teamName, completedCount, onTimeCount
     */
    List<java.util.Map<String, Object>> selectTeamOnTimeStats();

    /**
     * BI大屏：按节点统计活跃/已完成任务数量
     *
     * @return 每行含 nodeName, activeCount, completedCount
     */
    List<java.util.Map<String, Object>> selectNodeStatusSummary();

    /**
     * BI大屏：按班组统计当前超时未解决的任务数（活跃状态 + 未解决逾期预警）
     *
     * @return 每行含 teamName, unresolvedCount
     */
    List<java.util.Map<String, Object>> selectTeamUnresolvedOverdue();
}
