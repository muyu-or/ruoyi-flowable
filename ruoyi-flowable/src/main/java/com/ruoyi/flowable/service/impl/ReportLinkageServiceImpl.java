package com.ruoyi.flowable.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.ruoyi.flowable.service.IReportLinkageService;
import com.ruoyi.manage.domain.ReportRecord;
import com.ruoyi.manage.service.IReportRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 测试报告联动服务实现
 * 审批通过后，从当前节点表单的 reports 数组中提取报告元数据，写入 report_record 表。
 */
@Slf4j
@Service
public class ReportLinkageServiceImpl implements IReportLinkageService {

    @Resource
    private IReportRecordService reportRecordService;

    @Override
    public void handleReportOnCompletion(String taskDefinitionKey, Map<String, Object> allVariables) {
        String nsKey = taskDefinitionKey + "__formData";
        Object raw = allVariables.get(nsKey);
        if (raw == null) {
            return;
        }

        // 提取表单数据
        Map<String, Object> formData = toMap(raw);
        if (formData.isEmpty()) {
            return;
        }

        // 取出 reports 数组
        Object reportsRaw = formData.get("reports");
        if (reportsRaw == null) {
            return;
        }

        List<Map<String, Object>> reportsList = toReportList(reportsRaw);
        if (reportsList.isEmpty()) {
            return;
        }

        log.info("节点 {} 审批通过，写入 {} 条测试报告记录", taskDefinitionKey, reportsList.size());

        for (Map<String, Object> rpt : reportsList) {
            try {
                ReportRecord record = new ReportRecord();
                record.setStoragePath(toStr(rpt.get("storagePath")));
                record.setReportName(toStr(rpt.get("reportName")));
                record.setMaterialName(toStr(rpt.get("materialName")));
                record.setNodeName(toStr(rpt.get("nodeName")));
                record.setUploader(toStr(rpt.get("uploader")));

                Object qty = rpt.get("materialQuantity");
                if (qty != null) {
                    try {
                        record.setMaterialQuantity(new BigDecimal(qty.toString()));
                    } catch (NumberFormatException e) {
                        log.warn("materialQuantity 转换失败: {}", qty);
                    }
                }

                // insertReportRecord 内部自动生成 reportCode + createBy + createTime
                reportRecordService.insertReportRecord(record);
            } catch (Exception e) {
                log.error("写入测试报告记录失败，节点={}, report={}", taskDefinitionKey, rpt, e);
                // 不抛异常，单条失败不影响整体审批流程
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object raw) {
        if (raw instanceof Map) {
            return (Map<String, Object>) raw;
        }
        if (raw instanceof String) {
            try {
                return JSONObject.parseObject((String) raw, new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                log.warn("解析表单数据失败", e);
                return Collections.emptyMap();
            }
        }
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> toReportList(Object reportsRaw) {
        if (reportsRaw instanceof List) {
            return (List<Map<String, Object>>) reportsRaw;
        }
        if (reportsRaw instanceof String) {
            try {
                return JSON.parseObject((String) reportsRaw, new TypeReference<List<Map<String, Object>>>() {});
            } catch (Exception e) {
                log.warn("解析 reports 数组失败", e);
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    private String toStr(Object val) {
        return val == null ? "" : val.toString().trim();
    }
}
