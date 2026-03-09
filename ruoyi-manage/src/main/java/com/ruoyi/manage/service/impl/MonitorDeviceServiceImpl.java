package com.ruoyi.manage.service.impl;

import java.util.List;
import javax.annotation.Resource;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.stereotype.Service;
import com.ruoyi.manage.mapper.MonitorDeviceMapper;
import com.ruoyi.manage.domain.MonitorDevice;
import com.ruoyi.manage.service.IMonitorDeviceService;

/**
 * 监控设备Service业务层处理
 *
 * @author xgh
 * @date 2026-03-07
 */
@Service
public class MonitorDeviceServiceImpl implements IMonitorDeviceService
{
    @Resource
    private MonitorDeviceMapper monitorDeviceMapper;

    /**
     * 查询监控设备
     *
     * @param id 监控设备主键
     * @return 监控设备
     */
    @Override
    public MonitorDevice selectMonitorDeviceById(Long id)
    {
        return monitorDeviceMapper.selectMonitorDeviceById(id);
    }

    /**
     * 查询监控设备列表
     *
     * @param monitorDevice 监控设备
     * @return 监控设备
     */
    @Override
    public List<MonitorDevice> selectMonitorDeviceList(MonitorDevice monitorDevice)
    {
        return monitorDeviceMapper.selectMonitorDeviceList(monitorDevice);
    }

    /**
     * 新增监控设备
     *
     * @param monitorDevice 监控设备
     * @return 结果
     */
    @Override
    public int insertMonitorDevice(MonitorDevice monitorDevice)
    {
        monitorDevice.setCreateTime(DateUtils.getNowDate());
        return monitorDeviceMapper.insertMonitorDevice(monitorDevice);
    }

    /**
     * 修改监控设备
     *
     * @param monitorDevice 监控设备
     * @return 结果
     */
    @Override
    public int updateMonitorDevice(MonitorDevice monitorDevice)
    {
        monitorDevice.setUpdateTime(DateUtils.getNowDate());
        return monitorDeviceMapper.updateMonitorDevice(monitorDevice);
    }

    /**
     * 批量删除监控设备
     *
     * @param ids 需要删除的监控设备主键
     * @return 结果
     */
    @Override
    public int deleteMonitorDeviceByIds(Long[] ids)
    {
        return monitorDeviceMapper.deleteMonitorDeviceByIds(ids);
    }

    /**
     * 删除监控设备信息
     *
     * @param id 监控设备主键
     * @return 结果
     */
    @Override
    public int deleteMonitorDeviceById(Long id)
    {
        return monitorDeviceMapper.deleteMonitorDeviceById(id);
    }
}
