package com.ruoyi.flowable.service;

import com.ruoyi.flowable.domain.dto.HomeStatDto;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * 首页统计 Service 接口
 *
 * @author claude
 * @date 2026-03-04
 */
public interface IHomeStatService {
    /**
     * 获取首页统计数据
     */
    HomeStatDto getHomeStats(Long userId, String period);
}