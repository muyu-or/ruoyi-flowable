package com.ruoyi.manage.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.manage.mapper.ReportTemplateMapper;
import com.ruoyi.manage.domain.ReportTemplate;
import com.ruoyi.manage.service.IReportTemplateService;

/**
 * 测试报告模板配置Service业务层处理
 * 
 * @author xgh
 * @date 2026-01-23
 */
@Service
public class ReportTemplateServiceImpl implements IReportTemplateService 
{
    @Autowired
    private ReportTemplateMapper reportTemplateMapper;

    /**
     * 查询测试报告模板配置
     * 
     * @param id 测试报告模板配置主键
     * @return 测试报告模板配置
     */
    @Override
    public ReportTemplate selectReportTemplateById(Long id)
    {
        return reportTemplateMapper.selectReportTemplateById(id);
    }

    /**
     * 查询测试报告模板配置列表
     * 
     * @param reportTemplate 测试报告模板配置
     * @return 测试报告模板配置
     */
    @Override
    public List<ReportTemplate> selectReportTemplateList(ReportTemplate reportTemplate)
    {
        return reportTemplateMapper.selectReportTemplateList(reportTemplate);
    }

    /**
     * 新增测试报告模板配置
     * 
     * @param reportTemplate 测试报告模板配置
     * @return 结果
     */
    @Override
    public int insertReportTemplate(ReportTemplate reportTemplate)
    {
        reportTemplate.setCreateTime(DateUtils.getNowDate());
        return reportTemplateMapper.insertReportTemplate(reportTemplate);
    }

    /**
     * 修改测试报告模板配置
     * 
     * @param reportTemplate 测试报告模板配置
     * @return 结果
     */
    @Override
    public int updateReportTemplate(ReportTemplate reportTemplate)
    {
        reportTemplate.setUpdateTime(DateUtils.getNowDate());
        return reportTemplateMapper.updateReportTemplate(reportTemplate);
    }

    /**
     * 批量删除测试报告模板配置
     * 
     * @param ids 需要删除的测试报告模板配置主键
     * @return 结果
     */
    @Override
    public int deleteReportTemplateByIds(Long[] ids)
    {
        return reportTemplateMapper.deleteReportTemplateByIds(ids);
    }

    /**
     * 删除测试报告模板配置信息
     * 
     * @param id 测试报告模板配置主键
     * @return 结果
     */
    @Override
    public int deleteReportTemplateById(Long id)
    {
        return reportTemplateMapper.deleteReportTemplateById(id);
    }
}
