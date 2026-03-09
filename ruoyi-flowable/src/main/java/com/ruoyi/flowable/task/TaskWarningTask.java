package com.ruoyi.flowable.task;

import com.ruoyi.flowable.service.ITaskWarningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 任务超时预警扫描定时任务
 * <p>
 * 由 Quartz 调度，每天 00:05 执行，扫描即将到期和已超期的流程节点，推送预警消息。
 * invoke_target: taskWarningTask.scanWarnings()
 * </p>
 *
 * @author xgh
 * @date 2026-03-09
 */
@Slf4j
@Component("taskWarningTask")
public class TaskWarningTask
{
    @Resource
    private ITaskWarningService taskWarningService;

    /**
     * 扫描预警（Quartz 调用入口）
     */
    public void scanWarnings()
    {
        log.info("===== Quartz 触发任务超时预警扫描 =====");
        try
        {
            taskWarningService.scanWarnings();
        }
        catch (Exception e)
        {
            log.error("任务超时预警扫描执行失败", e);
        }
    }
}
