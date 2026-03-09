package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 流程执行记录对象 task_execution_record
 *
 * @author xgh
 * @date 2026-02-28
 */
public class TaskExecutionRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** Flowable流程实例ID */
    @Excel(name = "流程实例ID")
    private String procInstId;

    /** 流程定义key */
    @Excel(name = "流程定义key")
    private String procDefKey;

    /** 流程版本 */
    @Excel(name = "流程版本")
    private Integer procDefVersion;

    /** 流程发起人ID */
    @Excel(name = "发起人ID")
    private Long initiatorId;

    /** 主班组ID */
    @Excel(name = "主班组ID")
    private Long mainTeamId;

    /** 流程状态 */
    @Excel(name = "流程状态")
    private String status;

    /** 流程完成时间 */
    @Excel(name = "完成时间")
    private String completeTime;

    /** 流程总耗时（秒） */
    @Excel(name = "总耗时（秒）")
    private Long totalDuration;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setProcInstId(String procInstId)
    {
        this.procInstId = procInstId;
    }

    public String getProcInstId()
    {
        return procInstId;
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

    public void setInitiatorId(Long initiatorId)
    {
        this.initiatorId = initiatorId;
    }

    public Long getInitiatorId()
    {
        return initiatorId;
    }

    public void setMainTeamId(Long mainTeamId)
    {
        this.mainTeamId = mainTeamId;
    }

    public Long getMainTeamId()
    {
        return mainTeamId;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public void setCompleteTime(String completeTime)
    {
        this.completeTime = completeTime;
    }

    public String getCompleteTime()
    {
        return completeTime;
    }

    public void setTotalDuration(Long totalDuration)
    {
        this.totalDuration = totalDuration;
    }

    public Long getTotalDuration()
    {
        return totalDuration;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("procInstId", getProcInstId())
            .append("procDefKey", getProcDefKey())
            .append("procDefVersion", getProcDefVersion())
            .append("initiatorId", getInitiatorId())
            .append("mainTeamId", getMainTeamId())
            .append("status", getStatus())
            .append("completeTime", getCompleteTime())
            .append("totalDuration", getTotalDuration())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
