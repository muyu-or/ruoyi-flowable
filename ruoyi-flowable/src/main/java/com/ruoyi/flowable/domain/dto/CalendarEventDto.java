package com.ruoyi.flowable.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 日历看板事件DTO
 *
 * @author claude
 * @date 2026-03-10
 */
@Data
public class CalendarEventDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 节点执行记录ID */
    private Long id;

    /** 节点key */
    private String nodeKey;

    /** 节点名称 */
    private String nodeName;

    /** 任务名称（从 ACT_HI_VARINST 中取 taskName 变量） */
    private String taskName;

    /** 班组名称 */
    private String teamName;

    /** 节点状态：pending / claimed / completed */
    private String status;

    /** 开始时间（分配班组时确定） */
    private String startTime;

    /** 完成时间 */
    private String completeTime;

    /** 计划开始日期 yyyy-MM-dd */
    private String planStartDate;

    /** 计划截止日期 yyyy-MM-dd */
    private String planEndDate;

    /** 超时标记：0=正常 1=超时 */
    private Integer timeoutFlag;

    /** 流程实例ID */
    private String procInstId;

    /** 处理耗时 */
    private String processDuration;

    /** 处理人姓名（认领人） */
    private String claimUserName;
}
