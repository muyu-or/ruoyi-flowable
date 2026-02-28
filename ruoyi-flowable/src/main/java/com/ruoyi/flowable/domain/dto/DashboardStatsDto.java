package com.ruoyi.flowable.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 任务统计 Dashboard DTO
 */
@Data
@ApiModel("任务统计看板数据")
public class DashboardStatsDto implements Serializable {

    @ApiModelProperty("个人任务统计")
    private MyStatsDto myStats;

    @ApiModelProperty("是否是班组长")
    private Boolean isLeader;

    @ApiModelProperty("班组任务汇总（仅班组长有值）")
    private TeamStatsDto teamStats;

    @Data
    public static class MyStatsDto implements Serializable {
        @ApiModelProperty("待办数量（pending）")
        private long pending;
        @ApiModelProperty("进行中数量（claimed）")
        private long running;
        @ApiModelProperty("已完成数量（completed）")
        private long finished;
        @ApiModelProperty("失败数量（rejected）")
        private long rejected;
        @ApiModelProperty("总计")
        private long total;
    }

    @Data
    public static class TeamStatsDto implements Serializable {
        @ApiModelProperty("班组ID")
        private Long teamId;
        @ApiModelProperty("班组名称")
        private String teamName;
        @ApiModelProperty("成员数量")
        private int memberCount;
        @ApiModelProperty("待办数量")
        private long pending;
        @ApiModelProperty("进行中数量")
        private long running;
        @ApiModelProperty("已完成数量")
        private long finished;
        @ApiModelProperty("失败数量")
        private long rejected;
        @ApiModelProperty("总计")
        private long total;
    }
}
