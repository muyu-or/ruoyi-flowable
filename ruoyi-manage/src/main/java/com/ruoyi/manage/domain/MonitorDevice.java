package com.ruoyi.manage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 监控设备对象 monitor_device
 *
 * @author xgh
 * @date 2026-03-07
 */
public class MonitorDevice extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 设备名称 */
    @Excel(name = "设备名称")
    private String deviceName;

    /** 设备编号 */
    @Excel(name = "设备编号")
    private String deviceNo;

    /** 安装位置 */
    @Excel(name = "安装位置")
    private String location;

    /** 区域类型 */
    @Excel(name = "区域类型", dictType = "monitor_area_type")
    private String areaType;

    /** 设备类型 */
    @Excel(name = "设备类型", dictType = "monitor_device_type")
    private String deviceType;

    /** 品牌型号 */
    @Excel(name = "品牌型号")
    private String brand;

    /** IP地址 */
    @Excel(name = "IP地址")
    private String ipAddress;

    /** 视频流地址 */
    private String streamUrl;

    /** 设备状态（1在线 2离线 3故障） */
    @Excel(name = "设备状态", dictType = "monitor_device_status")
    private String status;

    /** 安装日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "安装日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date installDate;

    /** 负责人 */
    @Excel(name = "负责人")
    private String manager;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setDeviceName(String deviceName)
    {
        this.deviceName = deviceName;
    }

    public String getDeviceName()
    {
        return deviceName;
    }

    public void setDeviceNo(String deviceNo)
    {
        this.deviceNo = deviceNo;
    }

    public String getDeviceNo()
    {
        return deviceNo;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public String getLocation()
    {
        return location;
    }

    public void setAreaType(String areaType)
    {
        this.areaType = areaType;
    }

    public String getAreaType()
    {
        return areaType;
    }

    public void setDeviceType(String deviceType)
    {
        this.deviceType = deviceType;
    }

    public String getDeviceType()
    {
        return deviceType;
    }

    public void setBrand(String brand)
    {
        this.brand = brand;
    }

    public String getBrand()
    {
        return brand;
    }

    public void setIpAddress(String ipAddress)
    {
        this.ipAddress = ipAddress;
    }

    public String getIpAddress()
    {
        return ipAddress;
    }

    public void setStreamUrl(String streamUrl)
    {
        this.streamUrl = streamUrl;
    }

    public String getStreamUrl()
    {
        return streamUrl;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public void setInstallDate(Date installDate)
    {
        this.installDate = installDate;
    }

    public Date getInstallDate()
    {
        return installDate;
    }

    public void setManager(String manager)
    {
        this.manager = manager;
    }

    public String getManager()
    {
        return manager;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("deviceName", getDeviceName())
            .append("deviceNo", getDeviceNo())
            .append("location", getLocation())
            .append("areaType", getAreaType())
            .append("deviceType", getDeviceType())
            .append("brand", getBrand())
            .append("ipAddress", getIpAddress())
            .append("streamUrl", getStreamUrl())
            .append("status", getStatus())
            .append("installDate", getInstallDate())
            .append("manager", getManager())
            .append("remark", getRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
