package com.ruoyi.manage.mapper;


import com.ruoyi.manage.domain.StockIn;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 入库记录Mapper接口
 * 
 * @author xgh
 * @date 2025-11-04
 */
@Mapper
public interface StockInMapper 
{
    /**
     * 查询入库记录
     * 
     * @param id 入库记录主键
     * @return 入库记录
     */
    public StockIn selectStockInById(Long id);

    /**
     * 查询入库记录列表
     * 
     * @param stockIn 入库记录
     * @return 入库记录集合
     */
    public List<StockIn> selectStockInList(StockIn stockIn);

    /**
     * 新增入库记录
     * 
     * @param stockIn 入库记录
     * @return 结果
     */
    public int insertStockIn(StockIn stockIn);

    /**
     * 修改入库记录
     * 
     * @param stockIn 入库记录
     * @return 结果
     */
    public int updateStockIn(StockIn stockIn);

    /**
     * 删除入库记录
     * 
     * @param id 入库记录主键
     * @return 结果
     */
    public int deleteStockInById(Long id);

    /**
     * 批量删除入库记录
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteStockInByIds(Long[] ids);
}
