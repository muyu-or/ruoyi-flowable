package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 表格列宽全局配置表 sys_table_col_width
 *
 * admin 统一配置表格列宽，所有角色同步使用
 */
public class SysTableColWidth extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 路由路径，如 /system/user */
    private String routePath;

    /** 表格标识，同一页面多表格时区分 */
    private String tableId;

    /** 列宽配置JSON */
    private String columnsConfig;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getRoutePath()
    {
        return routePath;
    }

    public void setRoutePath(String routePath)
    {
        this.routePath = routePath;
    }

    public String getTableId()
    {
        return tableId;
    }

    public void setTableId(String tableId)
    {
        this.tableId = tableId;
    }

    public String getColumnsConfig()
    {
        return columnsConfig;
    }

    public void setColumnsConfig(String columnsConfig)
    {
        this.columnsConfig = columnsConfig;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("routePath", getRoutePath())
            .append("tableId", getTableId())
            .append("columnsConfig", getColumnsConfig())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}