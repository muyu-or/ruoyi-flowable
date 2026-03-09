import request from '@/utils/request'

/**
 * 获取待办任务列表
 */
export function todoList(query) {
  return request({
    url: '/flowable/task/todoList',
    method: 'get',
    params: query
  })
}

/**
 * 获取已办任务列表
 */
export function finishedList(query) {
  return request({
    url: '/flowable/task/finishedList',
    method: 'get',
    params: query
  })
}

/**
 * 我发起的流程
 */
export function myProcess(query) {
  return request({
    url: '/flowable/task/myProcess',
    method: 'get',
    params: query
  })
}

/**
 * 获取流程变量（前置节点传来的数据）
 */
export function getProcessVariables(taskId) {
  return request({
    url: '/flowable/task/processVariables/' + taskId,
    method: 'get'
  })
}

/**
 * 完成任务（并传递新变量给下一节点）
 *
 * 参数说明：
 * taskVo.taskId - 任务ID
 * taskVo.instanceId - 流程实例ID
 * taskVo.comment - 任务意见
 * taskVo.variables - 新增变量（如审批结果、审批意见等）
 *   - variables中应包含用于网关条件判断的关键字段
 *   - 例如：{ approval_status: 'approved', approvalComment: '同意' }
 */
export function completeTask(taskVo) {
  return request({
    url: '/flowable/task/complete',
    method: 'post',
    data: taskVo
  })
}

/**
 * 驳回任务
 */
export function rejectTask(taskVo) {
  return request({
    url: '/flowable/task/reject',
    method: 'post',
    data: taskVo
  })
}

/**
 * 退回任务
 */
export function returnTask(taskVo) {
  return request({
    url: '/flowable/task/return',
    method: 'post',
    data: taskVo
  })
}

/**
 * 认领任务
 */
export function claimTask(taskVo) {
  return request({
    url: '/flowable/task/claim',
    method: 'post',
    data: taskVo
  })
}

/**
 * 取消认领
 */
export function unClaimTask(taskVo) {
  return request({
    url: '/flowable/task/unClaim',
    method: 'post',
    data: taskVo
  })
}

/**
 * 转办任务
 */
export function assignTask(taskVo) {
  return request({
    url: '/flowable/task/assignTask',
    method: 'post',
    data: taskVo
  })
}

/**
 * 流程历史记录
 */
export function flowRecord(procInsId, deployId) {
  return request({
    url: '/flowable/task/flowRecord',
    method: 'get',
    params: {
      procInsId: procInsId,
      deployId: deployId
    }
  })
}

/**
 * 流程图
 */
export function diagram(processId) {
  return request({
    url: '/flowable/task/diagram/' + processId,
    method: 'get'
  })
}

/**
 * 获取下一节点信息
 */
export function getNextFlowNode(taskVo) {
  return request({
    url: '/flowable/task/nextFlowNode',
    method: 'post',
    data: taskVo
  })
}

/**
 * 发起流程获取下一节点
 */
export function getNextFlowNodeByStart(taskVo) {
  return request({
    url: '/flowable/task/nextFlowNodeByStart',
    method: 'post',
    data: taskVo
  })
}
