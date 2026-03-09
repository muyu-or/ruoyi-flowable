package com.ruoyi.flowable.service;

import com.ruoyi.flowable.domain.TaskWarning;

import java.util.List;

/**
 * 任务节点超时预警服务接口
 *
 * @author xgh
 * @date 2026-03-09
 */
public interface ITaskWarningService
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
     * @param userId   用户ID
     * @param pageNum  页码（从1开始）
     * @param pageSize 每页条数
     * @return 预警消息列表
     */
    List<TaskWarning> selectByUserId(Long userId, int pageNum, int pageSize);

    /**
     * 查询用户未读预警数
     *
     * @param userId 用户ID
     * @return 未读数
     */
    int countUnreadByUserId(Long userId);

    /**
     * 将用户所有未读预警标为已读
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int markAllReadByUserId(Long userId);

    /**
     * 将单条预警标为已读
     *
     * @param id 预警主键
     * @return 影响行数
     */
    int markReadById(Long id);

    /**
     * 扫描即将到期和已超期的节点，推送预警消息
     */
    void scanWarnings();

    /**
     * 将指定流程实例+节点的所有未读预警标为已读（任务完成时自动清除）
     *
     * @param procInstId 流程实例ID
     * @param nodeKey    节点key
     * @return 影响行数
     */
    int markReadByProcInstAndNodeKey(String procInstId, String nodeKey);
}
