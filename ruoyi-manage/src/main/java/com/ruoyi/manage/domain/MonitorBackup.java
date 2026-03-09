package com.ruoyi.manage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 录像备份对象 monitor_backup
 *
 * @author xgh
 * @date 2026-03-07
 */
public class MonitorBackup extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 关联设备ID */
    @Excel(name = "设备ID")
    private Long deviceId;

    /** 设备名称（冗余） */
    @Excel(name = "设备名称")
    private String deviceName;

    /** 录像日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "录像日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date recordDate;

    /** 录像开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "开始时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /** 录像结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "结束时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /** 文件名 */
    @Excel(name = "文件名")
    private String fileName;

    /** 文件存储路径 */
    private String filePath;

    /** 文件大小（字节） */
    @Excel(name = "文件大小(字节)")
    private Long fileSize;

    /** 备份状态（0待备份 1已备份 2备份失败） */
    @Excel(name = "备份状态", dictType = "monitor_backup_status")
    private String backupStatus;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setDeviceId(Long deviceId)
    {
        this.deviceId = deviceId;
    }

    public Long getDeviceId()
    {
        return deviceId;
    }

    public void setDeviceName(String deviceName)
    {
        this.deviceName = deviceName;
    }

    public String getDeviceName()
    {
        return deviceName;
    }

    public void setRecordDate(Date recordDate)
    {
        this.recordDate = recordDate;
    }

    public Date getRecordDate()
    {
        return recordDate;
    }

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }

    public Date getStartTime()
    {
        return startTime;
    }

    public void setEndTime(Date endTime)
    {
        this.endTime = endTime;
    }

    public Date getEndTime()
    {
        return endTime;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }

    public String getFilePath()
    {
        return filePath;
    }

    public void setFileSize(Long fileSize)
    {
        this.fileSize = fileSize;
    }

    public Long getFileSize()
    {
        return fileSize;
    }

    public void setBackupStatus(String backupStatus)
    {
        this.backupStatus = backupStatus;
    }

    public String getBackupStatus()
    {
        return backupStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("deviceId", getDeviceId())
            .append("deviceName", getDeviceName())
            .append("recordDate", getRecordDate())
            .append("startTime", getStartTime())
            .append("endTime", getEndTime())
            .append("fileName", getFileName())
            .append("filePath", getFilePath())
            .append("fileSize", getFileSize())
            .append("backupStatus", getBackupStatus())
            .append("remark", getRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
