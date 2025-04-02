import request from '@/utils/request'

// 获取部门列表（不分页）
export function getDepartmentList(params) {
  return request({
    url: '/api/departments',
    method: 'get',
    params
  })
}

// 获取部门列表（分页）
export function getDepartmentPage(params) {
  return request({
    url: '/api/departments/page',
    method: 'get',
    params
  })
}

// 新增部门
export function createDepartment(data) {
  return request({
    url: '/api/departments',
    method: 'post',
    data
  })
}

// 修改部门
export function updateDepartment(data) {
  return request({
    url: '/api/departments',
    method: 'put',
    data
  })
}

// 删除部门
export function deleteDepartment(id) {
  return request({
    url: `/api/departments/${id}`,
    method: 'delete'
  })
}
