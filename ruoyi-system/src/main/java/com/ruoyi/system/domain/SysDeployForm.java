package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 流程实例关联表单对象 sys_instance_form
 * 
 * @author Tony
 * @date 2021-03-30
 */
public class SysDeployForm extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 表单主键 */
    @Excel(name = "表单主键")
    private Long formId;

    /** 流程定义主键 */
    @Excel(name = "流程定义主键")
    private String deployId;

    /** 自定义Vue表单组件名（与formId互斥） */
    @Excel(name = "自定义表单组件名")
    private String formComponent;

    public String getFormComponent() {
        return formComponent;
    }

    public void setFormComponent(String formComponent) {
        this.formComponent = formComponent;
    }

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setFormId(Long formId) 
    {
        this.formId = formId;
    }

    public Long getFormId() 
    {
        return formId;
    }

    public String getDeployId() {
        return deployId;
    }

    public void setDeployId(String deployId) {
        this.deployId = deployId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("formId", getFormId())
            .append("deployId", getDeployId())
            .append("formComponent", getFormComponent())
            .toString();
    }
}
