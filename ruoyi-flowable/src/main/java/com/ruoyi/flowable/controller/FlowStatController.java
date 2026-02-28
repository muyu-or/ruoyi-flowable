package com.ruoyi.flowable.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.flowable.domain.dto.DashboardStatsDto;
import com.ruoyi.flowable.service.IFlowStatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 任务统计 Dashboard Controller
 */
@Api(tags = "任务统计看板")
@RestController
@RequestMapping("/flowable/stat")
public class FlowStatController extends BaseController {

    @Autowired
    private IFlowStatService flowStatService;

    /**
     * 获取当前用户的 Dashboard 统计数据
     * GET /flowable/stat/dashboard
     */
    @ApiOperation("获取任务统计看板数据")
    @PreAuthorize("@ss.hasPermi('flowable:stat:dashboard')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard() {
        Long userId = SecurityUtils.getUserId();
        DashboardStatsDto stats = flowStatService.getDashboardStats(userId);
        return AjaxResult.success(stats);
    }
}
