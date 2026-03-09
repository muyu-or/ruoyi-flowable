package com.ruoyi.manage.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.manage.mapper.ProductionTeamMapper;
import com.ruoyi.manage.domain.ProductionTeam;
import com.ruoyi.manage.service.IProductionTeamService;

import java.util.ArrayList;
import java.util.HashMap;
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
            List<SysUser> userList = productionTeamMapper.selectUserListByTeamId(id);
            // 将 searchValue 中暂存的 position 转移到 params["position"]
            if (userList != null)
            {
                for (SysUser user : userList)
                {
                    if (StringUtils.isNotEmpty(user.getSearchValue()))
                    {
                        if (user.getParams() == null)
                        {
                            user.setParams(new HashMap<>());
                        }
                        user.getParams().put("position", user.getSearchValue());
                        user.setSearchValue(null);
                    }
                }
            }
            team.setUserList(userList);
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
        // 优先使用 memberList（含职位），其次使用 userIds（兼容旧逻辑）
        if (productionTeam.getMemberList() != null || productionTeam.getUserIds() != null) {
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
     * 新增班组用户关联信息（优先使用 memberList 含职位，兼容旧 userIds）
     */
    public void insertProductionTeamUser(ProductionTeam productionTeam)
    {
        List<ProductionTeamUser> memberList = productionTeam.getMemberList();
        if (memberList != null && memberList.size() > 0)
        {
            // 使用 memberList（含 userId + position）
            for (ProductionTeamUser member : memberList)
            {
                member.setTeamId(productionTeam.getId());
            }
            productionTeamMapper.batchProductionTeamUser(memberList);
        }
        else
        {
            // 兼容旧方式：仅传 userIds（无 position）
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
}
