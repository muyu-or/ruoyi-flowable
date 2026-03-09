import request from '@/utils/request'

export function listReport(query) {
  return request({ url: '/manage/report/list', method: 'get', params: query })
}

export function getReport(id) {
  return request({ url: '/manage/report/' + id, method: 'get' })
}

export function addReport(data) {
  return request({ url: '/manage/report', method: 'post', data })
}

export function updateReport(data) {
  return request({ url: '/manage/report', method: 'put', data })
}

export function delReport(id) {
  return request({ url: '/manage/report/' + id, method: 'delete' })
}

export function previewReport(resource) {
  return request({ url: '/manage/report/preview', method: 'get', params: { resource }})
}

export function uploadFromTask(data) {
  return request({ url: '/manage/report/uploadFromTask', method: 'post', data })
}
