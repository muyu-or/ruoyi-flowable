import request from '@/utils/request'

// 查询测试报告模板配置列表
export function listTemplate(query) {
  return request({
    url: '/manage/template/list',
    method: 'get',
    params: query
  })
}

// 查询测试报告模板配置详细
export function getTemplate(id) {
  return request({
    url: '/manage/template/' + id,
    method: 'get'
  })
}

// 新增测试报告模板配置
export function addTemplate(data) {
  return request({
    url: '/manage/template',
    method: 'post',
    data: data
  })
}

// 修改测试报告模板配置
export function updateTemplate(data) {
  return request({
    url: '/manage/template',
    method: 'put',
    data: data
  })
}

// 删除测试报告模板配置
export function delTemplate(id) {
  return request({
    url: '/manage/template/' + id,
    method: 'delete'
  })
}
