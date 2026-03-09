package com.ruoyi.manage.service;

import java.util.List;
import com.ruoyi.manage.domain.MonitorDevice;

/**
 * 监控设备Service接口
 *
 * @author xgh
 * @date 2026-03-07
 */
public interface IMonitorDeviceService
{
    /**
     * 查询监控设备
     *
     * @param id 监控设备主键
     * @return 监控设备
     */
    public MonitorDevice selectMonitorDeviceById(Long id);

    /**
     * 查询监控设备列表
     *
     * @param monitorDevice 监控设备
     * @return 监控设备集合
     */
    public List<MonitorDevice> selectMonitorDeviceList(MonitorDevice monitorDevice);

    /**
     * 新增监控设备
     *
     * @param monitorDevice 监控设备
     * @return 结果
     */
    public int insertMonitorDevice(MonitorDevice monitorDevice);

    /**
     * 修改监控设备
     *
     * @param monitorDevice 监控设备
     * @return 结果
     */
    public int updateMonitorDevice(MonitorDevice monitorDevice);

    /**
     * 批量删除监控设备
     *
     * @param ids 需要删除的监控设备主键集合
     * @return 结果
     */
    public int deleteMonitorDeviceByIds(Long[] ids);

    /**
     * 删除监控设备信息
     *
     * @param id 监控设备主键
     * @return 结果
     */
    public int deleteMonitorDeviceById(Long id);
}
