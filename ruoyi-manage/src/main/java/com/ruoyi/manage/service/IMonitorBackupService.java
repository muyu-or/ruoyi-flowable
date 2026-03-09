package com.ruoyi.manage.service;

import java.util.List;
import com.ruoyi.manage.domain.MonitorBackup;

/**
 * 录像备份Service接口
 *
 * @author xgh
 * @date 2026-03-07
 */
public interface IMonitorBackupService
{
    /**
     * 查询录像备份
     *
     * @param id 录像备份主键
     * @return 录像备份
     */
    public MonitorBackup selectMonitorBackupById(Long id);

    /**
     * 查询录像备份列表
     *
     * @param monitorBackup 录像备份
     * @return 录像备份集合
     */
    public List<MonitorBackup> selectMonitorBackupList(MonitorBackup monitorBackup);

    /**
     * 新增录像备份
     *
     * @param monitorBackup 录像备份
     * @return 结果
     */
    public int insertMonitorBackup(MonitorBackup monitorBackup);

    /**
     * 修改录像备份
     *
     * @param monitorBackup 录像备份
     * @return 结果
     */
    public int updateMonitorBackup(MonitorBackup monitorBackup);

    /**
     * 批量删除录像备份
     *
     * @param ids 需要删除的录像备份主键集合
     * @return 结果
     */
    public int deleteMonitorBackupByIds(Long[] ids);

    /**
     * 删除录像备份信息
     *
     * @param id 录像备份主键
     * @return 结果
     */
    public int deleteMonitorBackupById(Long id);
}
