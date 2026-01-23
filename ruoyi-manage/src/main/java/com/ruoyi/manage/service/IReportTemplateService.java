package com.ruoyi.manage.service;

import java.util.List;
import com.ruoyi.manage.domain.ReportTemplate;

/**
 * 测试报告模板配置Service接口
 * 
 * @author xgh
 * @date 2026-01-23
 */
public interface IReportTemplateService 
{
    /**
     * 查询测试报告模板配置
     * 
     * @param id 测试报告模板配置主键
     * @return 测试报告模板配置
     */
    public ReportTemplate selectReportTemplateById(Long id);

    /**
     * 查询测试报告模板配置列表
     * 
     * @param reportTemplate 测试报告模板配置
     * @return 测试报告模板配置集合
     */
    public List<ReportTemplate> selectReportTemplateList(ReportTemplate reportTemplate);

    /**
     * 新增测试报告模板配置
     * 
     * @param reportTemplate 测试报告模板配置
     * @return 结果
     */
    public int insertReportTemplate(ReportTemplate reportTemplate);

    /**
     * 修改测试报告模板配置
     * 
     * @param reportTemplate 测试报告模板配置
     * @return 结果
     */
    public int updateReportTemplate(ReportTemplate reportTemplate);

    /**
     * 批量删除测试报告模板配置
     * 
     * @param ids 需要删除的测试报告模板配置主键集合
     * @return 结果
     */
    public int deleteReportTemplateByIds(Long[] ids);

    /**
     * 删除测试报告模板配置信息
     * 
     * @param id 测试报告模板配置主键
     * @return 结果
     */
    public int deleteReportTemplateById(Long id);
}
