import request from '@/utils/request'

// 用户管理
export function createUser(data) {
  return request({
    url: '/api/admin/users',
    method: 'post',
    data
  })
}

export function updateUser(data) {
  return request({
    url: '/api/admin/users',
    method: 'put',
    data
  })
}

export function deleteUser(id) {
  return request({
    url: `/api/admin/users/${id}`,
    method: 'delete'
  })
}

export function getUser(id) {
  return request({
    url: `/api/admin/users/${id}`,
    method: 'get'
  })
}

export function getUserList(params) {
  return request({
    url: '/api/admin/users',
    method: 'get',
    params
  })
}

export function resetUserPassword(id) {
  return request({
    url: `/api/auth/${id}/reset-password`,
    method: 'post'
  })
}

// 教师管理
export function createTeacher(data) {
  return request({
    url: '/api/admin/teachers',
    method: 'post',
    data
  })
}

export function batchCreateTeachers(data) {
  return request({
    url: '/api/admin/teachers/batch',
    method: 'post',
    data
  })
}

export function updateTeacher(data) {
  return request({
    url: '/api/admin/teachers',
    method: 'put',
    data
  })
}

export function deleteTeacher(id) {
  return request({
    url: `/api/admin/teachers/${id}`,
    method: 'delete'
  })
}

export function batchDeleteTeachers(data) {
  return request({
    url: '/api/admin/teachers/batch',
    method: 'delete',
    data
  })
}

export function getTeacher(id) {
  return request({
    url: `/api/admin/teachers/${id}`,
    method: 'get'
  })
}

export function getTeacherList(params) {
  return request({
    url: '/api/admin/teachers',
    method: 'get',
    params
  })
}

export function importTeachers(data) {
  return request({
    url: '/api/admin/teachers/import',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data
  })
}

// 院系管理
export function createDepartment(data) {
  return request({
    url: '/api/departments',
    method: 'post',
    data
  })
}

export function updateDepartment(data) {
  return request({
    url: '/api/departments',
    method: 'put',
    data
  })
}

export function deleteDepartment(id) {
  return request({
    url: `/api/departments/${id}`,
    method: 'delete'
  })
}

export function getDepartment(id) {
  return request({
    url: `/api/departments/${id}`,
    method: 'get'
  })
}

export function getDepartmentList(params) {
  return request({
    url: '/api/departments',
    method: 'get',
    params
  })
}
