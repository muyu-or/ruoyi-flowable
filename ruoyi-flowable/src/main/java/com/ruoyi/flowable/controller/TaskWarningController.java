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
     * 判断用户是否为管理角色（超级管理员 或 拥有"查看全部数据"权限）
     */
    private boolean isManagerRole(Long userId) {
        return SecurityUtils.isAdmin(userId)
                || SecurityUtils.hasPermi("flowable:stat:all");
    }

    /**
     * 预警列表（分页）
     * admin：去重查询（每个节点只返回一条）
     * 普通用户：查询自己的预警
     *
     * 返回字段：
     *   rows: 预警列表
     *   unreadCount: 未读数（badge用，全部已读可清零）
     *   unresolvedCount: 未处理数（仅admin，展示用）
     */
    @ApiOperation(value = "预警列表")
    @GetMapping("/list")
    public AjaxResult list(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                           @RequestParam(value = "pageSize", defaultValue = "10") int pageSize)
    {
        Long userId = SecurityUtils.getUserId();
        boolean isManager = isManagerRole(userId);
        Map<String, Object> data = new HashMap<>();
        if (isManager)
        {
            List<TaskWarning> list = taskWarningService.selectAllDistinct(pageNum, pageSize);
            data.put("rows", list);
            data.put("unreadCount", taskWarningService.countAllDistinctUnread());
            data.put("unresolvedCount", taskWarningService.countAllUnresolved());
        }
        else
        {
            List<TaskWarning> list = taskWarningService.selectByUserId(userId, pageNum, pageSize);
            data.put("rows", list);
            data.put("unreadCount", taskWarningService.countUnreadByUserId(userId));
        }
        return AjaxResult.success(data);
    }

    /**
     * 未读预警数（badge 数字）
     * 管理角色：去重后的未读数
     * 普通用户：未读数
     */
    @ApiOperation(value = "未读预警数")
    @GetMapping("/unread")
    public AjaxResult unreadCount()
    {
        Long userId = SecurityUtils.getUserId();
        boolean isManager = isManagerRole(userId);
        int count;
        if (isManager)
        {
            count = taskWarningService.countAllDistinctUnread();
        }
        else
        {
            count = taskWarningService.countUnreadByUserId(userId);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        return AjaxResult.success(data);
    }

    /**
     * 全部标为已读
     * 管理角色：标记全部预警已读
     * 普通用户：只标记自己的
     */
    @ApiOperation(value = "全部标为已读")
    @PutMapping("/readAll")
    public AjaxResult readAll()
    {
        Long userId = SecurityUtils.getUserId();
        boolean isManager = isManagerRole(userId);
        if (isManager)
        {
            // 管理角色：只标记 admin_read，不影响普通用户的 is_read
            taskWarningService.markAllAdminRead();
        }
        else
        {
            // 普通用户：只标记自己的预警为已读
            taskWarningService.markAllReadByUserId(userId);
        }
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

    /**
     * 清空已处理的预警
     * 管理角色：删除全部已处理预警
     * 普通用户：只删除自己的已处理预警
     */
    @ApiOperation(value = "清空已处理预警")
    @DeleteMapping("/clearResolved")
    public AjaxResult clearResolved()
    {
        Long userId = SecurityUtils.getUserId();
        boolean isManager = isManagerRole(userId);
        int count = taskWarningService.clearResolved(userId, isManager);
        return AjaxResult.success("已删除 " + count + " 条已处理预警");
    }
}
