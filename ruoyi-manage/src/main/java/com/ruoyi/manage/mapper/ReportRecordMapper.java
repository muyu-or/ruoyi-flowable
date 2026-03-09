package com.ruoyi.manage.mapper;

import com.ruoyi.manage.domain.ReportRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 测试报告管理 Mapper 接口
 */
@Mapper
public interface ReportRecordMapper {

    ReportRecord selectReportRecordById(Long id);

    List<ReportRecord> selectReportRecordList(ReportRecord record);

    /**
     * 按可见用户名列表查询（供班组长使用）
     * @param record  查询条件
     * @param visibleUsers 可见用户名列表
     */
    List<ReportRecord> selectReportRecordListByUsers(
            @Param("record") ReportRecord record,
            @Param("visibleUsers") List<String> visibleUsers);

    int insertReportRecord(ReportRecord record);

    int updateReportRecord(ReportRecord record);

    int deleteReportRecordById(Long id);

    int deleteReportRecordByIds(Long[] ids);

    /**
     * 按 ID 和创建人校验归属（用于删除权限校验）
     */
    int countByIdAndCreateBy(@Param("id") Long id, @Param("createBy") String createBy);
}
