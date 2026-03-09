package com.ruoyi.flowable.domain.dto;

import com.alibaba.fastjson2.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 流程执行汇总DTO - 包含完整的执行记录和节点数据
 *
 * @author xgh
 * @date 2026-02-28
 */
@Getter
@Setter
@ApiModel("流程执行汇总")
public class FlowExecutionSummaryDto implements Serializable {

    @ApiModelProperty("执行记录ID")
    private Long execRecordId;

    @ApiModelProperty("流程实例ID")
    private String procInstId;

    @ApiModelProperty("流程定义key")
    private String procDefKey;

    @ApiModelProperty("流程版本")
    private Integer procDefVersion;

    @ApiModelProperty("发起人ID")
    private Long initiatorId;

    @ApiModelProperty("发起人名称")
    private String initiatorName;

    @ApiModelProperty("主班组ID")
    private Long mainTeamId;

    @ApiModelProperty("主班组名称")
    private String mainTeamName;

    @ApiModelProperty("流程状态")
    private String status;

    @ApiModelProperty("创建时间")
    private String createTime;

    @ApiModelProperty("更新时间")
    private String updateTime;

    @ApiModelProperty("节点执行记录列表")
    private List<NodeExecutionSummaryDto> nodeExecutions;

    @ApiModelProperty("汇总数据（所有节点表单数据的聚合）")
    private Map<String, Object> summaryData;

    @ApiModelProperty("备注")
    private String remark;

    /**
     * 节点执行汇总
     */
    @Getter
    @Setter
    @ApiModel("节点执行汇总")
    public static class NodeExecutionSummaryDto implements Serializable {

        @ApiModelProperty("节点执行记录ID")
        private Long nodeExecId;

        @ApiModelProperty("Flowable任务ID")
        private String taskId;

        @ApiModelProperty("节点key")
        private String nodeKey;

        @ApiModelProperty("节点名称")
        private String nodeName;

        @ApiModelProperty("执行班组ID")
        private Long assignedTeamId;

        @ApiModelProperty("执行班组名称")
        private String assignedTeamName;

        @ApiModelProperty("认领人ID")
        private Long claimUserId;

        @ApiModelProperty("认领人名称")
        private String claimUserName;

        @ApiModelProperty("任务状态")
        private String status;

        @ApiModelProperty("开始时间")
        private String startTime;

        @ApiModelProperty("认领时间")
        private String claimTime;

        @ApiModelProperty("完成时间")
        private String completeTime;

        @ApiModelProperty("表单数据（JSON）")
        private JSONObject formData;
    }
}
