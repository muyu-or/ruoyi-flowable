package com.ruoyi.manage.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 测试报告管理对象 report_record
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ReportRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 报告唯一编码 */
    @Excel(name = "报告编码")
    private String reportCode;

    /** 报告名称 */
    @Excel(name = "报告名称")
    private String reportName;

    /** 测试类型 */
    @Excel(name = "测试类型")
    private String testType;

    /** 文件存储路径 */
    private String storagePath;

    /** 关联物料名称 */
    @Excel(name = "物料名称")
    private String materialName;

    /** 关联物料数量 */
    @Excel(name = "物料数量")
    private BigDecimal materialQuantity;

    /** 上传所在节点名称 */
    @Excel(name = "节点名称")
    private String nodeName;

    /** 实际上传人用户名（区别于 createBy 审批人） */
    @Excel(name = "上传人")
    private String uploader;
}
