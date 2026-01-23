package com.ruoyi.manage.mapper;

import java.util.List;
import com.ruoyi.manage.domain.ProductionTeam;
import org.apache.ibatis.annotations.Mapper;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.manage.domain.ProductionTeamUser;

/**
 * 产线班组Mapper接口
 * 
 * @author xgh
 * @date 2026-01-23
 */
@Mapper
public interface ProductionTeamMapper 
{
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
     * 删除产线班组
     * 
     * @param id 产线班组主键
     * @return 结果
     */
    public int deleteProductionTeamById(Long id);

    /**
     * 批量删除产线班组
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteProductionTeamByIds(Long[] ids);

    /**
     * 批量删除班组用户关联
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteProductionTeamUserByTeamIds(Long[] ids);
    
    /**
     * 删除班组用户关联
     * 
     * @param id 班组ID
     * @return 结果
     */
    public int deleteProductionTeamUserByTeamId(Long id);
    
    /**
     * 批量新增班组用户关联
     * 
     * @param teamUserList 班组用户关联列表
     * @return 结果
     */
    public int batchProductionTeamUser(List<ProductionTeamUser> teamUserList);
    
    /**
     * 查询班组成员列表
     * 
     * @param teamId 班组ID
     * @return 用户列表
     */
    public List<SysUser> selectUserListByTeamId(Long teamId);

    /**
     * 查询用户列表（含角色名称）
     * 
     * @param user 用户信息
     * @return 用户列表
     */
    public List<SysUser> selectUserListForTeam(SysUser user);
}
