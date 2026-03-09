import request from '@/utils/request'

// 查询录像备份列表
export function listBackup(query) {
  return request({
    url: '/monitor/backup/list',
    method: 'get',
    params: query
  })
}

// 查询录像备份详细
export function getBackup(id) {
  return request({
    url: '/monitor/backup/' + id,
    method: 'get'
  })
}

// 新增录像备份
export function addBackup(data) {
  return request({
    url: '/monitor/backup',
    method: 'post',
    data: data
  })
}

// 修改录像备份
export function updateBackup(data) {
  return request({
    url: '/monitor/backup',
    method: 'put',
    data: data
  })
}

// 删除录像备份
export function delBackup(id) {
  return request({
    url: '/monitor/backup/' + id,
    method: 'delete'
  })
}
