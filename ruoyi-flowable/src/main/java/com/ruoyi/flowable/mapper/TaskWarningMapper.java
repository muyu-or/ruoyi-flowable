package com.ruoyi.flowable.mapper;

import com.ruoyi.flowable.domain.TaskWarning;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 任务节点超时预警消息Mapper接口
 *
 * @author xgh
 * @date 2026-03-09
 */
@Mapper
public interface TaskWarningMapper
{
    /**
     * 新增预警消息
     *
     * @param warning 预警消息
     * @return 结果
     */
    int insertTaskWarning(TaskWarning warning);

    /**
     * 分页查询用户的预警消息列表
     *
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit  每页条数
     * @return 预警消息列表
     */
    List<TaskWarning> selectByUserId(@Param("userId") Long userId,
                                     @Param("offset") int offset,
                                     @Param("limit") int limit);

    /**
     * 查询用户未读预警数
     *
     * @param userId 用户ID
     * @return 未读数
     */
    int countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 将用户所有未读预警标为已读
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int markAllReadByUserId(@Param("userId") Long userId);

    /**
     * 将单条预警标为已读
     *
     * @param id 预警主键
     * @return 影响行数
     */
    int markReadById(@Param("id") Long id);

    /**
     * 查询是否已存在同类型预警（去重）
     *
     * @param userId     用户ID
     * @param procInstId 流程实例ID
     * @param nodeKey    节点key
     * @param warnType   预警类型
     * @return 数量
     */
    int countByUserNodeType(@Param("userId") Long userId,
                            @Param("procInstId") String procInstId,
                            @Param("nodeKey") String nodeKey,
                            @Param("warnType") String warnType);

    /**
     * 将指定流程实例+节点的所有预警标为已读+已处理（任务完成时自动调用）
     *
     * @param procInstId 流程实例ID
     * @param nodeKey    节点key
     * @return 影响行数
     */
    int markReadByProcInstAndNodeKey(@Param("procInstId") String procInstId,
                                     @Param("nodeKey") String nodeKey);

    /**
     * Admin专用：按 procInstId+nodeKey+warnType 去重，分页查询预警（每个节点只返回一条）
     *
     * @param offset 偏移量
     * @param limit  每页条数
     * @return 预警消息列表
     */
    List<TaskWarning> selectAllDistinct(@Param("offset") int offset,
                                        @Param("limit") int limit);

    /**
     * Admin专用：去重后的未处理预警数
     *
     * @return 未处理数
     */
    int countAllUnresolved();

    /**
     * Admin专用：去重后的未读预警数（badge用）
     *
     * @return 未读数
     */
    int countAllDistinctUnread();

    /**
     * Admin专用：将全部预警标为已读
     *
     * @return 影响行数
     */
    int markAllRead();

    /**
     * Admin专用：将全部预警标为管理员已读（不影响普通用户的 is_read）
     *
     * @return 影响行数
     */
    int markAllAdminRead();

    /**
     * 批量删除已处理的预警
     *
     * @param ids 预警ID列表
     * @return 影响行数
     */
    int deleteResolvedByIds(@Param("ids") List<Long> ids);

    /**
     * 删除全部已处理的预警
     *
     * @return 影响行数
     */
    int deleteAllResolved();

    /**
     * 删除指定用户的已处理预警
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteResolvedByUserId(@Param("userId") Long userId);

    /**
     * 将指定节点的 deadline_soon 预警升级为 overdue
     *
     * @param procInstId 流程实例ID
     * @param nodeKey    节点key
     * @return 影响行数
     */
    int upgradeToOverdue(@Param("procInstId") String procInstId,
                         @Param("nodeKey") String nodeKey);

    /**
     * BI大屏：按节点统计预警次数和平均响应时间（分钟）
     *
     * @return 每行含 nodeName, warningCount, avgResponseMinutes
     */
    List<java.util.Map<String, Object>> selectWarningStatsByNode();
}
