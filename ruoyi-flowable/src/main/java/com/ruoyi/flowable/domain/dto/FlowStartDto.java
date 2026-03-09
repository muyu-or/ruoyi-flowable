package com.ruoyi.flowable.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

/**
 * 流程启动参数DTO - 支持班组任务分配
 *
 * @author xgh
 * @date 2026-02-28
 */
@Getter
@Setter
@ApiModel("流程启动参数")
public class FlowStartDto implements Serializable {

    @ApiModelProperty("流程定义ID")
    private String procDefId;

    @ApiModelProperty("流程定义key")
    private String procDefKey;

    @ApiModelProperty("流程变量（表单数据）")
    private Map<String, Object> variables;

    @ApiModelProperty("主班组ID（可选，流程级别的默认班组）")
    private Long mainTeamId;

    @ApiModelProperty("流程实例业务数据的业务key（用于跟踪业务记录）")
    private String businessKey;

    @ApiModelProperty("节点班组映射：key=nodeKey（BPMN节点ID），value=teamId")
    private Map<String, Long> nodeTeamMap;

    @ApiModelProperty("备注")
    private String remark;
}
