import request from '@/utils/request'

export function getHomeStats(params) {
  return request({
    url: '/flowable/stat/home',
    method: 'get',
    params
  })
}

export function getDashboardStats() {
  return request({
    url: '/flowable/stat/dashboard',
    method: 'get'
  })
}