package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 流程节点与班组绑定配置对象 process_node_binding
 *
 * @author xgh
 * @date 2026-02-28
 */
public class ProcessNodeBinding extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 流程定义key */
    @Excel(name = "流程定义key")
    private String procDefKey;

    /** 流程版本 */
    @Excel(name = "流程版本")
    private Integer procDefVersion;

    /** 节点业务key */
    @Excel(name = "节点key")
    private String nodeKey;

    /** 节点名称 */
    @Excel(name = "节点名称")
    private String nodeName;

    /** 是否允许节点表单选择班组 */
    @Excel(name = "允许选择班组")
    private Integer allowTeamSelection;

    /** 默认班组ID */
    @Excel(name = "默认班组ID")
    private Long defaultTeamId;

    /** 表单中班组选择字段的name */
    @Excel(name = "表单字段名")
    private String formFieldName;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setProcDefKey(String procDefKey)
    {
        this.procDefKey = procDefKey;
    }

    public String getProcDefKey()
    {
        return procDefKey;
    }

    public void setProcDefVersion(Integer procDefVersion)
    {
        this.procDefVersion = procDefVersion;
    }

    public Integer getProcDefVersion()
    {
        return procDefVersion;
    }

    public void setNodeKey(String nodeKey)
    {
        this.nodeKey = nodeKey;
    }

    public String getNodeKey()
    {
        return nodeKey;
    }

    public void setNodeName(String nodeName)
    {
        this.nodeName = nodeName;
    }

    public String getNodeName()
    {
        return nodeName;
    }

    public void setAllowTeamSelection(Integer allowTeamSelection)
    {
        this.allowTeamSelection = allowTeamSelection;
    }

    public Integer getAllowTeamSelection()
    {
        return allowTeamSelection;
    }

    public void setDefaultTeamId(Long defaultTeamId)
    {
        this.defaultTeamId = defaultTeamId;
    }

    public Long getDefaultTeamId()
    {
        return defaultTeamId;
    }

    public void setFormFieldName(String formFieldName)
    {
        this.formFieldName = formFieldName;
    }

    public String getFormFieldName()
    {
        return formFieldName;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("procDefKey", getProcDefKey())
            .append("procDefVersion", getProcDefVersion())
            .append("nodeKey", getNodeKey())
            .append("nodeName", getNodeName())
            .append("allowTeamSelection", getAllowTeamSelection())
            .append("defaultTeamId", getDefaultTeamId())
            .append("formFieldName", getFormFieldName())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
