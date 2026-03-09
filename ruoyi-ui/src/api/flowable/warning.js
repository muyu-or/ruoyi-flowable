import request from '@/utils/request'

// 查询当前用户预警列表
export function getWarningList(query) {
  return request({
    url: '/flowable/warning/list',
    method: 'get',
    params: query
  })
}

// 查询当前用户未读预警数
export function getUnreadWarningCount() {
  return request({
    url: '/flowable/warning/unread',
    method: 'get'
  })
}

// 全部标为已读
export function markAllRead() {
  return request({
    url: '/flowable/warning/readAll',
    method: 'put'
  })
}

// 单条标为已读
export function markOneRead(id) {
  return request({
    url: '/flowable/warning/read/' + id,
    method: 'put'
  })
}

// 手动触发预警扫描
export function triggerScan() {
  return request({
    url: '/flowable/warning/scan',
    method: 'post'
  })
}
