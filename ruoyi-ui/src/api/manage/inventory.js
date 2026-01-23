import request from '@/utils/request'

// 查询库存信息列表
export function listInventory(query) {
  return request({
    url: '/manage/inventory/list',
    method: 'get',
    params: query
  })
}

// 查询库存信息详细
export function getInventory(id) {
  return request({
    url: '/manage/inventory/' + id,
    method: 'get'
  })
}

// 新增库存信息
export function addInventory(data) {
  return request({
    url: '/manage/inventory',
    method: 'post',
    data: data
  })
}

// 库存出库
export function stockOutInventory(data) {
  return request({
    url: '/manage/inventory/stock-out',
    method: 'post',
    data: data
  })
}

// 扫码入库
export function scanInbound(data) {
  return request({
    url: '/manage/inventory/scan-inbound',
    method: 'post',
    data: data
  })
}

// 修改库存信息
export function updateInventory(data) {
  return request({
    url: '/manage/inventory',
    method: 'put',
    data: data
  })
}

// 删除库存信息
export function delInventory(id) {
  return request({
    url: '/manage/inventory/' + id,
    method: 'delete'
  })
}
