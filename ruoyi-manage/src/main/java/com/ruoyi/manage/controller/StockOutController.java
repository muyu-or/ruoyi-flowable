package com.ruoyi.manage.controller;


import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.manage.domain.StockOut;
import com.ruoyi.manage.service.IStockOutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 出库记录Controller
 * 
 * @author xgh
 * @date 2025-11-04
 */
@RestController
@RequestMapping("/manage/stockout")
public class StockOutController extends BaseController
{
    @Autowired
    private IStockOutService stockOutService;

    /**
     * 查询出库记录列表
     */
    @PreAuthorize("@ss.hasPermi('manage:stockout:list')")
    @GetMapping("/list")
    public TableDataInfo list(StockOut stockOut)
    {
        startPage();
        List<StockOut> list = stockOutService.selectStockOutList(stockOut);
        return getDataTable(list);
    }

    /**
     * 导出出库记录列表
     */
    @PreAuthorize("@ss.hasPermi('manage:stockout:export')")
    @Log(title = "出库记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, StockOut stockOut)
    {
        List<StockOut> list = stockOutService.selectStockOutList(stockOut);
        ExcelUtil<StockOut> util = new ExcelUtil<StockOut>(StockOut.class);
        util.exportExcel(response, list, "出库记录数据");
    }

    /**
     * 获取出库记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('manage:stockout:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(stockOutService.selectStockOutById(id));
    }

    /**
     * 新增出库记录
     */
    @PreAuthorize("@ss.hasPermi('manage:stockout:add')")
    @Log(title = "出库记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody StockOut stockOut)
    {
        return toAjax(stockOutService.insertStockOut(stockOut));
    }

    /**
     * 修改出库记录
     */
    @PreAuthorize("@ss.hasPermi('manage:stockout:edit')")
    @Log(title = "出库记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody StockOut stockOut)
    {
        return toAjax(stockOutService.updateStockOut(stockOut));
    }

    /**
     * 删除出库记录
     */
    @PreAuthorize("@ss.hasPermi('manage:stockout:remove')")
    @Log(title = "出库记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(stockOutService.deleteStockOutByIds(ids));
    }
}
