import request from '@/utils/request'

// 学生选课
export function selectCourse(studentId, courseClassId) {
  return request({
    url: `/api/course-selection/${studentId}/${courseClassId}`,
    method: 'post'
  })
}

// 退课
export function dropCourse(studentId, courseClassId) {
  return request({
    url: `/api/course-selection/${studentId}/${courseClassId}`,
    method: 'delete'
  })
}

// 获取学生选课记录
export function getStudentSelections(studentId, params) {
  return request({
    url: `/api/course-selection/student/${studentId}`,
    method: 'get',
    params
  })
}

// 获取课程班级选课情况
export function getClassSelections(courseClassId, params) {
  return request({
    url: `/api/course-selection/class/${courseClassId}`,
    method: 'get',
    params
  })
}

// 统计课程班级选课人数
export function getClassSelectionCount(courseClassId) {
  return request({
    url: `/api/course-selection/count/${courseClassId}`,
    method: 'get'
  })
}

// 检查是否已选课
export function checkSelected(studentId, courseClassId) {
  return request({
    url: `/api/course-selection/check-selected/${studentId}/${courseClassId}`,
    method: 'get'
  })
}

// 检查课程是否已满
export function checkClassFull(courseClassId) {
  return request({
    url: `/api/course-selection/check-full/${courseClassId}`,
    method: 'get'
  })
}

// 获取课程统计信息
export function getCourseStatistics(courseId) {
  return request({
    url: `/api/course-selection/statistics/${courseId}`,
    method: 'get'
  })
}
