package com.ruoyi.manage.controller;


import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.manage.domain.StockIn;
import com.ruoyi.manage.service.IStockInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 入库记录Controller
 * 
 * @author xgh
 * @date 2025-11-04
 */
@RestController
@RequestMapping("/manage/stockin")
public class StockInController extends BaseController
{
    @Autowired
    private IStockInService stockInService;

    /**
     * 查询入库记录列表
     */
    @PreAuthorize("@ss.hasPermi('manage:stockin:list')")
    @GetMapping("/list")
    public TableDataInfo list(StockIn stockIn)
    {
        startPage();
        List<StockIn> list = stockInService.selectStockInList(stockIn);
        return getDataTable(list);
    }

    /**
     * 导出入库记录列表
     */
    @PreAuthorize("@ss.hasPermi('manage:stockin:export')")
    @Log(title = "入库记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, StockIn stockIn)
    {
        List<StockIn> list = stockInService.selectStockInList(stockIn);
        ExcelUtil<StockIn> util = new ExcelUtil<StockIn>(StockIn.class);
        util.exportExcel(response, list, "入库记录数据");
    }

    /**
     * 获取入库记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('manage:stockin:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(stockInService.selectStockInById(id));
    }

    /**
     * 新增入库记录
     */
    @PreAuthorize("@ss.hasPermi('manage:stockin:add')")
    @Log(title = "入库记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody StockIn stockIn)
    {
        return toAjax(stockInService.insertStockIn(stockIn));
    }

    /**
     * 修改入库记录
     */
    @PreAuthorize("@ss.hasPermi('manage:stockin:edit')")
    @Log(title = "入库记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody StockIn stockIn)
    {
        return toAjax(stockInService.updateStockIn(stockIn));
    }

    /**
     * 删除入库记录
     */
    @PreAuthorize("@ss.hasPermi('manage:stockin:remove')")
    @Log(title = "入库记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(stockInService.deleteStockInByIds(ids));
    }
}
