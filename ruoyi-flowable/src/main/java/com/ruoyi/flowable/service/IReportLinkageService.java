package com.ruoyi.flowable.service;

import java.util.Map;

/**
 * 测试报告联动服务接口
 * 在流程节点审批通过（complete）时，将表单中暂存的测试报告元数据写入 report_record 表。
 *
 * 上传时只把文件传到服务器（/common/upload），报告元数据（storagePath、reportName、materialName 等）
 * 暂存在流程变量的 form.reports 数组中。审批通过后由本服务统一入库。
 */
public interface IReportLinkageService {

    /**
     * 根据当前完成的节点 key，从流程变量中提取 reports 数组，批量写入 report_record 表。
     * 若该节点表单中无 reports 数据则直接返回。
     *
     * @param taskDefinitionKey 当前完成的任务节点 key
     * @param allVariables      合并后的全量流程变量
     */
    void handleReportOnCompletion(String taskDefinitionKey, Map<String, Object> allVariables);
}
