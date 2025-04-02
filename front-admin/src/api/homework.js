import request from '@/utils/request'

// 获取作业列表
export function getHomeworkList(params) {
  const { teacherId, ...otherParams } = params
  if (!teacherId) {
    teacherId = 1;
  }
  if (teacherId) {
    return request({
      url: `/api/homework/teacher/${teacherId}`,
      method: 'get',
      params: otherParams
    })
  }
}

// 获取作业详情
export function getHomework(id) {
  return request({
    url: `/api/homework/${id}`,
    method: 'get'
  })
}

// 创建作业
export function createHomework(data) {
  return request({
    url: '/api/homework',
    method: 'post',
    data
  })
}

// 更新作业
export function updateHomework(data) {
  return request({
    url: '/api/homework',
    method: 'put',
    data
  })
}

// 删除作业
export function deleteHomework(id) {
  return request({
    url: `/api/homework/${id}`,
    method: 'delete'
  })
}

// 提交作业
export function submitHomework(data) {
  return request({
    url: `/api/homework/submit`,
    method: 'post',
    data
  })
}

// 评分作业
export function gradeHomework(submissionId, data) {
  return request({
    url: `/api/homework/grade/${submissionId}`,
    method: 'post',
    data
  })
}

export function returnHomework(id) {
  return request({
    url: `/api/homework/submission/${id}`,
    method: 'delete'
  })
}

// 获取作业统计数据
export function getHomeworkStats(homeworkId) {
  return request({
    url: `/api/homework/${homeworkId}/stats`,
    method: 'get'
  })
}

// 获取教师布置的作业
export function getTeacherHomeworks(params) {
  return request({
    url: '/api/homework/teacher',
    method: 'get',
    params
  })
}

// 获取学生的作业
export function getStudentHomeworks(params) {
  const { studentId, ...otherParams } = params
  return request({
    url: `/api/homework/student/${studentId}`,
    method: 'get',
    params: otherParams
  })
}

// 获取学生可提交的作业列表
export function getStudentAvailableHomeworks(studentId, params) {
  return request({
    url: `/api/homework/student/${studentId}/available`,
    method: 'get',
    params
  })
}

// 获取作业提交记录
export function getHomeworkSubmissions(homeworkId, params) {
  return request({
    url: `/api/homework/submission/homework/${homeworkId}`,
    method: 'get',
    params
  })
}

// 获取单个学生的作业提交
export function getStudentSubmission(homeworkId, studentId) {
  return request({
    url: `/api/homework/${homeworkId}/students/${studentId}/submission`,
    method: 'get'
  })
}

// 获取快捷评语列表
export function getQuickComments(homeworkId) {
  return request({
    url: `/api/homework/${homeworkId}/quick-comments`,
    method: 'get'
  })
}

// 添加快捷评语
export function addQuickComment(homeworkId, data) {
  return request({
    url: `/api/homework/${homeworkId}/quick-comments`,
    method: 'post',
    data
  })
}

// 删除快捷评语
export function deleteQuickComment(homeworkId, commentId) {
  return request({
    url: `/api/homework/${homeworkId}/quick-comments/${commentId}`,
    method: 'delete'
  })
}

// 下载作业附件
export function downloadHomeworkFile(fileId) {
  return request({
    url: `/api/homework/files/${fileId}/download`,
    method: 'get',
    responseType: 'blob'
  })
}

// 获取作业模板
export function getHomeworkTemplates() {
  return request({
    url: '/api/homework/templates',
    method: 'get'
  })
}

// 保存作业模板
export function saveHomeworkTemplate(data) {
  return request({
    url: '/api/homework/templates',
    method: 'post',
    data
  })
}

// 获取作业批改进度
export function getGradingProgress(homeworkId) {
  return request({
    url: `/api/homework/${homeworkId}/grading-progress`,
    method: 'get'
  })
}

// 批量评分作业
export function batchGradeHomework(data) {
  return request({
    url: '/api/homework/grade/batch',
    method: 'post',
    data
  })
}

// 获取学生作业成绩列表
export function getStudentHomeworkGrades(studentId, params) {
  return request({
    url: `/api/homework/grades/student/${studentId}`,
    method: 'get',
    params
  })
}

// 获取教师作业提交成绩
export function getTeacherHomeworkGrades(teacherId, params) {
  return request({
    url: `/api/homework/teacher/${teacherId}/grades`,
    method: 'get',
    params: {
      ...params,
      // 如果没有传入作业类型，默认为期末作业
      homeworkType: params.homeworkType
    }
  })
}

// 获取教师成绩构成
export function getTeacherGradeComposition(teacherId) {
  return request({
    url: `/api/homework/teacher/${teacherId}/grade-composition`,
    method: 'get'
  })
}

// 更新教师成绩构成
export function updateTeacherGradeComposition(teacherId, data) {
  return request({
    url: `/api/homework/teacher/${teacherId}/grade-composition`,
    method: 'put',
    data
  })
}
