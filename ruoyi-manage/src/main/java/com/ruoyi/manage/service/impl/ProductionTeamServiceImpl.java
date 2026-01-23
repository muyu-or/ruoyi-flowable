package com.ruoyi.manage.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.manage.mapper.ProductionTeamMapper;
import com.ruoyi.manage.domain.ProductionTeam;
import com.ruoyi.manage.service.IProductionTeamService;

import java.util.ArrayList;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.manage.domain.ProductionTeamUser;

import com.ruoyi.common.core.domain.entity.SysUser;

/**
 * 产线班组Service业务层处理
 * 
 * @author xgh
 * @date 2026-01-23
 */
@Service
public class ProductionTeamServiceImpl implements IProductionTeamService 
{
    @Autowired
    private ProductionTeamMapper productionTeamMapper;

    /**
     * 查询用户列表（含角色名称）
     * 
     * @param user 用户信息
     * @return 用户列表
     */
    @Override
    public List<SysUser> selectUserListForTeam(SysUser user)
    {
        return productionTeamMapper.selectUserListForTeam(user);
    }

    /**
     * 查询产线班组
     * 
     * @param id 产线班组主键
     * @return 产线班组
     */
    @Override
    public ProductionTeam selectProductionTeamById(Long id)
    {
        ProductionTeam team = productionTeamMapper.selectProductionTeamById(id);
        if (team != null)
        {
            team.setUserList(productionTeamMapper.selectUserListByTeamId(id));
        }
        return team;
    }

    /**
     * 查询产线班组列表
     * 
     * @param productionTeam 产线班组
     * @return 产线班组
     */
    @Override
    public List<ProductionTeam> selectProductionTeamList(ProductionTeam productionTeam)
    {
        return productionTeamMapper.selectProductionTeamList(productionTeam);
    }

    /**
     * 新增产线班组
     * 
     * @param productionTeam 产线班组
     * @return 结果
     */
    @Override
    public int insertProductionTeam(ProductionTeam productionTeam)
    {
        productionTeam.setCreateTime(DateUtils.getNowDate());
        int rows = productionTeamMapper.insertProductionTeam(productionTeam);
        insertProductionTeamUser(productionTeam);
        return rows;
    }

    /**
     * 修改产线班组
     * 
     * @param productionTeam 产线班组
     * @return 结果
     */
    @Override
    public int updateProductionTeam(ProductionTeam productionTeam)
    {
        productionTeam.setUpdateTime(DateUtils.getNowDate());
        // 如果传入了userIds（不为null），则更新成员列表；否则（为null）不修改成员
        if (productionTeam.getUserIds() != null) {
            productionTeamMapper.deleteProductionTeamUserByTeamId(productionTeam.getId());
            insertProductionTeamUser(productionTeam);
        }
        return productionTeamMapper.updateProductionTeam(productionTeam);
    }

    /**
     * 批量删除产线班组
     * 
     * @param ids 需要删除的产线班组主键
     * @return 结果
     */
    @Override
    public int deleteProductionTeamByIds(Long[] ids)
    {
        productionTeamMapper.deleteProductionTeamUserByTeamIds(ids);
        return productionTeamMapper.deleteProductionTeamByIds(ids);
    }

    /**
     * 删除产线班组信息
     * 
     * @param id 产线班组主键
     * @return 结果
     */
    @Override
    public int deleteProductionTeamById(Long id)
    {
        productionTeamMapper.deleteProductionTeamUserByTeamId(id);
        return productionTeamMapper.deleteProductionTeamById(id);
    }

    /**
     * 新增班组用户关联信息
     */
    public void insertProductionTeamUser(ProductionTeam productionTeam)
    {
        Long[] userIds = productionTeam.getUserIds();
        if (StringUtils.isNotNull(userIds))
        {
            List<ProductionTeamUser> list = new ArrayList<ProductionTeamUser>();
            for (Long userId : userIds)
            {
                ProductionTeamUser tu = new ProductionTeamUser();
                tu.setTeamId(productionTeam.getId());
                tu.setUserId(userId);
                list.add(tu);
            }
            if (list.size() > 0)
            {
                productionTeamMapper.batchProductionTeamUser(list);
            }
        }
    }
}
