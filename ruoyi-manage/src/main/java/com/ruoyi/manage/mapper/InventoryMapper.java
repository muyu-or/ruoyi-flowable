package com.ruoyi.manage.mapper;


import com.ruoyi.manage.domain.Inventory;
import com.ruoyi.manage.domain.dto.InventoryDTO;
import com.ruoyi.manage.domain.vo.InventoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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
    @Select("select * from inventory where material_id = #{materialId} limit 1")
    Inventory selectInventoryBymaterialId(String materialId);

    /*
       获取当日流水号
     */
    String findMaxCodeByPrefix(String codePrefix);
}
