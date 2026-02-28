package com.ruoyi.flowable.service;

import com.ruoyi.flowable.domain.dto.DashboardStatsDto;

/**
 * 任务统计 Service 接口
 */
public interface IFlowStatService {

    /**
     * 获取当前用户的 Dashboard 统计数据
     *
     * @param userId 当前登录用户ID
     * @return DashboardStatsDto
     */
    DashboardStatsDto getDashboardStats(Long userId);
}
