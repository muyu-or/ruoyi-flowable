package com.ruoyi.manage.mapper;

import java.util.List;
import com.ruoyi.manage.domain.MonitorDevice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 监控设备Mapper接口
 *
 * @author xgh
 * @date 2026-03-07
 */
@Mapper
public interface MonitorDeviceMapper
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
     * 删除监控设备
     *
     * @param id 监控设备主键
     * @return 结果
     */
    public int deleteMonitorDeviceById(Long id);

    /**
     * 批量删除监控设备
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMonitorDeviceByIds(Long[] ids);
}
