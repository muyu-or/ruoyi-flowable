package com.ruoyi.manage.service;

import java.util.List;
import com.ruoyi.manage.domain.ProductionTeam;

import com.ruoyi.common.core.domain.entity.SysUser;

/**
 * 产线班组Service接口
 * 
 * @author xgh
 * @date 2026-01-23
 */
public interface IProductionTeamService 
{
    /**
     * 查询用户列表（含角色名称）
     * 
     * @param user 用户信息
     * @return 用户列表
     */
    public List<SysUser> selectUserListForTeam(SysUser user);

    /**
     * 查询产线班组
     * 
     * @param id 产线班组主键
     * @return 产线班组
     */
    public ProductionTeam selectProductionTeamById(Long id);

    /**
     * 查询产线班组列表
     * 
     * @param productionTeam 产线班组
     * @return 产线班组集合
     */
    public List<ProductionTeam> selectProductionTeamList(ProductionTeam productionTeam);

    /**
     * 新增产线班组
     * 
     * @param productionTeam 产线班组
     * @return 结果
     */
    public int insertProductionTeam(ProductionTeam productionTeam);

    /**
     * 修改产线班组
     * 
     * @param productionTeam 产线班组
     * @return 结果
     */
    public int updateProductionTeam(ProductionTeam productionTeam);

    /**
     * 批量删除产线班组
     * 
     * @param ids 需要删除的产线班组主键集合
     * @return 结果
     */
    public int deleteProductionTeamByIds(Long[] ids);

    /**
     * 删除产线班组信息
     * 
     * @param id 产线班组主键
     * @return 结果
     */
    public int deleteProductionTeamById(Long id);
}
