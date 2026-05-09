package com.ruoyi.web.controller.system;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.SysTableColWidth;
import com.ruoyi.system.service.ISysTableColWidthService;

/**
 * 表格列宽全局配置Controller
 *
 * admin 统一配置表格列宽，所有角色同步使用
 */
@RestController
@RequestMapping("/system/tableColWidth")
public class SysTableColWidthController extends BaseController
{
    @Autowired
    private ISysTableColWidthService tableColWidthService;

    /**
     * 获取指定表格的列宽配置（所有用户可读）
     * 使用查询参数避免路径参数中 / 的截断问题
     */
    @GetMapping("/get")
    public AjaxResult getConfig(@RequestParam String routePath, @RequestParam String tableId)
    {
        SysTableColWidth config = tableColWidthService.selectConfigByRoute(routePath, tableId);
        return success(config);
    }

    /**
     * 获取指定路由下所有表格的列宽配置（所有用户可读）
     */
    @GetMapping("/getByRoute")
    public AjaxResult getConfigsByRoute(@RequestParam String routePath)
    {
        List<SysTableColWidth> configs = tableColWidthService.selectConfigsByRoutePath(routePath);
        return success(configs);
    }

    /**
     * 获取所有列宽配置（admin 可查看全部）
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/list")
    public AjaxResult list()
    {
        List<SysTableColWidth> configs = tableColWidthService.selectConfigList();
        return success(configs);
    }

    /**
     * 保存列宽配置（仅 admin）
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "表格列宽配置", businessType = BusinessType.UPDATE)
    @PostMapping
    public AjaxResult save(@RequestBody SysTableColWidth config)
    {
        config.setUpdateBy(getUsername());
        return toAjax(tableColWidthService.saveOrUpdateConfig(config));
    }

    /**
     * 重置指定表格的列宽配置（仅 admin）
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "表格列宽配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/reset")
    public AjaxResult reset(@RequestParam String routePath, @RequestParam String tableId)
    {
        return toAjax(tableColWidthService.deleteConfig(routePath, tableId));
    }

    /**
     * 重置指定路由下所有表格的列宽配置（仅 admin）
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "表格列宽配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/resetByRoute")
    public AjaxResult resetByRoute(@RequestParam String routePath)
    {
        return toAjax(tableColWidthService.deleteConfigsByRoutePath(routePath));
    }

    /**
     * 重置所有表格列宽配置（仅 admin）
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "表格列宽配置", businessType = BusinessType.CLEAN)
    @DeleteMapping("/all")
    public AjaxResult resetAll()
    {
        return toAjax(tableColWidthService.deleteAllConfig());
    }
}