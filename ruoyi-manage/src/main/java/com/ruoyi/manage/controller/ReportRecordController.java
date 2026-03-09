package com.ruoyi.manage.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.manage.domain.ReportRecord;
import com.ruoyi.manage.service.IReportRecordService;
import com.ruoyi.manage.utils.ExcelHtmlConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试报告管理 Controller
 */
@RestController
@RequestMapping("/manage/report")
public class ReportRecordController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ReportRecordController.class);

    @Resource
    private IReportRecordService reportRecordService;

    /** 查询列表 */
    @PreAuthorize("@ss.hasPermi('manage:report:list')")
    @GetMapping("/list")
    public TableDataInfo list(ReportRecord record) {
        startPage();
        List<ReportRecord> list = reportRecordService.selectReportRecordList(record);
        return getDataTable(list);
    }

    /** 获取详情 */
    @PreAuthorize("@ss.hasPermi('manage:report:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(reportRecordService.selectReportRecordById(id));
    }

    /** 新增 */
    @PreAuthorize("@ss.hasPermi('manage:report:add')")
    @Log(title = "测试报告管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ReportRecord record) {
        return toAjax(reportRecordService.insertReportRecord(record));
    }

    /** 任务表单中上传测试报告（自动生成编码，返回 id + reportCode） */
    @PreAuthorize("@ss.hasPermi('manage:report:add')")
    @Log(title = "测试报告管理", businessType = BusinessType.INSERT)
    @PostMapping("/uploadFromTask")
    public AjaxResult uploadFromTask(@RequestBody ReportRecord record) {
        int rows = reportRecordService.insertReportRecord(record);
        if (rows > 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("id", record.getId());
            result.put("reportCode", record.getReportCode());
            result.put("reportName", record.getReportName());
            result.put("storagePath", record.getStoragePath());
            return AjaxResult.success(result);
        }
        return AjaxResult.error("保存测试报告失败");
    }

    /** 修改 */
    @PreAuthorize("@ss.hasPermi('manage:report:edit')")
    @Log(title = "测试报告管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ReportRecord record) {
        return toAjax(reportRecordService.updateReportRecord(record));
    }

    /** 删除 */
    @PreAuthorize("@ss.hasPermi('manage:report:remove')")
    @Log(title = "测试报告管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(reportRecordService.deleteReportRecordByIds(ids));
    }

    /** xlsx/xls 预览（复用 ExcelHtmlConverter） */
    @PreAuthorize("@ss.hasPermi('manage:report:query')")
    @GetMapping("/preview")
    public AjaxResult preview(@RequestParam String resource) {
        try {
            if (!FileUtils.checkAllowDownload(resource)) {
                return AjaxResult.error("资源文件非法，不允许访问");
            }
            String lower = resource.toLowerCase();
            if (!lower.endsWith(".xlsx") && !lower.endsWith(".xls")) {
                return AjaxResult.error("仅支持预览 xlsx/xls 格式文件");
            }
            String filePath = RuoYiConfig.getProfile()
                    + StringUtils.substringAfter(resource, Constants.RESOURCE_PREFIX);
            String html = ExcelHtmlConverter.convertToHtml(filePath);
            return AjaxResult.success("操作成功", html);
        } catch (Exception e) {
            log.error("报告预览失败，resource={}", resource, e);
            return AjaxResult.error("文件预览失败：" + e.getMessage());
        }
    }
}
