package com.ruoyi.flowable.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.flowable.domain.TaskWarning;
import com.ruoyi.flowable.service.ITaskWarningService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务节点超时预警Controller
 *
 * @author xgh
 * @date 2026-03-09
 */
@Slf4j
@Api(tags = "任务超时预警")
@RestController
@RequestMapping("/flowable/warning")
public class TaskWarningController extends BaseController
{
    @Resource
    private ITaskWarningService taskWarningService;

    /**
     * 当前用户预警列表（分页）
     */
    @ApiOperation(value = "当前用户预警列表")
    @GetMapping("/list")
    public AjaxResult list(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                           @RequestParam(value = "pageSize", defaultValue = "10") int pageSize)
    {
        Long userId = SecurityUtils.getUserId();
        List<TaskWarning> list = taskWarningService.selectByUserId(userId, pageNum, pageSize);
        int unreadCount = taskWarningService.countUnreadByUserId(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("rows", list);
        data.put("unreadCount", unreadCount);
        return AjaxResult.success(data);
    }

    /**
     * 当前用户未读预警数
     */
    @ApiOperation(value = "当前用户未读预警数")
    @GetMapping("/unread")
    public AjaxResult unreadCount()
    {
        Long userId = SecurityUtils.getUserId();
        int count = taskWarningService.countUnreadByUserId(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        return AjaxResult.success(data);
    }

    /**
     * 全部标为已读
     */
    @ApiOperation(value = "全部标为已读")
    @PutMapping("/readAll")
    public AjaxResult readAll()
    {
        Long userId = SecurityUtils.getUserId();
        taskWarningService.markAllReadByUserId(userId);
        return AjaxResult.success();
    }

    /**
     * 单条标为已读
     */
    @ApiOperation(value = "单条标为已读")
    @PutMapping("/read/{id}")
    public AjaxResult readOne(@PathVariable("id") Long id)
    {
        taskWarningService.markReadById(id);
        return AjaxResult.success();
    }

    /**
     * 手动触发预警扫描（测试用，也可在紧急情况下手动执行）
     */
    @ApiOperation(value = "手动触发预警扫描")
    @PostMapping("/scan")
    public AjaxResult scan()
    {
        taskWarningService.scanWarnings();
        return AjaxResult.success("扫描完成");
    }
}
