import request from '@/utils/request'

/**
 * 获取任务统计看板数据（当前登录用户）
 */
export function getDashboardStats() {
  return request({
    url: '/flowable/stat/dashboard',
    method: 'get'
  })
}
