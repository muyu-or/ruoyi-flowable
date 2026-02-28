package com.ruoyi.flowable.service.impl;

import com.ruoyi.flowable.domain.dto.DashboardStatsDto;
import com.ruoyi.flowable.service.IFlowStatService;
import com.ruoyi.manage.domain.ProductionTeam;
import com.ruoyi.manage.mapper.ProductionTeamMapper;
import com.ruoyi.system.mapper.TaskNodeExecutionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 任务统计 Service 实现
 */
@Slf4j
@Service
public class FlowStatServiceImpl implements IFlowStatService {

    @Resource
    private TaskNodeExecutionMapper taskNodeExecutionMapper;

    @Resource
    private ProductionTeamMapper productionTeamMapper;

    @Override
    public DashboardStatsDto getDashboardStats(Long userId) {
        DashboardStatsDto result = new DashboardStatsDto();

        // 1. 个人任务统计
        List<Map<String, Object>> myRows = taskNodeExecutionMapper.countMyStatsByStatus(userId);
        result.setMyStats(buildMyStats(myRows));

        // 2. 判断是否为班组长（取第一个担任班组长的启用班组）
        ProductionTeam query = new ProductionTeam();
        query.setLeaderId(userId);
        query.setTeamStatus("1");
        List<ProductionTeam> leaderTeams = productionTeamMapper.selectProductionTeamList(query);

        if (leaderTeams != null && !leaderTeams.isEmpty()) {
            ProductionTeam team = leaderTeams.get(0);
            result.setIsLeader(true);

            // 3. 班组成员人数
            int memberCount = productionTeamMapper.selectUserListByTeamId(team.getId()).size();

            // 4. 班组任务统计
            List<Map<String, Object>> teamRows = taskNodeExecutionMapper.countTeamStatsByStatus(team.getId());
            DashboardStatsDto.TeamStatsDto teamStats = buildTeamStats(teamRows);
            teamStats.setTeamId(team.getId());
            teamStats.setTeamName(team.getTeamName());
            teamStats.setMemberCount(memberCount);
            result.setTeamStats(teamStats);
        } else {
            result.setIsLeader(false);
        }

        return result;
    }

    // ---- 私有帮助方法 ----

    private DashboardStatsDto.MyStatsDto buildMyStats(List<Map<String, Object>> rows) {
        DashboardStatsDto.MyStatsDto dto = new DashboardStatsDto.MyStatsDto();
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

    private DashboardStatsDto.TeamStatsDto buildTeamStats(List<Map<String, Object>> rows) {
        DashboardStatsDto.TeamStatsDto dto = new DashboardStatsDto.TeamStatsDto();
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

    private long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Number) return ((Number) val).longValue();
        return Long.parseLong(val.toString());
    }
}
