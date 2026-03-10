package com.ruoyi.flowable.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.flowable.domain.dto.CalendarEventDto;
import com.ruoyi.manage.domain.ProductionTeam;
import com.ruoyi.manage.mapper.ProductionTeamMapper;
import com.ruoyi.system.domain.TaskExecutionRecord;
import com.ruoyi.system.domain.TaskNodeExecution;
import com.ruoyi.system.mapper.TaskExecutionRecordMapper;
import com.ruoyi.system.mapper.TaskNodeExecutionMapper;
import org.flowable.engine.HistoryService;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 日历看板Controller
 *
 * @author claude
 * @date 2026-03-10
 */
@RestController
@RequestMapping("/flowable/calendar")
public class CalendarController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(CalendarController.class);

    @Resource
    private TaskNodeExecutionMapper taskNodeExecutionMapper;

    @Resource
    private ProductionTeamMapper productionTeamMapper;

    @Resource
    private TaskExecutionRecordMapper taskExecutionRecordMapper;

    @Resource
    private HistoryService historyService;

    /**
     * 查询日历看板事件
     *
     * @param year  年份
     * @param month 月份
     * @return 日历事件列表
     */
    @GetMapping("/events")
    @PreAuthorize("@ss.hasPermi('flowable:calendar:view')")
    public AjaxResult getEvents(@RequestParam int year, @RequestParam int month) {
        Long userId = SecurityUtils.getUserId();
        boolean isAdmin = SecurityUtils.isAdmin(userId);

        // 月份首末日
        LocalDate first = LocalDate.of(year, month, 1);
        String monthStart = first.toString();
        String monthEnd = first.withDayOfMonth(first.lengthOfMonth()).toString();

        // 角色过滤：Admin 看全部，组长按班组看，成员只看自己
        List<Long> teamIds = null;
        boolean filterByUser = !isAdmin;
        if (!isAdmin) {
            // 查是否是班组长：组长按班组过滤（看班组所有成员任务）
            ProductionTeam query = new ProductionTeam();
            query.setLeaderId(userId);
            List<ProductionTeam> leaderTeams = productionTeamMapper.selectProductionTeamList(query);
            if (leaderTeams != null && !leaderTeams.isEmpty()) {
                teamIds = leaderTeams.stream()
                        .map(ProductionTeam::getId)
                        .collect(Collectors.toList());
            }
            // 不是组长 → teamIds 保持 null → SQL 走 userId 过滤（只看自己的任务）
        }

        List<Map<String, Object>> rows = taskNodeExecutionMapper
                .selectCalendarEvents(monthStart, monthEnd, userId, teamIds, filterByUser);

        // 组装 DTO
        List<CalendarEventDto> events = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            CalendarEventDto dto = new CalendarEventDto();
            Object idVal = row.get("id");
            dto.setId(idVal != null ? Long.parseLong(idVal.toString()) : null);
            dto.setNodeKey((String) row.get("nodeKey"));
            dto.setNodeName((String) row.get("nodeName"));
            dto.setStatus((String) row.get("status"));
            Object startTimeVal = row.get("startTime");
            dto.setStartTime(startTimeVal != null ? startTimeVal.toString() : null);
            Object completeTimeVal = row.get("completeTime");
            dto.setCompleteTime(completeTimeVal != null ? completeTimeVal.toString() : null);
            // displayDate 保证非空：优先 plan_end_date，回退 start_time 日期部分
            Object displayDateVal = row.get("displayDate");
            dto.setPlanEndDate(displayDateVal != null ? displayDateVal.toString() : null);
            Object planStartDateVal = row.get("planStartDate");
            dto.setPlanStartDate(planStartDateVal != null ? planStartDateVal.toString() : null);
            Object timeoutVal = row.get("timeoutFlag");
            dto.setTimeoutFlag(timeoutVal != null ? Integer.parseInt(timeoutVal.toString()) : 0);
            dto.setTeamName((String) row.get("teamName"));
            dto.setProcInstId((String) row.get("procInstId"));
            Object durationVal = row.get("processDuration");
            dto.setProcessDuration(durationVal != null ? durationVal.toString() : null);
            Object taskNameVal = row.get("taskName");
            dto.setTaskName(taskNameVal != null ? taskNameVal.toString() : "");
            Object claimUserNameVal = row.get("claimUserName");
            dto.setClaimUserName(claimUserNameVal != null ? claimUserNameVal.toString() : null);
            events.add(dto);
        }

        return AjaxResult.success(events);
    }

    /**
     * 一次性回填历史数据的 plan_start_date
     * <p>
     * 查找所有 plan_start_date 为空但 plan_end_date 不为空的记录，
     * 通过 Flowable 历史变量 nodeTimeMap 读取对应节点的 startDate 回填。
     * 此接口幂等，可重复调用，仅管理员可执行。
     */
    @PostMapping("/backfill-plan-start-date")
    @PreAuthorize("@ss.hasRole('admin')")
    public AjaxResult backfillPlanStartDate() {
        // 查所有 planEndDate 有值但 planStartDate 为空的记录
        TaskNodeExecution query = new TaskNodeExecution();
        List<TaskNodeExecution> allRecords = taskNodeExecutionMapper.selectTaskNodeExecutionList(query);

        int updated = 0;
        int skipped = 0;
        for (TaskNodeExecution record : allRecords) {
            // 只处理 planStartDate 为空且 planEndDate 不为空的
            if (record.getPlanStartDate() != null && !record.getPlanStartDate().isEmpty()) {
                continue;
            }
            if (record.getPlanEndDate() == null || record.getPlanEndDate().isEmpty()) {
                continue;
            }

            try {
                // 通过 execRecordId 获取 procInstId
                TaskExecutionRecord execRecord = taskExecutionRecordMapper
                        .selectTaskExecutionRecordById(record.getExecRecordId());
                if (execRecord == null || execRecord.getProcInstId() == null) {
                    skipped++;
                    continue;
                }

                // 从 Flowable 历史变量中读取 nodeTimeMap
                List<HistoricVariableInstance> varList = historyService
                        .createHistoricVariableInstanceQuery()
                        .processInstanceId(execRecord.getProcInstId())
                        .variableName("nodeTimeMap")
                        .list();
                if (varList == null || varList.isEmpty()) {
                    skipped++;
                    continue;
                }

                Object ntmObj = varList.get(0).getValue();
                if (ntmObj instanceof Map) {
                    Map<String, Object> ntm = (Map<String, Object>) ntmObj;
                    Object nodeTime = ntm.get(record.getNodeKey());
                    if (nodeTime instanceof Map) {
                        Object startDate = ((Map<?, ?>) nodeTime).get("startDate");
                        if (startDate != null && !startDate.toString().isEmpty()) {
                            TaskNodeExecution upd = new TaskNodeExecution();
                            upd.setId(record.getId());
                            upd.setPlanStartDate(startDate.toString());
                            taskNodeExecutionMapper.updateTaskNodeExecution(upd);
                            updated++;
                            continue;
                        }
                    }
                }
                skipped++;
            } catch (Exception e) {
                log.warn("回填 plan_start_date 失败，recordId={}", record.getId(), e);
                skipped++;
            }
        }

        return AjaxResult.success("回填完成：成功 " + updated + " 条，跳过 " + skipped + " 条");
    }
}
