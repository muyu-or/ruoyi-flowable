package com.ruoyi.system.service.impl;

import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.SysTableColWidth;
import com.ruoyi.system.mapper.SysTableColWidthMapper;
import com.ruoyi.system.service.ISysTableColWidthService;

/**
 * 表格列宽全局配置Service实现
 */
@Service
public class SysTableColWidthServiceImpl implements ISysTableColWidthService
{
    @Resource
    private SysTableColWidthMapper configMapper;

    /**
     * 查询指定路由和表格的列宽配置
     */
    @Override
    public SysTableColWidth selectConfigByRoute(String routePath, String tableId)
    {
        return configMapper.selectConfigByRoute(routePath, tableId);
    }

    /**
     * 查询指定路由下所有表格的列宽配置
     */
    @Override
    public List<SysTableColWidth> selectConfigsByRoutePath(String routePath)
    {
        return configMapper.selectConfigsByRoutePath(routePath);
    }

    /**
     * 查询所有列宽配置
     */
    @Override
    public List<SysTableColWidth> selectConfigList()
    {
        return configMapper.selectConfigList();
    }

    /**
     * 保存或更新配置（按 routePath + tableId 唯一键）
     * 先查询是否存在，存在则更新，不存在则新增
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveOrUpdateConfig(SysTableColWidth config)
    {
        String username = SecurityUtils.getUsername();
        Date now = new Date();

        // 查询是否已存在
        SysTableColWidth existing = configMapper.selectConfigByRoute(config.getRoutePath(), config.getTableId());

        if (existing != null)
        {
            // 更新
            config.setUpdateBy(username);
            config.setUpdateTime(now);
            return configMapper.updateConfig(config);
        }
        else
        {
            // 新增
            config.setCreateBy(username);
            config.setCreateTime(now);
            config.setUpdateBy(username);
            config.setUpdateTime(now);
            return configMapper.insertConfig(config);
        }
    }

    /**
     * 删除指定路由和表格的配置（重置为默认）
     */
    @Override
    public int deleteConfig(String routePath, String tableId)
    {
        return configMapper.deleteConfig(routePath, tableId);
    }

    /**
     * 删除指定路由下所有表格配置
     */
    @Override
    public int deleteConfigsByRoutePath(String routePath)
    {
        return configMapper.deleteConfigsByRoutePath(routePath);
    }

    /**
     * 删除所有配置（重置全部）
     */
    @Override
    public int deleteAllConfig()
    {
        return configMapper.deleteAllConfig();
    }
}