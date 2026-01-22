package com.ruoyi.manage.mapper;


import com.ruoyi.manage.domain.StockOut;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 出库记录Mapper接口
 * 
 * @author xgh
 * @date 2025-11-04
 */
@Mapper
public interface StockOutMapper 
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
     * 删除出库记录
     * 
     * @param id 出库记录主键
     * @return 结果
     */
    public int deleteStockOutById(Long id);

    /**
     * 批量删除出库记录
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteStockOutByIds(Long[] ids);
}
