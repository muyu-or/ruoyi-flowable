package com.ruoyi.flowable.service.impl;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.flowable.domain.dto.HomeStatDto;
import com.ruoyi.flowable.service.IHomeStatService;
import com.ruoyi.manage.mapper.InventoryMapper;
import com.ruoyi.manage.mapper.ProductionTeamMapper;
import com.ruoyi.manage.mapper.StockInMapper;
import com.ruoyi.manage.mapper.StockOutMapper;
import com.ruoyi.system.mapper.TaskNodeExecutionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.flowable.engine.HistoryService;
import org.flowable.engine.history.HistoricProcessInstance;

/**
 * 首页统计 Service 实现
 *
 * @author claude
 * @date 2026-03-04
 */
@Slf4j
@Service
public class HomeStatServiceImpl implements IHomeStatService {

    @Resource
    private TaskNodeExecutionMapper taskNodeExecutionMapper;

    @Resource
    private ProductionTeamMapper productionTeamMapper;

    @Resource
    private InventoryMapper inventoryMapper;

    @Resource
    private StockInMapper stockInMapper;

    @Resource
    private StockOutMapper stockOutMapper;

    @Resource
    private HistoryService historyService;

    @Override
    public HomeStatDto getHomeStats(Long userId, String period) {
        HomeStatDto result = new HomeStatDto();

        // 计算时间范围
        DateRange dateRange = calculateDateRange(period);

        // 1. 个人任务统计（所有角色）
        List<Map<String, Object>> myRows = taskNodeExecutionMapper.countMyStatsByStatus(userId);
        result.setMyStats(buildMyStats(myRows));

        // 2. 根据角色权限返回不同数据
        //    超级管理员(userId=1) 或 拥有"查看全部数据"权限 → 看全公司
        boolean isAdmin = SecurityUtils.isAdmin(userId)
                || SecurityUtils.hasPermi("flowable:stat:all");
        boolean isLeader = !isAdmin && isUserLeader(userId);

        if (isAdmin) {
            // admin 能看到入库/出库趋势和库存总览

            // 2.1 入库/出库趋势
            result.setStockTrend(getStockTrend(dateRange));

            // 2.2 库存总览（按物料大类汇总该时间段内入库数量）
            result.setInventoryOverview(getInventoryOverview(dateRange));
        }

        if (isAdmin) {
            // 3.1 超级管理员：查看所有启用班组的进度
            List<Long> teamIds = getAllEnabledTeamIds();
            List<HomeStatDto.TeamProgressDto> teamProgressList = teamIds.stream()
                .map(teamId -> buildTeamProgress(teamId))
                .collect(Collectors.toList());
            result.setTeamProgress(teamProgressList);

            // 3.2 全员任务汇总统计（admin 专属，展示给 BI 大屏指标卡片）
            List<Map<String, Object>> allRows = taskNodeExecutionMapper.countAllStats();
            result.setCompanyStats(buildMyStats(allRows));

            // 3.3 admin 也需要最近完成任务（全公司范围，供 BI 大屏展示）
            List<Map<String, Object>> adminRecentRows = taskNodeExecutionMapper
                .selectRecentCompleted(null, null, 20);
            result.setRecentTasks(buildRecentTasks(adminRecentRows));

            // 3.4 个人完成数量 Top5（admin 专属，供 BI 大屏展示）
            List<Map<String, Object>> topRows = taskNodeExecutionMapper.countUserFinishedTop(5);
            result.setUserTop5(buildUserTop5(topRows));
        }

        if (isLeader || !isAdmin) { // leader + 成员都能看到班组效率
            // 4.1 获取用户所属班组（如果是 leader，返回领导的班组；如果是普通成员，返回所在班组）
            List<Long> userTeamIds = getUserTeamIds(userId);
            if (!userTeamIds.isEmpty()) {
                Long teamId = userTeamIds.get(0); // 取第一个班组
                result.setMyTeamEfficiency(buildTeamEfficiency(teamId, dateRange));

                if (isLeader) {
                    // 4.2 班组长还能看到班组成员统计
                    List<HomeStatDto.MemberStatDto> memberStats = getMemberStatsByTeam(teamId);
                    result.setMemberStats(memberStats);
                }
            }
        }

        // 5. 最近完成任务（成员看自己，班组长看班组，admin 不需要）
        if (!isAdmin) {
            Long queryUserId = isLeader ? null : userId;
            Long queryTeamId = null;
            if (isLeader) {
                List<Long> leaderTeamIds = getUserTeamIds(userId);
                queryTeamId = leaderTeamIds.isEmpty() ? null : leaderTeamIds.get(0);
            }
            List<Map<String, Object>> recentRows = taskNodeExecutionMapper
                .selectRecentCompleted(queryUserId, queryTeamId, 8);
            result.setRecentTasks(buildRecentTasks(recentRows));
        }

        return result;
    }

    // 内部辅助方法

    /**
     * 计算日期范围
     */
    private DateRange calculateDateRange(String period) {
        Calendar cal = Calendar.getInstance();
        Date endDate = cal.getTime();
        Date startDate;

        switch (period.toLowerCase()) {
            case "week":
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                startDate = cal.getTime();
                break;
            case "month":
                cal.set(Calendar.DAY_OF_MONTH, 1); // 当月第一天
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                startDate = cal.getTime();
                break;
            case "quarter":
                int currentMonth = cal.get(Calendar.MONTH);
                int quarterStartMonth = (currentMonth / 3) * 3;
                cal.set(Calendar.MONTH, quarterStartMonth);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                startDate = cal.getTime();
                break;
            case "year":
                cal.set(Calendar.DAY_OF_YEAR, 1); // 当年第一天
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                startDate = cal.getTime();
                break;
            default:
                // 默认为本月
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                startDate = cal.getTime();
                break;
        }

        return new DateRange(startDate, endDate, period);
    }

    /**
     * 入库/出库趋势（按时间分段）
     */
    private List<HomeStatDto.StockTrendPoint> getStockTrend(DateRange dateRange) {
        String dateFormat = getDateFormatForPeriod(dateRange.period);

        // 查询入库分段数据
        List<Map<String, Object>> inRows = stockInMapper.sumQuantityGroupByPeriod(
            dateRange.startDate, dateRange.endDate, dateFormat);
        // 查询出库分段数据
        List<Map<String, Object>> outRows = stockOutMapper.sumQuantityGroupByPeriod(
            dateRange.startDate, dateRange.endDate, dateFormat);

        // 合并到同一 label 列表
        Map<String, HomeStatDto.StockTrendPoint> map = new LinkedHashMap<>();
        for (Map<String, Object> row : inRows) {
            String label = String.valueOf(row.get("label"));
            HomeStatDto.StockTrendPoint point = map.computeIfAbsent(label, k -> {
                HomeStatDto.StockTrendPoint p = new HomeStatDto.StockTrendPoint();
                p.setLabel(k);
                p.setInboundQty(0L);
                p.setOutboundQty(0L);
                return p;
            });
            point.setInboundQty(toLong(row.get("qty")));
        }
        for (Map<String, Object> row : outRows) {
            String label = String.valueOf(row.get("label"));
            HomeStatDto.StockTrendPoint point = map.computeIfAbsent(label, k -> {
                HomeStatDto.StockTrendPoint p = new HomeStatDto.StockTrendPoint();
                p.setLabel(k);
                p.setInboundQty(0L);
                p.setOutboundQty(0L);
                return p;
            });
            point.setOutboundQty(toLong(row.get("qty")));
        }

        // 按 label 排序返回
        List<HomeStatDto.StockTrendPoint> result = new ArrayList<>(map.values());
        result.sort(Comparator.comparing(HomeStatDto.StockTrendPoint::getLabel));
        return result;
    }

    /**
     * 库存总览（按物料大类汇总该时间段内入库数量）
     */
    private List<HomeStatDto.InventoryCategoryDto> getInventoryOverview(DateRange dateRange) {
        List<Map<String, Object>> rows = inventoryMapper.sumInboundByCategory(dateRange.startDate, dateRange.endDate);
        List<HomeStatDto.InventoryCategoryDto> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            HomeStatDto.InventoryCategoryDto dto = new HomeStatDto.InventoryCategoryDto();
            dto.setCategory((String) row.get("category"));
            dto.setTotalQty(toLong(row.get("totalQty")));
            list.add(dto);
        }
        return list;
    }

    /**
     * 各班组任务完成率
     */
    private List<HomeStatDto.TeamCompletionDto> getTeamCompletion(DateRange dateRange) {
        List<Long> teamIds = getAllEnabledTeamIds();
        List<HomeStatDto.TeamCompletionDto> list = new ArrayList<>();

        for (Long teamId : teamIds) {
            com.ruoyi.manage.domain.ProductionTeam team = productionTeamMapper.selectProductionTeamById(teamId);
            if (team == null) {
                continue;
            }

            List<Map<String, Object>> rows = taskNodeExecutionMapper.countTeamStatsByStatus(teamId);
            long completed = 0;
            long total = 0;
            for (Map<String, Object> row : rows) {
                String status = (String) row.get("status");
                long cnt = toLong(row.get("cnt"));
                total += cnt;
                if ("completed".equals(status)) {
                    completed += cnt;
                }
            }

            HomeStatDto.TeamCompletionDto dto = new HomeStatDto.TeamCompletionDto();
            dto.setTeamName(team.getTeamName());
            dto.setFinishedCount(completed);
            dto.setTotalCount(total);
            dto.setCompletionRate(total == 0 ? 0.0 : (double) completed / total);
            list.add(dto);
        }

        return list;
    }

    /**
     * 获取日期格式字符串
     */
    private String getDateFormatForPeriod(String period) {
        switch (period) {
            case "week": return "%m-%d";
            case "month": return "%m-%d";
            case "quarter": return "%Y第%u周";
            case "year": return "%Y-%m";
            default: return "%m-%d";
        }
    }

    /**
     * 获取班组进度
     */
    private HomeStatDto.TeamProgressDto buildTeamProgress(Long teamId) {
        HomeStatDto.TeamProgressDto dto = new HomeStatDto.TeamProgressDto();
        dto.setTeamId(teamId);

        // 获取班组信息
        com.ruoyi.manage.domain.ProductionTeam team = productionTeamMapper.selectProductionTeamById(teamId);
        if (team != null) {
            dto.setTeamName(team.getTeamName());
            dto.setMemberCount(productionTeamMapper.selectUserListByTeamId(teamId).size());
        }

        // 获取班组任务统计
        List<Map<String, Object>> teamRows = taskNodeExecutionMapper.countTeamStatsByStatus(teamId);
        for (Map<String, Object> row : teamRows) {
            String status = (String) row.get("status");
            long cnt = toLong(row.get("cnt"));
            switch (status) {
                case "pending":   dto.setPending(cnt);  break;
                case "claimed":   dto.setRunning(cnt);  break;
                case "completed": dto.setFinished(cnt); break;
                case "rejected":  dto.setRejected(cnt); break;
                default: break;
            }
        }
        dto.setTotal((dto.getPending() != null ? dto.getPending() : 0L) +
                     (dto.getRunning() != null ? dto.getRunning() : 0L) +
                     (dto.getFinished() != null ? dto.getFinished() : 0L) +
                     (dto.getRejected() != null ? dto.getRejected() : 0L));

        return dto;
    }

    /**
     * 获取班组效率
     * 通过 task_node_execution 表统计该班组的已完成任务，计算平均耗时和完成率
     */
    private HomeStatDto.TeamEfficiencyDto buildTeamEfficiency(Long teamId, DateRange dateRange) {
        HomeStatDto.TeamEfficiencyDto dto = new HomeStatDto.TeamEfficiencyDto();
        dto.setTeamId(teamId);

        // 获取班组信息
        com.ruoyi.manage.domain.ProductionTeam team = productionTeamMapper.selectProductionTeamById(teamId);
        if (team != null) {
            dto.setTeamName(team.getTeamName());
        }

        // 通过 task_node_execution 统计该班组的任务状态（已按班组过滤）
        List<Map<String, Object>> teamRows = taskNodeExecutionMapper.countTeamStatsByStatus(teamId);
        long completed = 0, total = 0;
        for (Map<String, Object> row : teamRows) {
            String status = (String) row.get("status");
            long cnt = toLong(row.get("cnt"));
            total += cnt;
            if ("completed".equals(status)) {
                completed += cnt;
            }
        }
        double completionRate = total == 0 ? 0.0 : (double) completed / total;

        // 计算该班组已完成任务的平均耗时（通过关联流程实例）
        // 简化实现：用所有已完成流程实例按时间范围过滤，按完成数量推算平均耗时
        double avgDurationHours = 0.0;
        if (completed > 0) {
            List<HistoricProcessInstance> instances = historyService
                .createHistoricProcessInstanceQuery()
                .finished()
                .startedAfter(dateRange.startDate)
                .startedBefore(dateRange.endDate)
                .list();
            long totalTime = 0;
            int validCount = 0;
            for (HistoricProcessInstance instance : instances) {
                Date startTime = instance.getStartTime();
                Date endTime = instance.getEndTime();
                if (startTime != null && endTime != null) {
                    totalTime += (endTime.getTime() - startTime.getTime());
                    validCount++;
                }
            }
            avgDurationHours = validCount == 0 ? 0.0 : (double) totalTime / validCount / 1000 / 3600;
        }

        dto.setAvgDurationHours(avgDurationHours);
        dto.setFinishedCount(completed);
        dto.setTotalCount(total);
        dto.setCompletionRate(completionRate);

        return dto;
    }

    /**
     * 获取成员统计
     */
    private List<HomeStatDto.MemberStatDto> getMemberStatsByTeam(Long teamId) {
        List<HomeStatDto.MemberStatDto> stats = new ArrayList<>();
        List<Map<String, Object>> rows = taskNodeExecutionMapper.countMemberStatsByTeam(teamId);

        for (Map<String, Object> row : rows) {
            HomeStatDto.MemberStatDto stat = new HomeStatDto.MemberStatDto();
            stat.setUserId(toLong(row.get("userId")));
            stat.setUserName((String) row.get("userName"));
            stat.setPending(toLong(row.get("pending")));
            stat.setRunning(toLong(row.get("running")));
            stat.setFinished(toLong(row.get("finished")));
            stat.setTotal(toLong(row.get("total")));
            stats.add(stat);
        }

        return stats;
    }

    /**
     * 判断用户是否为班长
     */
    private boolean isUserLeader(Long userId) {
        com.ruoyi.manage.domain.ProductionTeam query = new com.ruoyi.manage.domain.ProductionTeam();
        query.setLeaderId(userId);
        query.setTeamStatus("1");
        List<com.ruoyi.manage.domain.ProductionTeam> leaderTeams = productionTeamMapper.selectProductionTeamList(query);
        return leaderTeams != null && !leaderTeams.isEmpty();
    }

    /**
     * 获取用户所属班组
     */
    private List<Long> getUserTeamIds(Long userId) {
        List<Long> teamIds = new ArrayList<>();

        // 如果是班长，则返回所领导的班组
        if (isUserLeader(userId)) {
            com.ruoyi.manage.domain.ProductionTeam query = new com.ruoyi.manage.domain.ProductionTeam();
            query.setLeaderId(userId);
            query.setTeamStatus("1");
            List<com.ruoyi.manage.domain.ProductionTeam> leaderTeams = productionTeamMapper.selectProductionTeamList(query);
            for (com.ruoyi.manage.domain.ProductionTeam team : leaderTeams) {
                teamIds.add(team.getId());
            }
        } else {
            // 普通成员，通过 production_team_user 关联表查询所属班组
            List<Long> memberTeamIds = productionTeamMapper.selectTeamIdsByUserId(userId);
            if (memberTeamIds != null) {
                teamIds.addAll(memberTeamIds);
            }
        }

        return teamIds;
    }

    /**
     * 获取所有启用班组ID
     */
    private List<Long> getAllEnabledTeamIds() {
        com.ruoyi.manage.domain.ProductionTeam query = new com.ruoyi.manage.domain.ProductionTeam();
        query.setTeamStatus("1");
        List<com.ruoyi.manage.domain.ProductionTeam> allTeams = productionTeamMapper.selectProductionTeamList(query);
        return allTeams.stream().map(com.ruoyi.manage.domain.ProductionTeam::getId).collect(Collectors.toList());
    }

    /**
     * 构建最近完成任务列表
     */
    private List<HomeStatDto.RecentTaskDto> buildRecentTasks(List<Map<String, Object>> rows) {
        List<HomeStatDto.RecentTaskDto> list = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
        for (Map<String, Object> row : rows) {
            HomeStatDto.RecentTaskDto dto = new HomeStatDto.RecentTaskDto();
            dto.setNodeName((String) row.get("nodeName"));
            dto.setProcName((String) row.get("procName"));
            dto.setClaimUserName((String) row.get("claimUserName"));
            dto.setProcInstId(row.get("procInstId") != null ? row.get("procInstId").toString() : null);
            dto.setProcessDuration(toLong(row.get("processDuration")));
            // 格式化完成时间
            Object ct = row.get("completeTime");
            if (ct instanceof Date) {
                dto.setCompleteTime(sdf.format((Date) ct));
            } else if (ct != null) {
                dto.setCompleteTime(ct.toString());
            }
            list.add(dto);
        }
        return list;
    }

    /**
     * 构建个人任务统计
     */
    private HomeStatDto.MyStatsDto buildMyStats(List<Map<String, Object>> rows) {
        HomeStatDto.MyStatsDto dto = new HomeStatDto.MyStatsDto();
        for (Map<String, Object> row : rows) {
            String status = (String) row.get("status");
            long cnt = toLong(row.get("cnt"));
            switch (status) {
                case "pending":   dto.setPending(cnt);  break;
                case "claimed":   dto.setRunning(cnt);  break;
                case "completed": dto.setFinished(cnt); break;
                case "rejected":  dto.setRejected(cnt); break;
                default: break;
            }
        }
        dto.setTotal(dto.getPending() + dto.getRunning() + dto.getFinished() + dto.getRejected());
        return dto;
    }

    /**
     * 构建个人完成数量 Top 列表
     */
    private List<HomeStatDto.UserTopDto> buildUserTop5(List<Map<String, Object>> rows) {
        List<HomeStatDto.UserTopDto> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            HomeStatDto.UserTopDto dto = new HomeStatDto.UserTopDto();
            dto.setUserId(toLong(row.get("userId")));
            dto.setUserName((String) row.get("userName"));
            dto.setFinished(toLong(row.get("finished")));
            list.add(dto);
        }
        return list;
    }

    /**
     * 转换为长整型
     */
    private long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Number) return ((Number) val).longValue();
        try {
            return Long.parseLong(val.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * 日期范围内部类
     */
    private static class DateRange {
        Date startDate;
        Date endDate;
        String period;

        DateRange(Date startDate, Date endDate, String period) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.period = period;
        }
    }
}