package com.ruoyi.manage.service.impl;


import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.manage.domain.StockOut;
import com.ruoyi.manage.mapper.StockOutMapper;
import com.ruoyi.manage.service.IStockOutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 出库记录Service业务层处理
 * 
 * @author xgh
 * @date 2025-11-04
 */
@Service
public class StockOutServiceImpl implements IStockOutService
{
    @Autowired
    private StockOutMapper stockOutMapper;

    /**
     * 查询出库记录
     * 
     * @param id 出库记录主键
     * @return 出库记录
     */
    @Override
    public StockOut selectStockOutById(Long id)
    {
        return stockOutMapper.selectStockOutById(id);
    }

    /**
     * 查询出库记录列表
     * 
     * @param stockOut 出库记录
     * @return 出库记录
     */
    @Override
    public List<StockOut> selectStockOutList(StockOut stockOut)
    {
        return stockOutMapper.selectStockOutList(stockOut);
    }

    /**
     * 新增出库记录
     * 
     * @param stockOut 出库记录
     * @return 结果
     */
    @Override
    public int insertStockOut(StockOut stockOut)
    {
        stockOut.setCreateTime(DateUtils.getNowDate());
        return stockOutMapper.insertStockOut(stockOut);
    }

    /**
     * 修改出库记录
     * 
     * @param stockOut 出库记录
     * @return 结果
     */
    @Override
    public int updateStockOut(StockOut stockOut)
    {
        return stockOutMapper.updateStockOut(stockOut);
    }

    /**
     * 批量删除出库记录
     * 
     * @param ids 需要删除的出库记录主键
     * @return 结果
     */
    @Override
    public int deleteStockOutByIds(Long[] ids)
    {
        return stockOutMapper.deleteStockOutByIds(ids);
    }

    /**
     * 删除出库记录信息
     * 
     * @param id 出库记录主键
     * @return 结果
     */
    @Override
    public int deleteStockOutById(Long id)
    {
        return stockOutMapper.deleteStockOutById(id);
    }
}
