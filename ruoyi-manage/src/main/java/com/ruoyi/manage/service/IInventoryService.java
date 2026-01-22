package com.ruoyi.manage.service;


import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.manage.domain.Inventory;
import com.ruoyi.manage.domain.dto.InventoryDTO;
import com.ruoyi.manage.domain.vo.InventoryVO;

import java.util.List;

/**
 * 库存信息Service接口
 * 
 * @author xgh
 * @date 2025-11-04
 */
public interface IInventoryService 
{
    /**
     * 查询库存信息
     * 
     * @param id 库存信息主键
     * @return 库存信息
     */
    public Inventory selectInventoryById(Long id);

    /**
     * 查询库存信息列表
     * 
     * @param inventory 库存信息
     * @return 库存信息集合
     */
    public List<Inventory> selectInventoryList(Inventory inventory);

    /**
     * 新增库存信息
     * 
     * @param inventoryDTO 库存信息
     * @return 结果
     */
    public int insertInventory(InventoryDTO inventoryDTO);

    /**
     * 修改库存信息
     * 
     * @param inventory 库存信息
     * @return 结果
     */
    public int updateInventory(Inventory inventory);

    /**
     * 批量删除库存信息
     * 
     * @param ids 需要删除的库存信息主键集合
     * @return 结果
     */
    public int deleteInventoryByIds(Long[] ids);

    /**
     * 删除库存信息信息
     * 
     * @param id 库存信息主键
     * @return 结果
     */
    public int deleteInventoryById(Long id);

    /**
     * 查询库存信息列表
     */
    List<InventoryVO> selectInventoryVOList(InventoryDTO inventoryDTO);

    AjaxResult processStockOut(InventoryDTO inventoryDTO);
}
