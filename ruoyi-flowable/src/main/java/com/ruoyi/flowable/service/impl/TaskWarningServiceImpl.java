package com.ruoyi.flowable.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.flowable.domain.TaskWarning;
import com.ruoyi.flowable.mapper.TaskWarningMapper;
import com.ruoyi.flowable.service.ITaskWarningService;
import com.ruoyi.manage.mapper.ProductionTeamMapper;
import com.ruoyi.system.domain.TaskNodeExecution;
import com.ruoyi.system.mapper.TaskNodeExecutionMapper;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
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

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private TaskService taskService;

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

    /**
     * 扫描即将到期和已超期的节点，推送预警消息（由 Quartz 定时任务调用）
     */
    @Override
    public void scanWarnings()
    {
        String today = LocalDate.now().toString();
        String tomorrow = LocalDate.now().plusDays(1).toString();

        log.info("========== 开始扫描任务超时预警 ==========");
        log.info("today={}, tomorrow={}", today, tomorrow);

        // 查询所有运行中的流程实例
        List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().active().list();
        log.info("运行中的流程实例数: {}", processInstances.size());

        for (ProcessInstance pi : processInstances)
        {
            String procInstId = pi.getProcessInstanceId();
            try
            {
                Map<String, Object> vars = runtimeService.getVariables(procInstId);
                Map<String, Object> nodeTimeMap = extractNodeTimeMap(vars);
                if (nodeTimeMap == null || nodeTimeMap.isEmpty())
                {
                    continue;
                }

                // 查询该流程的所有活跃任务
                List<Task> activeTasks = taskService.createTaskQuery()
                    .processInstanceId(procInstId)
                    .active()
                    .list();

                for (Task task : activeTasks)
                {
                    String nodeKey = task.getTaskDefinitionKey();
                    String endDate = extractEndDate(nodeTimeMap, nodeKey);
                    if (endDate == null)
                    {
                        continue;
                    }

                    String taskId = task.getId();
                    String nodeName = task.getName();

                    // 情况1：已过期（endDate < today） → 发 overdue + 打 timeout_flag
                    if (endDate.compareTo(today) < 0)
                    {
                        handleOverdue(taskId, procInstId, nodeKey, nodeName, endDate);
                    }
                    // 情况2：今天到期或明天到期 → 发 deadline_soon
                    else if (endDate.equals(today) || endDate.equals(tomorrow))
                    {
                        sendWarningsToTeamMembers(taskId, procInstId, nodeKey, nodeName, endDate, "deadline_soon");
                    }
                }
            }
            catch (Exception e)
            {
                log.error("扫描流程实例 {} 预警时出错", procInstId, e);
            }
        }

        log.info("========== 任务超时预警扫描完成 ==========");
    }

    /**
     * 从流程变量中提取 nodeTimeMap
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractNodeTimeMap(Map<String, Object> vars)
    {
        Object ntmObj = vars.get("nodeTimeMap");
        if (ntmObj == null)
        {
            return null;
        }
        if (ntmObj instanceof Map)
        {
            return (Map<String, Object>) ntmObj;
        }
        try
        {
            return JSON.parseObject(ntmObj.toString(), Map.class);
        }
        catch (Exception e)
        {
            log.warn("解析 nodeTimeMap 失败: {}", ntmObj, e);
            return null;
        }
    }

    /**
     * 从 nodeTimeMap 中提取指定节点的 endDate
     */
    @SuppressWarnings("unchecked")
    private String extractEndDate(Map<String, Object> nodeTimeMap, String nodeKey)
    {
        Object nodeTime = nodeTimeMap.get(nodeKey);
        if (nodeTime == null)
        {
            return null;
        }
        Map<String, Object> timeInfo;
        if (nodeTime instanceof Map)
        {
            timeInfo = (Map<String, Object>) nodeTime;
        }
        else
        {
            try
            {
                timeInfo = JSON.parseObject(nodeTime.toString(), Map.class);
            }
            catch (Exception e)
            {
                log.warn("解析节点 {} 的时间信息失败: {}", nodeKey, nodeTime, e);
                return null;
            }
        }
        Object endDateObj = timeInfo.get("endDate");
        return endDateObj != null ? endDateObj.toString() : null;
    }

    /**
     * 向任务对应班组的所有成员推送预警消息
     */
    private void sendWarningsToTeamMembers(String taskId, String procInstId, String nodeKey,
                                           String nodeName, String endDate, String warnType)
    {
        TaskNodeExecution tne = taskNodeExecutionMapper.selectByTaskId(taskId);
        if (tne == null || tne.getAssignedTeamId() == null)
        {
            log.debug("任务 {} 无对应节点执行记录或班组，跳过预警", taskId);
            return;
        }

        Long teamId = tne.getAssignedTeamId();
        List<SysUser> members = productionTeamMapper.selectUserListByTeamId(teamId);
        if (members == null || members.isEmpty())
        {
            log.debug("班组 {} 无成员，跳过预警", teamId);
            return;
        }

        Date now = new Date();
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
            warning.setNodeKey(nodeKey);
            warning.setNodeName(nodeName);
            warning.setEndDate(endDate);
            warning.setIsRead(0);
            warning.setCreateTime(now);
            taskWarningMapper.insertTaskWarning(warning);
            log.info("已推送 {} 预警给用户 {}，节点: {}({})", warnType, userId, nodeName, nodeKey);
        }
    }

    /**
     * 处理已超时：打 timeout_flag + 推送 overdue 预警
     */
    private void handleOverdue(String taskId, String procInstId, String nodeKey,
                               String nodeName, String endDate)
    {
        TaskNodeExecution tne = taskNodeExecutionMapper.selectByTaskId(taskId);
        if (tne == null)
        {
            log.debug("任务 {} 无对应节点执行记录，跳过超时标记", taskId);
            return;
        }

        // 尚未标记超时 → 打标
        Integer timeoutFlag = tne.getTimeoutFlag();
        if (timeoutFlag == null || timeoutFlag == 0)
        {
            taskNodeExecutionMapper.updateTimeoutFlag(tne.getId(), 1);
            log.info("已标记任务 {} 超时，节点: {}({})", taskId, nodeName, nodeKey);
        }

        // 推送 overdue 预警
        sendWarningsToTeamMembers(taskId, procInstId, nodeKey, nodeName, endDate, "overdue");
    }
}
