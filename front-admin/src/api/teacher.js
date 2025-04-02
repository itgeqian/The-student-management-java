import request from '@/utils/request'

// 教师管理
export function createTeacher(data) {
  return request({
    url: '/api/admin/teachers',
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

// 教师课程
export function getTeacherCourses(params) {
  return request({
    url: '/api/teachers/courses',
    method: 'get',
    params
  })
}

// 教师作业
export function getTeacherHomeworks(params) {
  return request({
    url: '/api/teachers/homeworks',
    method: 'get',
    params
  })
}

// 教师成绩管理
export function getTeacherGrades(params) {
  return request({
    url: '/api/teachers/grades',
    method: 'get',
    params
  })
}

// 导入教师
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

// 导出教师
export function exportTeachers(params) {
  return request({
    url: '/api/admin/teachers/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 获取教师列表（不分页）
export function getTeacherListNoPage() {
  return request({
    url: '/api/admin/teachers',
    method: 'get',
    params: {
      page: 1,
      limit: 10000 // 设置一个足够大的数量来获取所有数据
    }
  })
}
