package com.ruoyi.manage.service.impl;


import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.manage.domain.StockIn;
import com.ruoyi.manage.mapper.StockInMapper;
import com.ruoyi.manage.service.IStockInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 入库记录Service业务层处理
 * 
 * @author xgh
 * @date 2025-11-04
 */
@Service
public class StockInServiceImpl implements IStockInService
{
    @Autowired
    private StockInMapper stockInMapper;

    /**
     * 查询入库记录
     * 
     * @param id 入库记录主键
     * @return 入库记录
     */
    @Override
    public StockIn selectStockInById(Long id)
    {
        return stockInMapper.selectStockInById(id);
    }

    /**
     * 查询入库记录列表
     * 
     * @param stockIn 入库记录
     * @return 入库记录
     */
    @Override
    public List<StockIn> selectStockInList(StockIn stockIn)
    {
        return stockInMapper.selectStockInList(stockIn);
    }

    /**
     * 新增入库记录
     * 
     * @param stockIn 入库记录
     * @return 结果
     */
    @Override
    public int insertStockIn(StockIn stockIn)
    {
        stockIn.setCreateTime(DateUtils.getNowDate());
        return stockInMapper.insertStockIn(stockIn);
    }

    /**
     * 修改入库记录
     * 
     * @param stockIn 入库记录
     * @return 结果
     */
    @Override
    public int updateStockIn(StockIn stockIn)
    {
        return stockInMapper.updateStockIn(stockIn);
    }

    /**
     * 批量删除入库记录
     * 
     * @param ids 需要删除的入库记录主键
     * @return 结果
     */
    @Override
    public int deleteStockInByIds(Long[] ids)
    {
        return stockInMapper.deleteStockInByIds(ids);
    }

    /**
     * 删除入库记录信息
     * 
     * @param id 入库记录主键
     * @return 结果
     */
    @Override
    public int deleteStockInById(Long id)
    {
        return stockInMapper.deleteStockInById(id);
    }
}
