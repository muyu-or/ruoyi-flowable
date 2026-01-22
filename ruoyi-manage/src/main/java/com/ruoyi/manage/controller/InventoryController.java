package com.ruoyi.manage.controller;


import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.manage.domain.Inventory;
import com.ruoyi.manage.domain.dto.InventoryDTO;
import com.ruoyi.manage.domain.vo.InventoryVO;
import com.ruoyi.manage.service.IInventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 库存信息Controller
 * 
 * @author xgh
 * @date 2025-11-04
 */
@RestController
@RequestMapping("/manage/inventory")
@Slf4j
public class InventoryController extends BaseController
{
    @Autowired
    private IInventoryService inventoryService;

    /**
     * 查询库存信息列表
     */
    @PreAuthorize("@ss.hasPermi('manage:inventory:list')")
    @GetMapping("/list")
    public TableDataInfo list(InventoryDTO inventoryDTO)
    {
        startPage();
        List<InventoryVO> list = inventoryService.selectInventoryVOList(inventoryDTO);
        return getDataTable(list);
    }

    /**
     * 导出库存信息列表
     */
    @PreAuthorize("@ss.hasPermi('manage:inventory:export')")
    @Log(title = "库存信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Inventory inventory)
    {
        List<Inventory> list = inventoryService.selectInventoryList(inventory);
        ExcelUtil<Inventory> util = new ExcelUtil<Inventory>(Inventory.class);
        util.exportExcel(response, list, "库存信息数据");
    }

    /**
     * 获取库存信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('manage:inventory:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(inventoryService.selectInventoryById(id));
    }

    /**
     * 新增库存信息
     */
    @PreAuthorize("@ss.hasPermi('manage:inventory:add')")
    @Log(title = "库存信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody InventoryDTO inventoryDTO)
    {
        log.info("新增库存信息: {}", inventoryDTO);
        return toAjax(inventoryService.insertInventory(inventoryDTO));
    }

    /**
     * 修改库存信息
     */
    @PreAuthorize("@ss.hasPermi('manage:inventory:edit')")
    @Log(title = "库存信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Inventory inventory)
    {
        return toAjax(inventoryService.updateInventory(inventory));
    }
    /**
     * 处理库存出库
     */
    @PostMapping("/stock-out")
    @PreAuthorize("@ss.hasPermi('manage:inventory:edit')") // 沿用编辑权限
    @Log(title = "库存信息", businessType = BusinessType.UPDATE)
    @Transactional(rollbackFor = Exception.class) // 确保事务性
    public AjaxResult handleStockOut(@RequestBody InventoryDTO stockOutDto) {
        // 调用 Service 层来处理出库逻辑
        return success(inventoryService.processStockOut(stockOutDto));
    }

    /**
     * 删除库存信息
     */
    @PreAuthorize("@ss.hasPermi('manage:inventory:remove')")
    @Log(title = "库存信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(inventoryService.deleteInventoryByIds(ids));
    }
}
