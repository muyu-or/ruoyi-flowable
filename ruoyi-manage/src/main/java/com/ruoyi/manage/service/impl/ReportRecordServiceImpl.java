package com.ruoyi.manage.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.manage.domain.ReportRecord;
import com.ruoyi.manage.mapper.ProductionTeamMapper;
import com.ruoyi.manage.mapper.ReportRecordMapper;
import com.ruoyi.manage.service.IReportRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 测试报告管理 Service 实现
 */
@Service
public class ReportRecordServiceImpl implements IReportRecordService {

    @Resource
    private ReportRecordMapper reportRecordMapper;

    @Resource
    private ProductionTeamMapper productionTeamMapper;

    @Override
    public ReportRecord selectReportRecordById(Long id) {
        return reportRecordMapper.selectReportRecordById(id);
    }

    @Override
    public List<ReportRecord> selectReportRecordList(ReportRecord record) {
        String username = SecurityUtils.getUsername();
        Long userId = SecurityUtils.getUserId();
        boolean viewAll = SecurityUtils.isAdmin(userId)
                || SecurityUtils.hasPermi("flowable:stat:all");
        boolean isLeader = !viewAll && SecurityUtils.hasRole("team_leader");

        if (viewAll) {
            // 管理角色（admin / zongguan 等拥有 flowable:stat:all）：查全部
            return reportRecordMapper.selectReportRecordList(record);
        } else if (isLeader) {
            // 班组长：查本人所在班组的所有成员用户名
            List<String> visibleUsers = productionTeamMapper.selectMemberUsernamesByLeaderUsername(username);
            // 确保包含自己
            if (!visibleUsers.contains(username)) {
                visibleUsers.add(username);
            }
            return reportRecordMapper.selectReportRecordListByUsers(record, visibleUsers);
        } else {
            // 普通用户：按 uploader = 自己 过滤（只看自己上传的报告）
            record.setUploader(username);
            return reportRecordMapper.selectReportRecordList(record);
        }
    }

    @Override
    public int insertReportRecord(ReportRecord record) {
        // 自动生成报告编码：RPT-yyyyMMdd-随机4位大写
        String datePart = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String randomPart = UUID.randomUUID().toString().replace("-", "")
                .substring(0, 4).toUpperCase();
        record.setReportCode("RPT-" + datePart + "-" + randomPart);
        // 设置创建人和时间
        record.setCreateBy(SecurityUtils.getUsername());
        record.setCreateTime(DateUtils.getNowDate());
        return reportRecordMapper.insertReportRecord(record);
    }

    @Override
    public int updateReportRecord(ReportRecord record) {
        record.setUpdateBy(SecurityUtils.getUsername());
        record.setUpdateTime(DateUtils.getNowDate());
        return reportRecordMapper.updateReportRecord(record);
    }

    @Override
    public int deleteReportRecordByIds(Long[] ids) {
        String username = SecurityUtils.getUsername();
        Long userId = SecurityUtils.getUserId();
        boolean viewAll = SecurityUtils.isAdmin(userId)
                || SecurityUtils.hasPermi("flowable:stat:all");
        boolean isLeader = !viewAll && SecurityUtils.hasRole("team_leader");

        for (Long id : ids) {
            if (viewAll) {
                continue; // 管理角色无限制
            } else if (isLeader) {
                // 班组长：可删本组成员（含自己）的记录
                List<String> visibleUsers = productionTeamMapper.selectMemberUsernamesByLeaderUsername(username);
                if (!visibleUsers.contains(username)) {
                    visibleUsers.add(username);
                }
                ReportRecord r = reportRecordMapper.selectReportRecordById(id);
                if (r == null || !visibleUsers.contains(r.getCreateBy())) {
                    throw new ServiceException("无权删除报告ID=" + id + "，该报告不属于您的班组");
                }
            } else {
                // 普通用户：只能删自己的
                int count = reportRecordMapper.countByIdAndCreateBy(id, username);
                if (count == 0) {
                    throw new ServiceException("无权删除报告ID=" + id + "，只能删除自己上传的报告");
                }
            }
        }
        return reportRecordMapper.deleteReportRecordByIds(ids);
    }
}
