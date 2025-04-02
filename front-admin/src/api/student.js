import request from '@/utils/request'

// 学生管理
export function createStudent(data) {
  return request({
    url: '/api/admin/students',
    method: 'post',
    data
  })
}

export function updateStudent(data) {
  return request({
    url: '/api/admin/students',
    method: 'put',
    data
  })
}

export function deleteStudent(id) {
  return request({
    url: `/api/admin/students/${id}`,
    method: 'delete'
  })
}

export function getStudent(id) {
  return request({
    url: `/api/admin/students/${id}`,
    method: 'get'
  })
}

export function getStudentList(params) {
  return request({
    url: '/api/admin/students',
    method: 'get',
    params
  })
}

// 学生选课
export function getStudentCourses(params) {
  return request({
    url: '/api/students/courses',
    method: 'get',
    params
  })
}

// 学生作业
export function getStudentHomeworks(params) {
  return request({
    url: '/api/students/homeworks',
    method: 'get',
    params
  })
}

// 学生成绩
export function getStudentGrades(params) {
  return request({
    url: '/api/students/grades',
    method: 'get',
    params
  })
}

// 导入学生
export function importStudents(data) {
  return request({
    url: '/api/admin/students/import',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data
  })
}

// 导出学生
export function exportStudents(params) {
  return request({
    url: '/api/admin/students/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
