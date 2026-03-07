package com.ruoyi.manage.mapper;


import com.ruoyi.manage.domain.Inventory;
import com.ruoyi.manage.domain.dto.InventoryDTO;
import com.ruoyi.manage.domain.vo.InventoryVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 库存信息Mapper接口
 * 
 * @author xgh
 * @date 2025-11-04
 */
@Mapper
public interface InventoryMapper 
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
     * @param inventory 库存信息
     * @return 结果
     */
    public int insertInventory(Inventory inventory);

    /**
     * 修改库存信息
     * 
     * @param inventory 库存信息
     * @return 结果
     */
    public int updateInventory(Inventory inventory);

    /**
     * 删除库存信息
     * 
     * @param id 库存信息主键
     * @return 结果
     */
    public int deleteInventoryById(Long id);

    /**
     * 批量删除库存信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteInventoryByIds(Long[] ids);

    /**
     * 查询库存信息列表（支持入库类型查询）
     */
    List<InventoryVO> selectInventoryVOList(InventoryDTO inventoryDTO);

    /*
    * 根据物料ID查询库存信息
     */
    Inventory selectInventoryBymaterialId(String materialId);

    /**
     * 根据物料名称精确查询库存信息（用于同名物料合并入库）
     */
    Inventory selectInventoryByMaterialName(String materialName);

    /*
       获取当日流水号
     */
    String findMaxCodeByPrefix(String codePrefix);

    /**
     * 按时间周期统计物料类别数量
     */
    List<java.util.Map<String, Object>> countByCategoryAndPeriod(@org.apache.ibatis.annotations.Param("startDate") java.util.Date startDate, @org.apache.ibatis.annotations.Param("endDate") java.util.Date endDate, @org.apache.ibatis.annotations.Param("materialCategory") String materialCategory);

    /**
     * 按时间周期分段统计物料类别数量（用于柱状图）
     */
    List<java.util.Map<String, Object>> countBarsByPeriod(@org.apache.ibatis.annotations.Param("startDate") java.util.Date startDate, @org.apache.ibatis.annotations.Param("endDate") java.util.Date endDate, @org.apache.ibatis.annotations.Param("dateFormat") String dateFormat);

    /**
     * 按物料大类汇总时间段内入库数量（首页库存总览饼图）
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return 每行含 category（物料大类）和 totalQty（数量合计）
     */
    List<java.util.Map<String, Object>> sumInboundByCategory(@org.apache.ibatis.annotations.Param("startDate") java.util.Date startDate, @org.apache.ibatis.annotations.Param("endDate") java.util.Date endDate);
}
