package com.ruoyi.manage.controller;

import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.manage.domain.MonitorDevice;
import com.ruoyi.manage.service.IMonitorDeviceService;

/**
 * 监控设备Controller
 *
 * @author xgh
 * @date 2026-03-07
 */
@RestController
@RequestMapping("/monitor/device")
public class MonitorDeviceController extends BaseController
{
    @Resource
    private IMonitorDeviceService monitorDeviceService;

    /**
     * 查询监控设备列表
     */
    @PreAuthorize("@ss.hasPermi('monitor:device:list')")
    @GetMapping("/list")
    public TableDataInfo list(MonitorDevice monitorDevice)
    {
        startPage();
        List<MonitorDevice> list = monitorDeviceService.selectMonitorDeviceList(monitorDevice);
        return getDataTable(list);
    }

    /**
     * 导出监控设备列表
     */
    @PreAuthorize("@ss.hasPermi('monitor:device:export')")
    @Log(title = "监控设备", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MonitorDevice monitorDevice)
    {
        List<MonitorDevice> list = monitorDeviceService.selectMonitorDeviceList(monitorDevice);
        ExcelUtil<MonitorDevice> util = new ExcelUtil<MonitorDevice>(MonitorDevice.class);
        util.exportExcel(response, list, "监控设备数据");
    }

    /**
     * 获取监控设备详细信息
     */
    @PreAuthorize("@ss.hasPermi('monitor:device:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(monitorDeviceService.selectMonitorDeviceById(id));
    }

    /**
     * 新增监控设备
     */
    @PreAuthorize("@ss.hasPermi('monitor:device:add')")
    @Log(title = "监控设备", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MonitorDevice monitorDevice)
    {
        return toAjax(monitorDeviceService.insertMonitorDevice(monitorDevice));
    }

    /**
     * 修改监控设备
     */
    @PreAuthorize("@ss.hasPermi('monitor:device:edit')")
    @Log(title = "监控设备", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MonitorDevice monitorDevice)
    {
        return toAjax(monitorDeviceService.updateMonitorDevice(monitorDevice));
    }

    /**
     * 删除监控设备
     */
    @PreAuthorize("@ss.hasPermi('monitor:device:remove')")
    @Log(title = "监控设备", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(monitorDeviceService.deleteMonitorDeviceByIds(ids));
    }
}
