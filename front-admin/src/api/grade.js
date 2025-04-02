import request from '@/utils/request'

// 成绩构成管理
export function createGradeStructure(data) {
  return request({
    url: '/api/grades/structure',
    method: 'post',
    data
  })
}

export function updateGradeStructure(data) {
  return request({
    url: '/api/grades/structure',
    method: 'put',
    data
  })
}

export function getGradeStructure(courseId) {
  return request({
    url: `/api/grades/structure/${courseId}`,
    method: 'get'
  })
}

// 设置成绩构成
export function setGradeStructure(data) {
  return request({
    url: '/api/grades/structure',
    method: data.id ? 'put' : 'post',
    data
  })
}

// 成绩管理
export function submitGrade(data) {
  return request({
    url: '/api/grades',
    method: 'post',
    data
  })
}

export function updateGrade(data) {
  return request({
    url: '/api/grades',
    method: 'put',
    data
  })
}

export function deleteGrade(id) {
  return request({
    url: `/api/grades/${id}`,
    method: 'delete'
  })
}

export function getGrade(id) {
  return request({
    url: `/api/grades/${id}`,
    method: 'get'
  })
}

export function getGradeList(params) {
  return request({
    url: '/api/grades',
    method: 'get',
    params
  })
}

// 成绩统计
export function getGradeStats(courseId) {
  return request({
    url: `/api/grades/stats/${courseId}`,
    method: 'get'
  })
}

// 成绩发布
export function publishGrades(courseId) {
  return request({
    url: `/api/grades/publish/${courseId}`,
    method: 'post'
  })
}

// 获取教师课程成绩
export function getTeacherGrades(params) {
  return request({
    url: '/api/grades/teacher',
    method: 'get',
    params
  })
}

// 获取学生成绩
export function getStudentGrades(params) {
  return request({
    url: '/api/grades/student',
    method: 'get',
    params
  })
}

// 导入成绩
export function importGrades(data) {
  return request({
    url: '/api/grades/import',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data
  })
}

// 导出成绩
export function exportGrades(params) {
  return request({
    url: '/api/grades/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 班级管理
export function getClassList(params) {
  return request({
    url: '/api/classes',
    method: 'get',
    params
  })
}

export function addClass(data) {
  return request({
    url: '/api/classes',
    method: 'post',
    data
  })
}

export function updateClass(data) {
  return request({
    url: '/api/classes',
    method: 'put',
    data
  })
}

export function deleteClass(id) {
  return request({
    url: `/api/classes/${id}`,
    method: 'delete'
  })
}

export function getClassStudents(classId) {
  return request({
    url: `/api/classes/${classId}/students`,
    method: 'get'
  })
}

// 作业统计
export function getHomeworkStats(params) {
  return request({
    url: '/api/homework/stats',
    method: 'get',
    params
  })
}

// 获取作业评语快捷录入选项
export function getQuickComments(homeworkId) {
  return request({
    url: `/api/homework/${homeworkId}/quick-comments`,
    method: 'get'
  })
}

// 添加作业评语快捷录入选项
export function addQuickComment(data) {
  return request({
    url: '/api/homework/quick-comments',
    method: 'post',
    data
  })
}

// 获取成绩分布统计
export function getGradeDistribution(courseId) {
  return request({
    url: `/api/grades/${courseId}/distribution`,
    method: 'get'
  })
}

// 获取课程成绩汇总（包含作业、实验、末考成绩）
export function getCourseGradeSummary(courseId) {
  return request({
    url: `/api/grades/${courseId}/summary`,
    method: 'get'
  })
}

// 提交成绩给教务
export function submitGradesToAdmin(courseId) {
  return request({
    url: `/api/grades/${courseId}/submit-to-admin`,
    method: 'post'
  })
}

// 教务审核发布成绩
export function approveAndPublishGrades(courseId) {
  return request({
    url: `/api/grades/${courseId}/approve-and-publish`,
    method: 'post'
  })
}

// 获取教务审核的成绩列表
export function getAdminGradeList(params) {
  return request({
    url: '/grade/admin/list',
    method: 'get',
    params
  })
}

// 获取成绩详情
export function getGradeDetail(id) {
  return request({
    url: `/grade/admin/detail/${id}`,
    method: 'get'
  })
}

// 审核并发布成绩
export function adminApproveAndPublishGrades(id) {
  return request({
    url: `/grade/admin/publish/${id}`,
    method: 'post'
  })
}

// 获取末考成绩列表
export function getFinalGrades(params) {
  return request({
    url: '/api/grades/final',
    method: 'get',
    params
  })
}

// 更新末考成绩
export function updateFinalGrade(data) {
  return request({
    url: '/api/grades/final',
    method: 'put',
    data
  })
}

// 锁定/解锁末考成绩
export function toggleGradeLock(data) {
  return request({
    url: '/api/grades/final/lock',
    method: 'put',
    data
  })
}

// 获取课程班级总成绩列表
export function getClassTotalGrades(params) {
  return request({
    url: `/api/homework/score/total`,
    method: 'get', 
    params
  })
}

// 获取已发布的成绩列表
export function getPublishedGrades(courseClassId, params) {
  return request({
    url: `/api/homework/score/published/${courseClassId}`,
    method: 'get',
    params
  })
}

// 获取班级成绩列表
export function getClassGrades(classId, params) {
  return request({
    url: `/api/grades/class/${classId}`,
    method: 'get',
    params
  })
}
