package com.ruoyi.flowable.domain;

import java.util.Date;

/**
 * 任务节点超时预警消息对象 task_warning
 *
 * @author xgh
 * @date 2026-03-09
 */
public class TaskWarning
{
    /** 主键 */
    private Long id;

    /** 接收人用户ID */
    private Long userId;

    /** 预警类型: deadline_soon=即将超时, overdue=已超时 */
    private String warnType;

    /** 流程实例ID */
    private String procInstId;

    /** 节点key(BPMN ID) */
    private String nodeKey;

    /** 节点名称 */
    private String nodeName;

    /** 节点计划结束日期 yyyy-MM-dd */
    private String endDate;

    /** 是否已读: 0=未读, 1=已读 */
    private Integer isRead;

    /** 创建时间 */
    private Date createTime;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setWarnType(String warnType)
    {
        this.warnType = warnType;
    }

    public String getWarnType()
    {
        return warnType;
    }

    public void setProcInstId(String procInstId)
    {
        this.procInstId = procInstId;
    }

    public String getProcInstId()
    {
        return procInstId;
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

    public void setEndDate(String endDate)
    {
        this.endDate = endDate;
    }

    public String getEndDate()
    {
        return endDate;
    }

    public void setIsRead(Integer isRead)
    {
        this.isRead = isRead;
    }

    public Integer getIsRead()
    {
        return isRead;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    @Override
    public String toString()
    {
        return "TaskWarning{" +
            "id=" + id +
            ", userId=" + userId +
            ", warnType='" + warnType + '\'' +
            ", procInstId='" + procInstId + '\'' +
            ", nodeKey='" + nodeKey + '\'' +
            ", nodeName='" + nodeName + '\'' +
            ", endDate='" + endDate + '\'' +
            ", isRead=" + isRead +
            ", createTime=" + createTime +
            '}';
    }
}
