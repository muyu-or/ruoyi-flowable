package com.ruoyi.flowable.domain.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 首页统计信息 DTO
 *
 * @author claude
 * @date 2026-03-04
 */
public class HomeStatDto {
    // ① 所有角色可见
    private MyStatsDto myStats;

    // ② admin + leader 可见
    private List<StockTrendPoint> stockTrend;
    private List<InventoryCategoryDto> inventoryOverview;

    // ⑧ admin 专属（库存成本统计）
    private List<CostByCategoryDto> costByCategory;
    private List<StockAmountTrendPoint> stockAmountTrend;

    // ③ admin 可见 / leader 看自己班组
    private List<TeamProgressDto> teamProgress;

    // ④ leader + 成员 可见
    private TeamEfficiencyDto myTeamEfficiency;
    private List<MemberStatDto> memberStats;

    // ⑤ leader + 成员 可见（最近完成任务）
    private List<RecentTaskDto> recentTasks = new ArrayList<>();

    // ⑥ admin 专属（全员任务汇总统计，供 BI 大屏指标卡片使用）
    private MyStatsDto companyStats;

    // ⑦ admin 专属（个人完成数量 Top5，供 BI 大屏使用）
    private List<UserTopDto> userTop5;

    // ⑨ admin 专属（BI 大屏新增指标）
    private List<WarningStatDto> warningStats;
    private CostSummaryDto costSummary;
    private List<NodeBottleneckDto> nodeBottleneck;
    private List<TeamStabilityDto> teamStability;
    private List<NodeStatusSummaryDto> nodeStatusSummary;
    private MaterialConversionDto materialConversion;
    private List<MaterialConversionTrendPoint> materialConversionTrend;
    private RealtimeStatusDto realtimeStatus;

    // ⑩ 物料子类数量（库存中不同 material_subcategory 的种类数）
    private int subcategoryCount;

    // Getters/Setters
    public MyStatsDto getMyStats() { return myStats; }
    public void setMyStats(MyStatsDto myStats) { this.myStats = myStats; }
    public List<StockTrendPoint> getStockTrend() { return stockTrend; }
    public void setStockTrend(List<StockTrendPoint> stockTrend) { this.stockTrend = stockTrend; }
    public List<InventoryCategoryDto> getInventoryOverview() { return inventoryOverview; }
    public void setInventoryOverview(List<InventoryCategoryDto> inventoryOverview) { this.inventoryOverview = inventoryOverview; }
    public List<TeamProgressDto> getTeamProgress() { return teamProgress; }
    public void setTeamProgress(List<TeamProgressDto> teamProgress) { this.teamProgress = teamProgress; }
    public TeamEfficiencyDto getMyTeamEfficiency() { return myTeamEfficiency; }
    public void setMyTeamEfficiency(TeamEfficiencyDto myTeamEfficiency) { this.myTeamEfficiency = myTeamEfficiency; }
    public List<MemberStatDto> getMemberStats() { return memberStats; }
    public void setMemberStats(List<MemberStatDto> memberStats) { this.memberStats = memberStats; }
    public List<RecentTaskDto> getRecentTasks() { return recentTasks; }
    public void setRecentTasks(List<RecentTaskDto> recentTasks) { this.recentTasks = recentTasks; }
    public MyStatsDto getCompanyStats() { return companyStats; }
    public void setCompanyStats(MyStatsDto companyStats) { this.companyStats = companyStats; }
    public List<UserTopDto> getUserTop5() { return userTop5; }
    public void setUserTop5(List<UserTopDto> userTop5) { this.userTop5 = userTop5; }
    public List<CostByCategoryDto> getCostByCategory() { return costByCategory; }
    public void setCostByCategory(List<CostByCategoryDto> costByCategory) { this.costByCategory = costByCategory; }
    public List<StockAmountTrendPoint> getStockAmountTrend() { return stockAmountTrend; }
    public void setStockAmountTrend(List<StockAmountTrendPoint> stockAmountTrend) { this.stockAmountTrend = stockAmountTrend; }
    public List<WarningStatDto> getWarningStats() { return warningStats; }
    public void setWarningStats(List<WarningStatDto> warningStats) { this.warningStats = warningStats; }
    public CostSummaryDto getCostSummary() { return costSummary; }
    public void setCostSummary(CostSummaryDto costSummary) { this.costSummary = costSummary; }
    public List<NodeBottleneckDto> getNodeBottleneck() { return nodeBottleneck; }
    public void setNodeBottleneck(List<NodeBottleneckDto> nodeBottleneck) { this.nodeBottleneck = nodeBottleneck; }
    public List<TeamStabilityDto> getTeamStability() { return teamStability; }
    public void setTeamStability(List<TeamStabilityDto> teamStability) { this.teamStability = teamStability; }
    public List<NodeStatusSummaryDto> getNodeStatusSummary() { return nodeStatusSummary; }
    public void setNodeStatusSummary(List<NodeStatusSummaryDto> nodeStatusSummary) { this.nodeStatusSummary = nodeStatusSummary; }
    public MaterialConversionDto getMaterialConversion() { return materialConversion; }
    public void setMaterialConversion(MaterialConversionDto materialConversion) { this.materialConversion = materialConversion; }
    public List<MaterialConversionTrendPoint> getMaterialConversionTrend() { return materialConversionTrend; }
    public void setMaterialConversionTrend(List<MaterialConversionTrendPoint> materialConversionTrend) { this.materialConversionTrend = materialConversionTrend; }
    public RealtimeStatusDto getRealtimeStatus() { return realtimeStatus; }
    public void setRealtimeStatus(RealtimeStatusDto realtimeStatus) { this.realtimeStatus = realtimeStatus; }
    public int getSubcategoryCount() { return subcategoryCount; }
    public void setSubcategoryCount(int subcategoryCount) { this.subcategoryCount = subcategoryCount; }

    // MyStatsDto（复用 DashboardStatsDto.MyStatsDto 或重新定义）
    public static class MyStatsDto {
        private long pending;    // 待办
        private long running;    // 进行中（claimed）
        private long finished;   // 已完成
        private long rejected;   // 失败
        private long total;

        // Getters/Setters
        public long getPending() { return pending; }
        public void setPending(long pending) { this.pending = pending; }
        public long getRunning() { return running; }
        public void setRunning(long running) { this.running = running; }
        public long getFinished() { return finished; }
        public void setFinished(long finished) { this.finished = finished; }
        public long getRejected() { return rejected; }
        public void setRejected(long rejected) { this.rejected = rejected; }
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
    }

    // StockTrendPoint（入库/出库趋势数据点）
    public static class StockTrendPoint {
        private String label;      // X轴标签（如 "03-01"、"3月"、"第1周"）
        private Long inboundQty;   // 入库数量
        private Long outboundQty;  // 出库数量

        // Getters/Setters
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public Long getInboundQty() { return inboundQty; }
        public void setInboundQty(Long inboundQty) { this.inboundQty = inboundQty; }
        public Long getOutboundQty() { return outboundQty; }
        public void setOutboundQty(Long outboundQty) { this.outboundQty = outboundQty; }
    }

    // TeamCompletionDto（班组任务完成率）
    public static class TeamCompletionDto {
        private String teamName;         // 班组名称
        private Long finishedCount;      // 已完成节点数
        private Long totalCount;         // 总节点数
        private Double completionRate;   // 完成率 0.0~1.0

        // Getters/Setters
        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }
        public Long getFinishedCount() { return finishedCount; }
        public void setFinishedCount(Long finishedCount) { this.finishedCount = finishedCount; }
        public Long getTotalCount() { return totalCount; }
        public void setTotalCount(Long totalCount) { this.totalCount = totalCount; }
        public Double getCompletionRate() { return completionRate; }
        public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }
    }

    // InventoryCategoryDto（库存总览饼图数据）
    public static class InventoryCategoryDto {
        private String category;    // 物料大类（字典值）
        private Long totalQty;      // 数量合计

        // Getters/Setters
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public Long getTotalQty() { return totalQty; }
        public void setTotalQty(Long totalQty) { this.totalQty = totalQty; }
    }

    // TeamProgressDto（班组进度，复用 DashboardStatsDto.TeamStatsDto 或重新定义）
    public static class TeamProgressDto {
        private Long teamId;
        private String teamName;
        private Integer memberCount;
        private Long pending;
        private Long running;
        private Long finished;
        private Long rejected;
        private Long total;

        // Getters/Setters
        public Long getTeamId() { return teamId; }
        public void setTeamId(Long teamId) { this.teamId = teamId; }
        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }
        public Integer getMemberCount() { return memberCount; }
        public void setMemberCount(Integer memberCount) { this.memberCount = memberCount; }
        public Long getPending() { return pending; }
        public void setPending(Long pending) { this.pending = pending; }
        public Long getRunning() { return running; }
        public void setRunning(Long running) { this.running = running; }
        public Long getFinished() { return finished; }
        public void setFinished(Long finished) { this.finished = finished; }
        public Long getRejected() { return rejected; }
        public void setRejected(Long rejected) { this.rejected = rejected; }
        public Long getTotal() { return total; }
        public void setTotal(Long total) { this.total = total; }
    }

    // TeamEfficiencyDto（我的班组效率）
    public static class TeamEfficiencyDto {
        private Long teamId;
        private String teamName;
        private Double avgDurationHours;
        private Long finishedCount;
        private Long totalCount;
        private Double completionRate;  // finished / total，0~1

        // Getters/Setters
        public Long getTeamId() { return teamId; }
        public void setTeamId(Long teamId) { this.teamId = teamId; }
        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }
        public Double getAvgDurationHours() { return avgDurationHours; }
        public void setAvgDurationHours(Double avgDurationHours) { this.avgDurationHours = avgDurationHours; }
        public Long getFinishedCount() { return finishedCount; }
        public void setFinishedCount(Long finishedCount) { this.finishedCount = finishedCount; }
        public Long getTotalCount() { return totalCount; }
        public void setTotalCount(Long totalCount) { this.totalCount = totalCount; }
        public Double getCompletionRate() { return completionRate; }
        public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }
    }

    // MemberStatDto（班组成员统计）
    public static class MemberStatDto {
        private Long userId;
        private String userName;
        private Long pending;
        private Long running;
        private Long finished;
        private Long total;

        // Getters/Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public Long getPending() { return pending; }
        public void setPending(Long pending) { this.pending = pending; }
        public Long getRunning() { return running; }
        public void setRunning(Long running) { this.running = running; }
        public Long getFinished() { return finished; }
        public void setFinished(Long finished) { this.finished = finished; }
        public Long getTotal() { return total; }
        public void setTotal(Long total) { this.total = total; }
    }

    // RecentTaskDto（最近完成任务）
    public static class RecentTaskDto {
        private String nodeName;        // 节点名称（如"检测"）
        private String procName;        // 流程备注（如"光纤跳线加工"）
        private String completeTime;    // 完成时间（格式化字符串）
        private Long   processDuration; // 耗时（秒）
        private String claimUserName;   // 完成人昵称
        private String procInstId;      // 流程实例ID（用于跳转）

        // Getters/Setters
        public String getNodeName() { return nodeName; }
        public void setNodeName(String nodeName) { this.nodeName = nodeName; }
        public String getProcName() { return procName; }
        public void setProcName(String procName) { this.procName = procName; }
        public String getCompleteTime() { return completeTime; }
        public void setCompleteTime(String completeTime) { this.completeTime = completeTime; }
        public Long getProcessDuration() { return processDuration; }
        public void setProcessDuration(Long processDuration) { this.processDuration = processDuration; }
        public String getClaimUserName() { return claimUserName; }
        public void setClaimUserName(String claimUserName) { this.claimUserName = claimUserName; }
        public String getProcInstId() { return procInstId; }
        public void setProcInstId(String procInstId) { this.procInstId = procInstId; }
    }

    // CostByCategoryDto（按物料大类汇总库存成本）
    public static class CostByCategoryDto {
        private String category;
        private Double totalCost;

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public Double getTotalCost() { return totalCost; }
        public void setTotalCost(Double totalCost) { this.totalCost = totalCost; }
    }

    // StockAmountTrendPoint（按时间段入库金额趋势）
    public static class StockAmountTrendPoint {
        private String label;
        private Double inboundAmount;

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public Double getInboundAmount() { return inboundAmount; }
        public void setInboundAmount(Double inboundAmount) { this.inboundAmount = inboundAmount; }
    }

    // UserTopDto（个人完成数量排名）
    public static class UserTopDto {
        private Long userId;
        private String userName;
        private Long finished;

        // Getters/Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public Long getFinished() { return finished; }
        public void setFinished(Long finished) { this.finished = finished; }
    }

    // WarningStatDto（预警按节点统计）
    public static class WarningStatDto {
        private String nodeName;
        private int warningCount;
        private int unresolvedCount;
        private double avgResponseMinutes;

        public String getNodeName() { return nodeName; }
        public void setNodeName(String nodeName) { this.nodeName = nodeName; }
        public int getWarningCount() { return warningCount; }
        public void setWarningCount(int warningCount) { this.warningCount = warningCount; }
        public int getUnresolvedCount() { return unresolvedCount; }
        public void setUnresolvedCount(int unresolvedCount) { this.unresolvedCount = unresolvedCount; }
        public double getAvgResponseMinutes() { return avgResponseMinutes; }
        public void setAvgResponseMinutes(double avgResponseMinutes) { this.avgResponseMinutes = avgResponseMinutes; }
    }

    // CostSummaryDto（成本汇总）
    public static class CostSummaryDto {
        private BigDecimal totalInventoryCost;
        private BigDecimal avgUnitCost;
        private BigDecimal totalStockInAmount;

        public BigDecimal getTotalInventoryCost() { return totalInventoryCost; }
        public void setTotalInventoryCost(BigDecimal totalInventoryCost) { this.totalInventoryCost = totalInventoryCost; }
        public BigDecimal getAvgUnitCost() { return avgUnitCost; }
        public void setAvgUnitCost(BigDecimal avgUnitCost) { this.avgUnitCost = avgUnitCost; }
        public BigDecimal getTotalStockInAmount() { return totalStockInAmount; }
        public void setTotalStockInAmount(BigDecimal totalStockInAmount) { this.totalStockInAmount = totalStockInAmount; }
    }

    // NodeBottleneckDto（节点瓶颈 P50/P90）
    public static class NodeBottleneckDto {
        private String nodeName;
        private long p50Seconds;
        private long p90Seconds;

        public String getNodeName() { return nodeName; }
        public void setNodeName(String nodeName) { this.nodeName = nodeName; }
        public long getP50Seconds() { return p50Seconds; }
        public void setP50Seconds(long p50Seconds) { this.p50Seconds = p50Seconds; }
        public long getP90Seconds() { return p90Seconds; }
        public void setP90Seconds(long p90Seconds) { this.p90Seconds = p90Seconds; }
    }

    // TeamStabilityDto（班组效率统计）
    public static class TeamStabilityDto {
        private String teamName;
        private double meanSeconds;
        private long completedCount;
        private double onTimeRate;

        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }
        public double getMeanSeconds() { return meanSeconds; }
        public void setMeanSeconds(double meanSeconds) { this.meanSeconds = meanSeconds; }
        public long getCompletedCount() { return completedCount; }
        public void setCompletedCount(long completedCount) { this.completedCount = completedCount; }
        public double getOnTimeRate() { return onTimeRate; }
        public void setOnTimeRate(double onTimeRate) { this.onTimeRate = onTimeRate; }
    }

    // NodeStatusSummaryDto（节点状态汇总）
    public static class NodeStatusSummaryDto {
        private String nodeName;
        private long activeCount;
        private long completedCount;

        public String getNodeName() { return nodeName; }
        public void setNodeName(String nodeName) { this.nodeName = nodeName; }
        public long getActiveCount() { return activeCount; }
        public void setActiveCount(long activeCount) { this.activeCount = activeCount; }
        public long getCompletedCount() { return completedCount; }
        public void setCompletedCount(long completedCount) { this.completedCount = completedCount; }
    }

    // MaterialConversionDto（产线物料/产品转化统计）
    public static class MaterialConversionDto {
        private long rawInboundQty;
        private long productInboundQty;
        private double conversionRate;

        public long getRawInboundQty() { return rawInboundQty; }
        public void setRawInboundQty(long rawInboundQty) { this.rawInboundQty = rawInboundQty; }
        public long getProductInboundQty() { return productInboundQty; }
        public void setProductInboundQty(long productInboundQty) { this.productInboundQty = productInboundQty; }
        public double getConversionRate() { return conversionRate; }
        public void setConversionRate(double conversionRate) { this.conversionRate = conversionRate; }
    }

    // MaterialConversionTrendPoint（物料/产品转化趋势）
    public static class MaterialConversionTrendPoint {
        private String label;
        private long rawInboundQty;
        private long productInboundQty;
        private double conversionRate;

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public long getRawInboundQty() { return rawInboundQty; }
        public void setRawInboundQty(long rawInboundQty) { this.rawInboundQty = rawInboundQty; }
        public long getProductInboundQty() { return productInboundQty; }
        public void setProductInboundQty(long productInboundQty) { this.productInboundQty = productInboundQty; }
        public double getConversionRate() { return conversionRate; }
        public void setConversionRate(double conversionRate) { this.conversionRate = conversionRate; }
    }

    // RealtimeStatusDto（大屏实时对接状态）
    public static class RealtimeStatusDto {
        private String serverTime;
        private int refreshIntervalSeconds;
        private String dataScope;

        public String getServerTime() { return serverTime; }
        public void setServerTime(String serverTime) { this.serverTime = serverTime; }
        public int getRefreshIntervalSeconds() { return refreshIntervalSeconds; }
        public void setRefreshIntervalSeconds(int refreshIntervalSeconds) { this.refreshIntervalSeconds = refreshIntervalSeconds; }
        public String getDataScope() { return dataScope; }
        public void setDataScope(String dataScope) { this.dataScope = dataScope; }
    }
}
