import request from '@/utils/request'

// 查询监控设备列表
export function listDevice(query) {
  return request({
    url: '/monitor/device/list',
    method: 'get',
    params: query
  })
}

// 查询监控设备详细
export function getDevice(id) {
  return request({
    url: '/monitor/device/' + id,
    method: 'get'
  })
}

// 新增监控设备
export function addDevice(data) {
  return request({
    url: '/monitor/device',
    method: 'post',
    data: data
  })
}

// 修改监控设备
export function updateDevice(data) {
  return request({
    url: '/monitor/device',
    method: 'put',
    data: data
  })
}

// 删除监控设备
export function delDevice(id) {
  return request({
    url: '/monitor/device/' + id,
    method: 'delete'
  })
}
