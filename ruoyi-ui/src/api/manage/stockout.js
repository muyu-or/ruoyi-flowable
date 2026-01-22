import request from '@/utils/request'

// 查询出库记录列表
export function listStockout(query) {
  return request({
    url: '/manage/stockout/list',
    method: 'get',
    params: query
  })
}

// 查询出库记录详细
export function getStockout(id) {
  return request({
    url: '/manage/stockout/' + id,
    method: 'get'
  })
}

// 新增出库记录
export function addStockout(data) {
  return request({
    url: '/manage/stockout',
    method: 'post',
    data: data
  })
}

// 修改出库记录
export function updateStockout(data) {
  return request({
    url: '/manage/stockout',
    method: 'put',
    data: data
  })
}

// 删除出库记录
export function delStockout(id) {
  return request({
    url: '/manage/stockout/' + id,
    method: 'delete'
  })
}
