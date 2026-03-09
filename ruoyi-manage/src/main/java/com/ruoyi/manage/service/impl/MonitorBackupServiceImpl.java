package com.ruoyi.manage.service.impl;

import java.util.List;
import javax.annotation.Resource;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.stereotype.Service;
import com.ruoyi.manage.mapper.MonitorBackupMapper;
import com.ruoyi.manage.domain.MonitorBackup;
import com.ruoyi.manage.service.IMonitorBackupService;

/**
 * 录像备份Service业务层处理
 *
 * @author xgh
 * @date 2026-03-07
 */
@Service
public class MonitorBackupServiceImpl implements IMonitorBackupService
{
    @Resource
    private MonitorBackupMapper monitorBackupMapper;

    /**
     * 查询录像备份
     *
     * @param id 录像备份主键
     * @return 录像备份
     */
    @Override
    public MonitorBackup selectMonitorBackupById(Long id)
    {
        return monitorBackupMapper.selectMonitorBackupById(id);
    }

    /**
     * 查询录像备份列表
     *
     * @param monitorBackup 录像备份
     * @return 录像备份
     */
    @Override
    public List<MonitorBackup> selectMonitorBackupList(MonitorBackup monitorBackup)
    {
        return monitorBackupMapper.selectMonitorBackupList(monitorBackup);
    }

    /**
     * 新增录像备份
     *
     * @param monitorBackup 录像备份
     * @return 结果
     */
    @Override
    public int insertMonitorBackup(MonitorBackup monitorBackup)
    {
        monitorBackup.setCreateTime(DateUtils.getNowDate());
        return monitorBackupMapper.insertMonitorBackup(monitorBackup);
    }

    /**
     * 修改录像备份
     *
     * @param monitorBackup 录像备份
     * @return 结果
     */
    @Override
    public int updateMonitorBackup(MonitorBackup monitorBackup)
    {
        monitorBackup.setUpdateTime(DateUtils.getNowDate());
        return monitorBackupMapper.updateMonitorBackup(monitorBackup);
    }

    /**
     * 批量删除录像备份
     *
     * @param ids 需要删除的录像备份主键
     * @return 结果
     */
    @Override
    public int deleteMonitorBackupByIds(Long[] ids)
    {
        return monitorBackupMapper.deleteMonitorBackupByIds(ids);
    }

    /**
     * 删除录像备份信息
     *
     * @param id 录像备份主键
     * @return 结果
     */
    @Override
    public int deleteMonitorBackupById(Long id)
    {
        return monitorBackupMapper.deleteMonitorBackupById(id);
    }
}
