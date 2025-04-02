import request from '@/utils/request'

// 获取班级列表
export function getClassList(params) {
  return request({
    url: '/api/class/list',
    method: 'get',
    params
  })
}

// 创建班级
export function createClass(data) {
  return request({
    url: '/api/class',
    method: 'post',
    data
  })
}

// 更新班级信息
export function updateClass(data) {
  return request({
    url: `/api/class/${data.id}`,
    method: 'put',
    data
  })
}

// 删除班级
export function deleteClass(id) {
  return request({
    url: `/api/class/${id}`,
    method: 'delete'
  })
}

// 获取班级详情
export function getClassDetail(id) {
  return request({
    url: `/api/class/${id}`,
    method: 'get'
  })
}

// 获取班级学生列表
export function getClassStudents(classId) {
  return request({
    url: `/api/class/${classId}/students`,
    method: 'get'
  })
}

// 添加学生到班级
export function addStudentsToClass(classId, studentIds) {
  return request({
    url: `/api/class/${classId}/students`,
    method: 'post',
    data: { studentIds }
  })
}

// 从班级移除学生
export function removeStudentsFromClass(classId, studentIds) {
  return request({
    url: `/api/class/${classId}/students`,
    method: 'delete',
    data: { studentIds }
  })
}

// 获取班级课程列表
export function getClassCourses(classId) {
  return request({
    url: `/api/class/${classId}/courses`,
    method: 'get'
  })
}

// 获取班级统计信息
export function getClassStats(classId) {
  return request({
    url: `/api/class/${classId}/stats`,
    method: 'get'
  })
}
