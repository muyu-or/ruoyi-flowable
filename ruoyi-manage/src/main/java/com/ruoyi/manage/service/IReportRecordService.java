package com.ruoyi.manage.service;

import com.ruoyi.manage.domain.ReportRecord;
import java.util.List;

/**
 * 测试报告管理 Service 接口
 */
public interface IReportRecordService {

    ReportRecord selectReportRecordById(Long id);

    /** 按当前登录用户角色过滤后查询列表 */
    List<ReportRecord> selectReportRecordList(ReportRecord record);

    /** 新增（自动生成 reportCode，自动设置 createBy/createTime） */
    int insertReportRecord(ReportRecord record);

    /** 修改 */
    int updateReportRecord(ReportRecord record);

    /**
     * 批量删除（含权限校验：普通用户只能删自己的，班组长只能删本组的，admin 不限）
     * 不满足权限时抛出 ServiceException
     */
    int deleteReportRecordByIds(Long[] ids);
}
