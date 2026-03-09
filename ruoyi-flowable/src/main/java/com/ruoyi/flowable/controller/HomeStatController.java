package com.ruoyi.flowable.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.flowable.domain.dto.HomeStatDto;
import com.ruoyi.flowable.service.IHomeStatService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 首页统计Controller
 *
 * @author claude
 * @date 2026-03-04
 */
@RestController
@RequestMapping("/flowable/stat")
public class HomeStatController extends BaseController {

    @Resource
    private IHomeStatService homeStatService;

    @GetMapping("/home")
    @PreAuthorize("@ss.hasPermi('flowable:stat:dashboard')")
    @Log(title = "首页统计", businessType = BusinessType.OTHER)
    public AjaxResult getHomeStats(@RequestParam(defaultValue = "month") String period) {
        Long userId = SecurityUtils.getUserId();
        HomeStatDto stats = homeStatService.getHomeStats(userId, period);
        return AjaxResult.success(stats);
    }
}