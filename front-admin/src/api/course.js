import request from '@/utils/request'

// 课程管理
export function createCourse(data) {
  return request({
    url: '/api/courses',
    method: 'post',
    data
  })
}

export function updateCourse(data) {
  return request({
    url: '/api/courses',
    method: 'put',
    data
  })
}

export function deleteCourse(id) {
  return request({
    url: `/api/courses/${id}`,
    method: 'delete'
  })
}

export function getCourse(id) {
  return request({
    url: `/api/courses/${id}`,
    method: 'get'
  })
}

export function getCourseList(params) {
  return request({
    url: '/api/courses',
    method: 'get',
    params
  })
}

// 课程班级管理
export function createCourseClass(data) {
  return request({
    url: '/api/course-class',
    method: 'post',
    data
  })
}

export function updateCourseClass(data) {
  return request({
    url: '/api/course-class',
    method: 'put',
    data
  })
}

export function deleteCourseClass(id) {
  return request({
    url: `/api/course-class/${id}`,
    method: 'delete'
  })
}

export function getCourseClass(id) {
  return request({
    url: `/api/course-class/${id}`,
    method: 'get'
  })
}

export function getCourseClassList(params) {
  return request({
    url: '/api/course-class',
    method: 'get',
    params
  })
}

// 获取教师的班级列表
export function getTeacherCourseClasses(teacherId, params) {
  return request({
    url: `/api/course-class/teacher/${teacherId}`,
    method: 'get',
    params
  })
}

// 获取学生的班级列表
export function getStudentCourseClasses(studentId, params) {
  return request({
    url: `/api/course-class/student/${studentId}`,
    method: 'get',
    params
  })
}

// 班级学生管理
export function getClassStudents(classId, params) {
  return request({
    url: `/api/course-class/${classId}/students`,
    method: 'get',
    params
  })
}

export function removeClassStudent(classId, studentId) {
  return request({
    url: `/api/course-class/${classId}/student/${studentId}`,
    method: 'delete'
  })
}

// 学生选课
export function joinCourseClass(classId, data) {
  return request({
    url: `/api/course-class/${classId}/join`,
    method: 'post',
    data
  })
}

export function quitCourseClass(classId, data) {
  return request({
    url: `/api/course-class/${classId}/quit`,
    method: 'post',
    data
  })
}

// 成绩构成管理
export function getGradeStructure(classId) {
  return request({
    url: `/api/grades/composition/${classId}`,
    method: 'get'
  })
}

export function updateGradeStructure(classId, data) {
  return request({
    url: `/api/grades/composition/${classId}`,
    method: 'post',
    data: {
      homeworkWeight: data.homeworkWeight,
      experimentWeight: data.experimentWeight,
      finalExamWeight: data.finalExamWeight
    }
  })
}

// 成绩管理
export function getStudentGrades(classId, params) {
  return request({
    url: `/api/grades/class/${classId}`,
    method: 'get',
    params
  })
}

export function updateStudentGrade(classId, studentId, data) {
  return request({
    url: `/api/grades/exam/${studentId}/${classId}`,
    method: 'post',
    data: {
      score: data.score
    }
  })
}

export function batchUpdateGrades(classId, data) {
  return request({
    url: `/api/grades/exam/batch/${classId}`,
    method: 'post',
    data: {
      grades: data.grades.map(item => ({
        studentId: item.studentId,
        score: item.score
      }))
    }
  })
}

export function submitGrades(classId) {
  return request({
    url: `/api/grades/submit/${classId}`,
    method: 'post'
  })
}

export function publishGrades(classId) {
  return request({
    url: `/api/grades/publish/${classId}`,
    method: 'post'
  })
}

export function checkGradesPublished(classId) {
  return request({
    url: `/api/grades/published/${classId}`,
    method: 'get'
  })
}

export function getGradeStats(classId) {
  return request({
    url: `/api/grades/stats/${classId}`,
    method: 'get'
  })
}

export function searchStudentGrades(classId, studentNumber) {
  return request({
    url: `/api/grades/search/${classId}`,
    method: 'get',
    params: { studentNumber }
  })
}

// 选课管理
export function createCourseSelection(data) {
  return request({
    url: '/api/courses/selections',
    method: 'post',
    data
  })
}

export function updateCourseSelection(data) {
  return request({
    url: '/api/courses/selections',
    method: 'put',
    data
  })
}

export function deleteCourseSelection(id) {
  return request({
    url: `/api/courses/selections/${id}`,
    method: 'delete'
  })
}

export function getCourseSelection(id) {
  return request({
    url: `/api/courses/selections/${id}`,
    method: 'get'
  })
}

export function getCourseSelectionList(params) {
  return request({
    url: '/api/courses/selections',
    method: 'get',
    params
  })
}

// 学生选课
export function selectCourse(data) {
  return request({
    url: '/api/courses/select',
    method: 'post',
    data
  })
}

export function withdrawCourse(id) {
  return request({
    url: `/api/courses/withdraw/${id}`,
    method: 'post'
  })
}

// 教师课程
export function getTeacherCourses(params) {
  return request({
    url: '/api/courses/teacher',
    method: 'get',
    params
  })
}

// 学生课程
export function getStudentCourses(params) {
  return request({
    url: '/api/courses/student',
    method: 'get',
    params
  })
}

// 导入导出
export function importCourses(data) {
  return request({
    url: '/api/courses/import',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data
  })
}

export function exportCourses(params) {
  return request({
    url: '/api/courses/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
