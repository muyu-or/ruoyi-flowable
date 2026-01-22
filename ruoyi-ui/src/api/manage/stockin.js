import request from '@/utils/request'

// 查询入库记录列表
export function listStockin(query) {
  return request({
    url: '/manage/stockin/list',
    method: 'get',
    params: query
  })
}

// 查询入库记录详细
export function getStockin(id) {
  return request({
    url: '/manage/stockin/' + id,
    method: 'get'
  })
}

// 新增入库记录
export function addStockin(data) {
  return request({
    url: '/manage/stockin',
    method: 'post',
    data: data
  })
}

// 修改入库记录
export function updateStockin(data) {
  return request({
    url: '/manage/stockin',
    method: 'put',
    data: data
  })
}

// 删除入库记录
export function delStockin(id) {
  return request({
    url: '/manage/stockin/' + id,
    method: 'delete'
  })
}
