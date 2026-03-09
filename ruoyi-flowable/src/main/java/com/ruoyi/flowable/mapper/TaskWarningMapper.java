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
     * 将指定流程实例+节点的所有未读预警标为已读（任务完成时自动清除）
     *
     * @param procInstId 流程实例ID
     * @param nodeKey    节点key
     * @return 影响行数
     */
    int markReadByProcInstAndNodeKey(@Param("procInstId") String procInstId,
                                     @Param("nodeKey") String nodeKey);
}
