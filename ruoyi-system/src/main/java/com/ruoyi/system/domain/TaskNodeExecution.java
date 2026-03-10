package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 节点级任务执行记录对象 task_node_execution
 *
 * @author xgh
 * @date 2026-02-28
 */
public class TaskNodeExecution extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 关联的流程执行记录ID */
    @Excel(name = "执行记录ID")
    private Long execRecordId;

    /** Flowable任务ID */
    @Excel(name = "任务ID")
    private String taskId;

    /** 节点业务key */
    @Excel(name = "节点key")
    private String nodeKey;

    /** 节点名称 */
    @Excel(name = "节点名称")
    private String nodeName;

    /** 该节点的班组ID */
    @Excel(name = "班组ID")
    private Long assignedTeamId;

    /** 分配给的用户ID */
    @Excel(name = "分配用户ID")
    private Long assignedUserId;

    /** 认领人ID */
    @Excel(name = "认领人ID")
    private Long claimUserId;

    /** 任务状态 */
    @Excel(name = "任务状态")
    private String status;

    /** 任务开始时间 */
    @Excel(name = "开始时间")
    private String startTime;

    /** 认领时间 */
    @Excel(name = "认领时间")
    private String claimTime;

    /** 完成时间 */
    @Excel(name = "完成时间")
    private String completeTime;

    /** 表单数据 */
    private String formData;

    /** 任务结果 */
    @Excel(name = "任务结果")
    private String result;

    /** 审批意见 */
    @Excel(name = "审批意见")
    private String approveComment;

    /** 处理耗时（秒） */
    @Excel(name = "处理耗时（秒）")
    private Long processDuration;

    /** 是否超时: 0=正常,1=已超时 */
    private Integer timeoutFlag;

    /** 计划结束日期 yyyy-MM-dd */
    private String planEndDate;

    /** 计划开始日期 yyyy-MM-dd */
    private String planStartDate;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setExecRecordId(Long execRecordId)
    {
        this.execRecordId = execRecordId;
    }

    public Long getExecRecordId()
    {
        return execRecordId;
    }

    public void setTaskId(String taskId)
    {
        this.taskId = taskId;
    }

    public String getTaskId()
    {
        return taskId;
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

    public void setAssignedTeamId(Long assignedTeamId)
    {
        this.assignedTeamId = assignedTeamId;
    }

    public Long getAssignedTeamId()
    {
        return assignedTeamId;
    }

    public void setAssignedUserId(Long assignedUserId)
    {
        this.assignedUserId = assignedUserId;
    }

    public Long getAssignedUserId()
    {
        return assignedUserId;
    }

    public void setClaimUserId(Long claimUserId)
    {
        this.claimUserId = claimUserId;
    }

    public Long getClaimUserId()
    {
        return claimUserId;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStartTime(String startTime)
    {
        this.startTime = startTime;
    }

    public String getStartTime()
    {
        return startTime;
    }

    public void setClaimTime(String claimTime)
    {
        this.claimTime = claimTime;
    }

    public String getClaimTime()
    {
        return claimTime;
    }

    public void setCompleteTime(String completeTime)
    {
        this.completeTime = completeTime;
    }

    public String getCompleteTime()
    {
        return completeTime;
    }

    public void setFormData(String formData)
    {
        this.formData = formData;
    }

    public String getFormData()
    {
        return formData;
    }

    public void setResult(String result)
    {
        this.result = result;
    }

    public String getResult()
    {
        return result;
    }

    public void setApproveComment(String approveComment)
    {
        this.approveComment = approveComment;
    }

    public String getApproveComment()
    {
        return approveComment;
    }

    public void setProcessDuration(Long processDuration)
    {
        this.processDuration = processDuration;
    }

    public Long getProcessDuration()
    {
        return processDuration;
    }

    public void setTimeoutFlag(Integer timeoutFlag)
    {
        this.timeoutFlag = timeoutFlag;
    }

    public Integer getTimeoutFlag()
    {
        return timeoutFlag;
    }

    public void setPlanEndDate(String planEndDate)
    {
        this.planEndDate = planEndDate;
    }

    public String getPlanEndDate()
    {
        return planEndDate;
    }

    public void setPlanStartDate(String planStartDate)
    {
        this.planStartDate = planStartDate;
    }

    public String getPlanStartDate()
    {
        return planStartDate;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("execRecordId", getExecRecordId())
            .append("taskId", getTaskId())
            .append("nodeKey", getNodeKey())
            .append("nodeName", getNodeName())
            .append("assignedTeamId", getAssignedTeamId())
            .append("assignedUserId", getAssignedUserId())
            .append("claimUserId", getClaimUserId())
            .append("status", getStatus())
            .append("startTime", getStartTime())
            .append("claimTime", getClaimTime())
            .append("completeTime", getCompleteTime())
            .append("formData", getFormData())
            .append("result", getResult())
            .append("approveComment", getApproveComment())
            .append("processDuration", getProcessDuration())
            .append("timeoutFlag", getTimeoutFlag())
            .append("planEndDate", getPlanEndDate())
            .append("planStartDate", getPlanStartDate())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
