package com.ruoyi.flowable.service;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.flowable.domain.vo.FlowQueryVo;
import com.ruoyi.flowable.domain.vo.FlowTaskVo;
import org.flowable.task.api.Task;

import java.io.InputStream;
import java.util.List;

/**
 * @author Tony
 * @date 2021-04-03 14:42
 */
public interface IFlowTaskService {

    /**
     * 审批任务
     *
     * @param task 请求实体参数
     */
    AjaxResult complete(FlowTaskVo task);

    /**
     * 驳回任务
     *
     * @param flowTaskVo
     */
    void taskReject(FlowTaskVo flowTaskVo);


    /**
     * 退回任务
     *
     * @param flowTaskVo 请求实体参数
     */
    void taskReturn(FlowTaskVo flowTaskVo);

    /**
     * 退回重审：将当前节点重置为待处理状态，由同班组人员重新处理（不退回上一节点）
     *
     * @param flowTaskVo 请求实体参数（taskId、comment 必填）
     */
    void redoTask(FlowTaskVo flowTaskVo);

    /**
     * 获取所有可回退的节点
     *
     * @param flowTaskVo
     * @return
     */
    AjaxResult findReturnTaskList(FlowTaskVo flowTaskVo);

    /**
     * 删除任务
     *
     * @param flowTaskVo 请求实体参数
     */
    void deleteTask(FlowTaskVo flowTaskVo);

    /**
     * 认领/签收任务
     *
     * @param flowTaskVo 请求实体参数
     */
    void claim(FlowTaskVo flowTaskVo);

    /**
     * 取消认领/签收任务
     *
     * @param flowTaskVo 请求实体参数
     */
    void unClaim(FlowTaskVo flowTaskVo);

    /**
     * 委派任务
     *
     * @param flowTaskVo 请求实体参数
     */
    void delegateTask(FlowTaskVo flowTaskVo);

    /**
     * 任务归还
     *
     * @param flowTaskVo 请求实体参数
     */
    void resolveTask(FlowTaskVo flowTaskVo);


    /**
     * 转办任务
     *
     * @param flowTaskVo 请求实体参数
     */
    void assignTask(FlowTaskVo flowTaskVo);


    /**
     * 多实例加签
     * @param flowTaskVo
     */
    void addMultiInstanceExecution(FlowTaskVo flowTaskVo);

    /**
     * 多实例减签
     * @param flowTaskVo
     */
    void deleteMultiInstanceExecution(FlowTaskVo flowTaskVo);

    /**
     * 我发起的流程
     * @param queryVo  请求参数
     * @return
     */
    AjaxResult myProcess(FlowQueryVo queryVo);

    /**
     * 取消申请
     * 目前实现方式: 直接将当前流程变更为已完成
     * @param flowTaskVo
     * @return
     */
    AjaxResult stopProcess(FlowTaskVo flowTaskVo);

    /**
     * 撤回流程
     * @param flowTaskVo
     * @return
     */
    AjaxResult revokeProcess(FlowTaskVo flowTaskVo);


    /**
     * 代办任务列表
     *
     * @param queryVo  请求参数
     * @return
     */
    AjaxResult todoList(FlowQueryVo queryVo);


    /**
     * 已办任务列表
     *
     * @param queryVo  请求参数
     * @return
     */
    AjaxResult finishedList(FlowQueryVo queryVo);

    /**
     * 流程历史流转记录
     *
     * @param procInsId 流程实例Id
     * @return
     */
    AjaxResult flowRecord(String procInsId,String deployId);

    /**
     * 根据任务ID查询挂载的表单信息
     *
     * @param taskId 任务Id
     * @return
     */
    AjaxResult getTaskForm(String taskId);

    /**
     * 获取流程过程图
     * @param processId
     * @return
     */
    InputStream diagram(String processId);

    /**
     * 获取流程执行节点
     * @param procInsId
     * @return
     */
    AjaxResult getFlowViewer(String procInsId,String executionId);

    /**
     * 获取流程变量
     * @param taskId
     * @return
     */
    AjaxResult processVariables(String taskId);

    /**
     * 获取下一节点
     * @param flowTaskVo 任务
     * @return
     */
    AjaxResult getNextFlowNode(FlowTaskVo flowTaskVo);

    AjaxResult getNextFlowNodeByStart(FlowTaskVo flowTaskVo);

    /**
     * 流程初始化表单
     * @param deployId
     * @return
     */
    AjaxResult flowFormData(String deployId);

    /**
     * 流程节点信息
     * @param procInsId
     * @return
     */
    AjaxResult flowXmlAndNode(String procInsId,String deployId);

    /**
     * 流程节点表单
     * @param taskId 流程任务编号
     * @return
     */
    AjaxResult flowTaskForm(String taskId) throws Exception;

    /**
     * 按流程实例ID聚合所有已完成节点的表单数据（流程进行中也可实时查看）
     * @param procInsId 流程实例ID
     * @return
     */
    AjaxResult flowTaskFormByProcInst(String procInsId) throws Exception;


    /**
     * 流程节点信息
     * @param procInsId
     * @param elementId
     * @return
     */
    AjaxResult flowTaskInfo(String procInsId, String elementId);

    /**
     * 更新任务候选人（当班组配置改变时调用）
     * @param taskId 任务ID
     */
    void updateTaskCandidates(String taskId);

    /**
     * 批量更新流程实例下的所有任务候选人
     * @param procInstId 流程实例ID
     */
    void updateFlowInstanceTasksCandidates(String procInstId);

    /**
     * 判断当前登录用户是否为指定任务所属班组的班组长
     *
     * @param taskId 任务ID
     * @return true=班组长，false=普通成员或无班组配置
     */
    boolean isLeaderOfTask(String taskId);

    /**
     * 仅保存表单数据到流程变量（不推进流程），供班组成员提交表单使用
     *
     * @param taskVo 包含 taskId、variables（含命名空间表单数据）
     */
    void saveTaskFormData(FlowTaskVo taskVo);
}
