import request from '@/utils/request'

// 查询产线班组列表
export function listTeam(query) {
  return request({
    url: '/manage/team/list',
    method: 'get',
    params: query
  })
}

// 查询产线班组详细
export function getTeam(id) {
  return request({
    url: '/manage/team/' + id,
    method: 'get'
  })
}

// 新增产线班组
export function addTeam(data) {
  return request({
    url: '/manage/team',
    method: 'post',
    data: data
  })
}

// 修改产线班组
export function updateTeam(data) {
  return request({
    url: '/manage/team',
    method: 'put',
    data: data
  })
}

// 删除产线班组
export function delTeam(id) {
  return request({
    url: '/manage/team/' + id,
    method: 'delete'
  })
}

// 查询用户列表（含角色名称）
export function listTeamUser(query) {
  return request({
    url: '/manage/team/userList',
    method: 'get',
    params: query
  })
}
