package com.ruoyi.manage.service;



import com.ruoyi.manage.domain.StockOut;

import java.util.List;

/**
 * 出库记录Service接口
 * 
 * @author xgh
 * @date 2025-11-04
 */
public interface IStockOutService 
{
    /**
     * 查询出库记录
     * 
     * @param id 出库记录主键
     * @return 出库记录
     */
    public StockOut selectStockOutById(Long id);

    /**
     * 查询出库记录列表
     * 
     * @param stockOut 出库记录
     * @return 出库记录集合
     */
    public List<StockOut> selectStockOutList(StockOut stockOut);

    /**
     * 新增出库记录
     * 
     * @param stockOut 出库记录
     * @return 结果
     */
    public int insertStockOut(StockOut stockOut);

    /**
     * 修改出库记录
     * 
     * @param stockOut 出库记录
     * @return 结果
     */
    public int updateStockOut(StockOut stockOut);

    /**
     * 批量删除出库记录
     * 
     * @param ids 需要删除的出库记录主键集合
     * @return 结果
     */
    public int deleteStockOutByIds(Long[] ids);

    /**
     * 删除出库记录信息
     * 
     * @param id 出库记录主键
     * @return 结果
     */
    public int deleteStockOutById(Long id);
}
