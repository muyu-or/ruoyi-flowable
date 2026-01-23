package com.ruoyi.manage.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 测试报告模板配置对象 report_template
 * 
 * @author xgh
 * @date 2026-01-23
 */
public class ReportTemplate extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 模板唯一编码  */
    @Excel(name = "模板唯一编码 ")
    private String templateCode;

    /** 模板名称 */
    @Excel(name = "模板名称")
    private String templateName;

    /** 模板引擎类型 (如: FREEMARKER, JASPER, POI) */
    @Excel(name = "模板引擎类型 (如: FREEMARKER, JASPER, POI)")
    private String templateType;

    /** 业务测试类型 (如: UNIT, INTEGRATION, API) */
    @Excel(name = "业务测试类型 (如: UNIT, INTEGRATION, API)")
    private String testType;

    /** 模板文件存储路径/URL */
    private String storagePath;

    /** 模板参数定义(JSON格式，用于前端生成表单) */
    private String paramConfig;

    /** 状态 (1=启用, 0=停用) */
    @Excel(name = "状态 (1=启用, 0=停用)")
    private Long reportStatus;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setTemplateCode(String templateCode) 
    {
        this.templateCode = templateCode;
    }

    public String getTemplateCode() 
    {
        return templateCode;
    }

    public void setTemplateName(String templateName) 
    {
        this.templateName = templateName;
    }

    public String getTemplateName() 
    {
        return templateName;
    }

    public void setTemplateType(String templateType) 
    {
        this.templateType = templateType;
    }

    public String getTemplateType() 
    {
        return templateType;
    }

    public void setTestType(String testType) 
    {
        this.testType = testType;
    }

    public String getTestType() 
    {
        return testType;
    }

    public void setStoragePath(String storagePath) 
    {
        this.storagePath = storagePath;
    }

    public String getStoragePath() 
    {
        return storagePath;
    }

    public void setParamConfig(String paramConfig) 
    {
        this.paramConfig = paramConfig;
    }

    public String getParamConfig() 
    {
        return paramConfig;
    }

    public void setReportStatus(Long reportStatus) 
    {
        this.reportStatus = reportStatus;
    }

    public Long getReportStatus() 
    {
        return reportStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("templateCode", getTemplateCode())
            .append("templateName", getTemplateName())
            .append("templateType", getTemplateType())
            .append("testType", getTestType())
            .append("storagePath", getStoragePath())
            .append("paramConfig", getParamConfig())
            .append("reportStatus", getReportStatus())
            .append("remark", getRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
