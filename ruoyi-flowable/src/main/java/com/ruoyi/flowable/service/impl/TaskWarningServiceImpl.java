package com.ruoyi.flowable.service.impl;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.flowable.domain.TaskWarning;
import com.ruoyi.flowable.mapper.TaskWarningMapper;
import com.ruoyi.flowable.service.ITaskWarningService;
import com.ruoyi.manage.domain.ProductionTeam;
import com.ruoyi.manage.mapper.ProductionTeamMapper;
import com.ruoyi.system.mapper.TaskNodeExecutionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 任务节点超时预警服务实现
 *
 * @author xgh
 * @date 2026-03-09
 */
@Slf4j
@Service
public class TaskWarningServiceImpl implements ITaskWarningService
{
    @Resource
    private TaskWarningMapper taskWarningMapper;

    @Resource
    private TaskNodeExecutionMapper taskNodeExecutionMapper;

    @Resource
    private ProductionTeamMapper productionTeamMapper;

    @Override
    public int insertTaskWarning(TaskWarning warning)
    {
        return taskWarningMapper.insertTaskWarning(warning);
    }

    @Override
    public List<TaskWarning> selectByUserId(Long userId, int pageNum, int pageSize)
    {
        int offset = (pageNum - 1) * pageSize;
        return taskWarningMapper.selectByUserId(userId, offset, pageSize);
    }

    @Override
    public int countUnreadByUserId(Long userId)
    {
        return taskWarningMapper.countUnreadByUserId(userId);
    }

    @Override
    public int markAllReadByUserId(Long userId)
    {
        return taskWarningMapper.markAllReadByUserId(userId);
    }

    @Override
    public int markReadById(Long id)
    {
        return taskWarningMapper.markReadById(id);
    }

    @Override
    public int markReadByProcInstAndNodeKey(String procInstId, String nodeKey)
    {
        return taskWarningMapper.markReadByProcInstAndNodeKey(procInstId, nodeKey);
    }

    @Override
    public List<TaskWarning> selectAllDistinct(int pageNum, int pageSize)
    {
        int offset = (pageNum - 1) * pageSize;
        return taskWarningMapper.selectAllDistinct(offset, pageSize);
    }

    @Override
    public int countAllUnresolved()
    {
        return taskWarningMapper.countAllUnresolved();
    }

    @Override
    public int countAllDistinctUnread()
    {
        return taskWarningMapper.countAllDistinctUnread();
    }

    @Override
    public int markAllRead()
    {
        return taskWarningMapper.markAllRead();
    }

    @Override
    public int markAllAdminRead()
    {
        return taskWarningMapper.markAllAdminRead();
    }

    @Override
    public int deleteResolvedByIds(List<Long> ids)
    {
        return taskWarningMapper.deleteResolvedByIds(ids);
    }

    @Override
    public int clearResolved(Long userId, boolean isManager)
    {
        if (isManager)
        {
            return taskWarningMapper.deleteAllResolved();
        }
        return taskWarningMapper.deleteResolvedByUserId(userId);
    }

    /**
     * 扫描即将到期和已超期的节点，推送预警消息（由 Quartz 定时任务调用）
     * <p>
     * 扫描范围：所有运行中流程的全部未完成节点（不仅是当前活跃节点），
     * 只要节点的 plan_end_date 已到期或即将到期就预警。
     * </p>
     */
    @Override
    public void scanWarnings()
    {
        String today = LocalDate.now().toString();
        String tomorrow = LocalDate.now().plusDays(1).toString();

        log.info("========== 开始扫描任务超时预警 ==========");
        log.info("today={}, tomorrow={}", today, tomorrow);

        // 查询所有未完成且 plan_end_date <= tomorrow 的节点（包含尚未轮到执行的节点）
        List<Map<String, Object>> nodes = taskNodeExecutionMapper.selectPendingNodesForWarning(tomorrow);
        log.info("需预警检查的节点数: {}", nodes.size());

        Date now = new Date();
        for (Map<String, Object> node : nodes)
        {
            try
            {
                Long tneId = toLong(node.get("id"));
                String nodeKey = (String) node.get("nodeKey");
                String nodeName = (String) node.get("nodeName");
                String planEndDate = (String) node.get("planEndDate");
                String procInstId = (String) node.get("procInstId");
                String taskName = (String) node.get("taskName");
                Long assignedTeamId = toLong(node.get("assignedTeamId"));
                Integer timeoutFlag = toInt(node.get("timeoutFlag"));

                if (planEndDate == null || procInstId == null)
                {
                    continue;
                }

                // 查班组名称
                String teamName = null;
                if (assignedTeamId != null && assignedTeamId > 0)
                {
                    ProductionTeam team = productionTeamMapper.selectProductionTeamById(assignedTeamId);
                    if (team != null)
                    {
                        teamName = team.getTeamName();
                    }
                }

                // 判断预警类型
                if (planEndDate.compareTo(today) < 0)
                {
                    // 已超时：打 timeout_flag + 升级旧的 deadline_soon → overdue + 推送 overdue
                    if (timeoutFlag == null || timeoutFlag == 0)
                    {
                        taskNodeExecutionMapper.updateTimeoutFlag(tneId, 1);
                        log.info("已标记节点 {} ({}) 超时", nodeName, nodeKey);
                    }
                    // 将之前的 deadline_soon 预警升级为 overdue
                    taskWarningMapper.upgradeToOverdue(procInstId, nodeKey);
                    sendWarningsToTeamMembers(assignedTeamId, procInstId, null, taskName,
                            nodeKey, nodeName, planEndDate, teamName, "overdue", now);
                }
                else
                {
                    // 今天或明天到期：deadline_soon
                    sendWarningsToTeamMembers(assignedTeamId, procInstId, null, taskName,
                            nodeKey, nodeName, planEndDate, teamName, "deadline_soon", now);
                }
            }
            catch (Exception e)
            {
                log.error("扫描节点预警时出错: {}", node, e);
            }
        }

        log.info("========== 任务超时预警扫描完成 ==========");
    }

    /**
     * 向任务对应班组的所有成员推送预警消息
     */
    private void sendWarningsToTeamMembers(Long teamId, String procInstId, String procName,
                                           String taskName, String nodeKey, String nodeName,
                                           String endDate, String teamName, String warnType, Date now)
    {
        if (teamId == null || teamId <= 0)
        {
            log.debug("节点 {}({}) 无分配班组，跳过预警", nodeName, nodeKey);
            return;
        }

        List<SysUser> members = productionTeamMapper.selectUserListByTeamId(teamId);
        if (members == null || members.isEmpty())
        {
            log.debug("班组 {} 无成员，跳过预警", teamId);
            return;
        }

        for (SysUser member : members)
        {
            Long userId = member.getUserId();
            // 去重检查
            int exists = taskWarningMapper.countByUserNodeType(userId, procInstId, nodeKey, warnType);
            if (exists > 0)
            {
                continue;
            }

            TaskWarning warning = new TaskWarning();
            warning.setUserId(userId);
            warning.setWarnType(warnType);
            warning.setProcInstId(procInstId);
            warning.setProcName(procName);
            warning.setTaskName(taskName);
            warning.setTeamName(teamName);
            warning.setNodeKey(nodeKey);
            warning.setNodeName(nodeName);
            warning.setEndDate(endDate);
            warning.setIsRead(0);
            warning.setResolved(0);
            warning.setCreateTime(now);
            taskWarningMapper.insertTaskWarning(warning);
            log.info("已推送 {} 预警给用户 {}，节点: {}({})", warnType, userId, nodeName, nodeKey);
        }
    }

    /**
     * 安全转换为 Long
     */
    private Long toLong(Object obj)
    {
        if (obj == null)
        {
            return null;
        }
        if (obj instanceof Long)
        {
            return (Long) obj;
        }
        if (obj instanceof Number)
        {
            return ((Number) obj).longValue();
        }
        try
        {
            return Long.parseLong(obj.toString());
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    /**
     * 安全转换为 Integer
     */
    private Integer toInt(Object obj)
    {
        if (obj == null)
        {
            return null;
        }
        if (obj instanceof Integer)
        {
            return (Integer) obj;
        }
        if (obj instanceof Number)
        {
            return ((Number) obj).intValue();
        }
        try
        {
            return Integer.parseInt(obj.toString());
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
}
