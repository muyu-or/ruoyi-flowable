import request from '@/utils/request'

// 查询待办任务列表
export function todoList(query) {
  return request({
    url: '/flowable/task/todoList',
    method: 'get',
    params: query
  })
}

// 完成任务
export function complete(data) {
  return request({
    url: '/flowable/task/complete',
    method: 'post',
    data: data
  })
}

// 委派任务
export function delegate(data) {
  return request({
    url: '/flowable/task/delegate',
    method: 'post',
    data: data
  })
}

// 退回任务（返回上一节点，功能保留备用，当前界面不展示）
export function returnTask(data) {
  return request({
    url: '/flowable/task/return',
    method: 'post',
    data: data
  })
}

// 退回重审（当前节点重置，由同班组人员重新处理）
export function redoTask(data) {
  return request({
    url: '/flowable/task/redo',
    method: 'post',
    data: data
  })
}

// 可退回任务列表（返回上一节点功能备用，当前界面不展示）
export function returnList(data) {
  return request({
    url: '/flowable/task/returnList',
    method: 'post',
    data: data
  })
}

// 驳回任务
export function rejectTask(data) {
  return request({
    url: '/flowable/task/reject',
    method: 'post',
    data: data
  })
}

// 下一节点
export function getNextFlowNode(data) {
  return request({
    url: '/flowable/task/nextFlowNode',
    method: 'post',
    data: data
  })
}

// 下一节点
export function getNextFlowNodeByStart(data) {
  return request({
    url: '/flowable/task/nextFlowNodeByStart',
    method: 'post',
    data: data
  })
}

// 部署流程实例
export function deployStart(deployId) {
  return request({
    url: '/flowable/process/startFlow/' + deployId,
    method: 'get'
  })
}

// 查询流程定义详细
export function getDeployment(id) {
  return request({
    url: '/system/deployment/' + id,
    method: 'get'
  })
}

// 新增流程定义
export function addDeployment(data) {
  return request({
    url: '/system/deployment',
    method: 'post',
    data: data
  })
}

// 修改流程定义
export function updateDeployment(data) {
  return request({
    url: '/system/deployment',
    method: 'put',
    data: data
  })
}

// 删除流程定义
export function delDeployment(id) {
  return request({
    url: '/system/deployment/' + id,
    method: 'delete'
  })
}

// 导出流程定义
export function exportDeployment(query) {
  return request({
    url: '/system/deployment/export',
    method: 'get',
    params: query
  })
}
// 流程节点表单
export function flowTaskForm(query) {
  return request({
    url: '/flowable/task/flowTaskForm',
    method: 'get',
    params: query
  })
}

// 按流程实例ID聚合已完成节点表单（发起人/管理员实时查看进行中流程）
export function flowTaskFormByProcInst(procInsId) {
  return request({
    url: '/flowable/task/flowTaskFormByProcInst',
    method: 'get',
    params: { procInsId }
  })
}

// 判断当前用户是否为任务所属班组的班组长
export function isLeaderOfTask(taskId) {
  return request({
    url: '/flowable/task/isLeaderOfTask',
    method: 'get',
    params: { taskId }
  })
}

// 班组成员提交表单数据（不推进流程）
export function saveFormData(data) {
  return request({
    url: '/flowable/task/saveFormData',
    method: 'post',
    data: data
  })
}
