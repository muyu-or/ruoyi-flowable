package com.ruoyi.manage.mapper;


import com.ruoyi.manage.domain.StockOut;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

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

    /**
     * 按时间范围统计出库总量
     */
    Long sumQuantityByPeriod(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 按时间段分组统计出库数量
     *
     * @param startDate  开始时间
     * @param endDate    结束时间
     * @param dateFormat MySQL DATE_FORMAT 格式串
     * @return 每行含 label（时间段标签）和 qty（数量）
     */
    List<Map<String, Object>> sumQuantityGroupByPeriod(@Param("startDate") Date startDate,
                                                       @Param("endDate") Date endDate,
                                                       @Param("dateFormat") String dateFormat);
}
