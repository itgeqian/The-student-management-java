import request from '@/utils/request'

// 获取用户列表
export function getUserList(params) {
  return request({
    url: '/api/users',
    method: 'get',
    params
  })
}

// 获取用户信息
export function getUserInfo(id) {
  return request({
    url: `/api/users/${id}`,
    method: 'get'
  })
}

// 更新用户信息
export function updateUserInfo(data) {
  return request({
    url: '/api/users',
    method: 'put',
    data
  })
}

// 修改密码
export function updatePassword(data) {
  return request({
    url: '/api/auth/change-password',
    method: 'put',
    data
  })
}

// 重置密码
export function resetPassword(id) {
  return request({
    url: `/api/users/${id}/password/reset`,
    method: 'post'
  })
}

// 删除用户
export function deleteUser(id) {
  return request({
    url: `/api/users/${id}`,
    method: 'delete'
  })
}

// 启用/禁用用户
export function toggleUserStatus(id, status) {
  return request({
    url: `/api/users/${id}/status`,
    method: 'put',
    data: { status }
  })
}
