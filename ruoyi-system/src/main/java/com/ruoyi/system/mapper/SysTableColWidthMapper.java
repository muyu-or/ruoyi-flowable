package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.SysTableColWidth;

/**
 * 表格列宽全局配置Mapper接口
 */
public interface SysTableColWidthMapper
{
    /**
     * 查询指定路由和表格的列宽配置
     *
     * @param routePath 路由路径
     * @param tableId 表格标识
     * @return 配置记录
     */
    public SysTableColWidth selectConfigByRoute(@Param("routePath") String routePath, @Param("tableId") String tableId);

    /**
     * 查询指定路由下所有表格的列宽配置
     *
     * @param routePath 路由路径
     * @return 配置列表
     */
    public List<SysTableColWidth> selectConfigsByRoutePath(@Param("routePath") String routePath);

    /**
     * 查询所有列宽配置
     *
     * @return 配置列表
     */
    public List<SysTableColWidth> selectConfigList();

    /**
     * 新增列宽配置
     *
     * @param config 配置信息
     * @return 影响行数
     */
    public int insertConfig(SysTableColWidth config);

    /**
     * 更新列宽配置（按 routePath + tableId）
     *
     * @param config 配置信息
     * @return 影响行数
     */
    public int updateConfig(SysTableColWidth config);

    /**
     * 删除指定路由和表格的配置
     *
     * @param routePath 路由路径
     * @param tableId 表格标识
     * @return 影响行数
     */
    public int deleteConfig(@Param("routePath") String routePath, @Param("tableId") String tableId);

    /**
     * 删除指定路由下所有表格配置
     *
     * @param routePath 路由路径
     * @return 影响行数
     */
    public int deleteConfigsByRoutePath(@Param("routePath") String routePath);

    /**
     * 删除所有配置
     *
     * @return 影响行数
     */
    public int deleteAllConfig();
}