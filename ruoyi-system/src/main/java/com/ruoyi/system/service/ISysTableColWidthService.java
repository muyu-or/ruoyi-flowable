package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.SysTableColWidth;

/**
 * 表格列宽全局配置Service接口
 */
public interface ISysTableColWidthService
{
    /**
     * 查询指定路由和表格的列宽配置
     *
     * @param routePath 路由路径
     * @param tableId 表格标识
     * @return 配置记录
     */
    public SysTableColWidth selectConfigByRoute(String routePath, String tableId);

    /**
     * 查询指定路由下所有表格的列宽配置
     *
     * @param routePath 路由路径
     * @return 配置列表
     */
    public List<SysTableColWidth> selectConfigsByRoutePath(String routePath);

    /**
     * 查询所有列宽配置
     *
     * @return 配置列表
     */
    public List<SysTableColWidth> selectConfigList();

    /**
     * 保存或更新配置（按 routePath + tableId 唯一键）
     *
     * @param config 配置信息
     * @return 影响行数
     */
    public int saveOrUpdateConfig(SysTableColWidth config);

    /**
     * 删除指定路由和表格的配置（重置为默认）
     *
     * @param routePath 路由路径
     * @param tableId 表格标识
     * @return 影响行数
     */
    public int deleteConfig(String routePath, String tableId);

    /**
     * 删除指定路由下所有表格配置
     *
     * @param routePath 路由路径
     * @return 影响行数
     */
    public int deleteConfigsByRoutePath(String routePath);

    /**
     * 删除所有配置（重置全部）
     *
     * @return 影响行数
     */
    public int deleteAllConfig();
}