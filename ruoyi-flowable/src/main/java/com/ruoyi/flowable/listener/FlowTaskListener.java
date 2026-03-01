package com.ruoyi.flowable.listener;

import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.flowable.service.IFlowTeamService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

/**
 * 任务监听器
 *
 * create（创建）:在任务被创建且所有的任务属性设置完成后才触发
 * assignment（指派）：在任务被分配给某个办理人之后触发
 * complete（完成）：在配置了监听器的上一个任务完成时触发
 * delete（删除）：在任务即将被删除前触发。请注意任务由completeTask正常完成时也会触发
 *
 * @author Tony
 * @date 2021/4/20
 */
@Slf4j
@Component
public class FlowTaskListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String event = delegateTask.getEventName();
        String taskId = delegateTask.getId();
        String procInstId = delegateTask.getProcessInstanceId();
        String nodeKey = delegateTask.getTaskDefinitionKey();
        String nodeName = delegateTask.getName();

        log.info("任务监听器触发，事件: {}, 任务ID: {}, 流程实例ID: {}", event, taskId, procInstId);

        try {
            IFlowTeamService flowTeamService = SpringUtils.getBean(IFlowTeamService.class);

            if (EVENTNAME_CREATE.equals(event)) {
                // 任务创建时：注入班组成员为候选人 + 写 task_node_execution
                flowTeamService.injectTeamCandidates(taskId, procInstId, nodeKey, nodeName);
            } else if (EVENTNAME_COMPLETE.equals(event)) {
                // 任务完成时：仅补充 claim_user_id（如果还没有认领记录）
                // result / status 由业务层（FlowTaskServiceImpl.complete / taskReject）明确写入
                // 这里不设置 result，避免"不通过"被误判为"completed"
                String assigneeStr = delegateTask.getAssignee();
                Long assigneeId = null;
                if (assigneeStr != null && !assigneeStr.isEmpty()) {
                    try {
                        assigneeId = Long.parseLong(assigneeStr);
                    } catch (NumberFormatException ex) {
                        log.warn("无法解析 assignee 为 userId: {}", assigneeStr);
                    }
                }
                // 只补充执行人，不覆盖 result（传 null result 让 onTaskCompleted 跳过 result 赋值）
                flowTeamService.onTaskCompleted(taskId, null, "", assigneeId);
            }
        } catch (Exception e) {
            log.error("任务监听器处理异常，事件: {}, 任务ID: {}", event, taskId, e);
        }
    }

}
