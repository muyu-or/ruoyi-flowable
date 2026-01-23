package com.ruoyi.manage.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.manage.domain.ReportTemplate;
import com.ruoyi.manage.service.IReportTemplateService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 测试报告模板配置Controller
 * 
 * @author xgh
 * @date 2026-01-23
 */
@RestController
@RequestMapping("/manage/template")
public class ReportTemplateController extends BaseController
{
    @Autowired
    private IReportTemplateService reportTemplateService;

    /**
     * 查询测试报告模板配置列表
     */
    @PreAuthorize("@ss.hasPermi('manage:template:list')")
    @GetMapping("/list")
    public TableDataInfo list(ReportTemplate reportTemplate)
    {
        startPage();
        List<ReportTemplate> list = reportTemplateService.selectReportTemplateList(reportTemplate);
        return getDataTable(list);
    }

    /**
     * 导出测试报告模板配置列表
     */
    @PreAuthorize("@ss.hasPermi('manage:template:export')")
    @Log(title = "测试报告模板配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ReportTemplate reportTemplate)
    {
        List<ReportTemplate> list = reportTemplateService.selectReportTemplateList(reportTemplate);
        ExcelUtil<ReportTemplate> util = new ExcelUtil<ReportTemplate>(ReportTemplate.class);
        util.exportExcel(response, list, "测试报告模板配置数据");
    }

    /**
     * 获取测试报告模板配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('manage:template:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(reportTemplateService.selectReportTemplateById(id));
    }

    /**
     * 新增测试报告模板配置
     */
    @PreAuthorize("@ss.hasPermi('manage:template:add')")
    @Log(title = "测试报告模板配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ReportTemplate reportTemplate)
    {
        return toAjax(reportTemplateService.insertReportTemplate(reportTemplate));
    }

    /**
     * 修改测试报告模板配置
     */
    @PreAuthorize("@ss.hasPermi('manage:template:edit')")
    @Log(title = "测试报告模板配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ReportTemplate reportTemplate)
    {
        return toAjax(reportTemplateService.updateReportTemplate(reportTemplate));
    }

    /**
     * 删除测试报告模板配置
     */
    @PreAuthorize("@ss.hasPermi('manage:template:remove')")
    @Log(title = "测试报告模板配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(reportTemplateService.deleteReportTemplateByIds(ids));
    }
}
