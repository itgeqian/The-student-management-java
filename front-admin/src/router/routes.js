import Layout from '@/layout/index.vue'

// 所有角色可访问的路由
export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login/index.vue'),
    hidden: true
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [{
      path: 'dashboard',
      name: 'Dashboard',
      component: () => import('@/views/dashboard/index.vue'),
      meta: { title: '首页', icon: 'el-icon-house' }
    }]
  },
  {
    path: '/profile',
    component: Layout,
    children: [{
      path: '',
      name: 'Profile',
      component: () => import('@/views/profile/index.vue'),
      meta: { title: '个人中心', icon: 'el-icon-user' }
    }]
  }
]

// 需要根据角色动态加载的路由
export const asyncRoutes = [
  {
    path: '/system',
    component: Layout,
    redirect: '/system/user',
    meta: { title: '系统管理', icon: 'Setting' },
    children: [
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'department',
        name: 'Department',
        component: () => import('@/views/system/department/index.vue'),
        meta: { title: '学院部门管理' }
      },
      {
        path: 'teacher',
        name: 'Teacher',
        component: () => import('@/views/system/teacher/index.vue'),
        meta: { title: '教师信息管理' }
      },
      {
        path: 'student',
        name: 'Student',
        component: () => import('@/views/system/student/index.vue'),
        meta: { title: '学生信息管理' }
      }
    ]
  },
  {
    path: '/academic',
    component: Layout,
    redirect: '/academic/course',
    meta: { title: '教务管理', icon: 'School', roles: ['admin', 'teacher', 'student'] },
    children: [
      {
        path: 'course',
        name: 'Course',
        component: () => import('@/views/course/index.vue'),
        meta: { title: '课程管理', icon: 'el-icon-notebook-1' },
        children: [
          {
            path: 'list',
            component: () => import('@/views/course/index.vue'),
            name: 'CourseList',
            meta: { title: '课程管理' }
          },
          {
            path: 'class',
            component: () => import('@/views/course/class.vue'),
            name: 'CourseClass',
            meta: { title: '班级管理' }
          },
          {
            path: 'student/selection',
            component: () => import('@/views/course/student/selection.vue'),
            name: 'CourseSelection',
            meta: { title: '选课管理', roles: ['STUDENT'] }
          }
        ]
      },
      {
        path: 'class',
        name: 'CourseClass',
        component: () => import('@/views/course/class.vue'),
        meta: { title: '课程班级管理', icon: 'el-icon-s-home' }
      },
      {
        path: 'class/students/:id',
        name: 'ClassStudents',
        component: () => import('@/views/course/students.vue'),
        meta: { title: '班级学生', icon: 'el-icon-user' },
        hidden: true
      },
      {
        path: 'class/homework/:id',
        name: 'ClassHomework',
        component: () => import('@/views/course/homework.vue'),
        meta: { title: '作业管理', icon: 'el-icon-edit-outline' },
        hidden: true
      },
      {
        path: 'class/grade/:id',
        name: 'ClassGrade',
        component: () => import('@/views/course/grade.vue'),
        meta: { title: '成绩管理', icon: 'el-icon-s-data' },
        hidden: true
      },
      {
        path: 'selection',
        name: 'Selection',
        component: () => import('@/views/academic/selection/index.vue'),
        meta: { title: '选课管理' }
      }
    ]
  },
  {
    path: '/homework',
    component: Layout,
    meta: { title: '作业管理', icon: 'el-icon-edit-outline', roles: ['teacher', 'student'] },
    children: [
      {
        path: 'teacher/list',
        name: 'HomeworkList',
        component: () => import('@/views/homework/index.vue'),
        meta: { title: '作业列表', roles: ['teacher'] }
      },
      {
        path: 'grade/:id',
        name: 'HomeworkGrade',
        component: () => import('@/views/homework/grade.vue'),
        meta: { title: '批改作业', icon: 'el-icon-edit' },
        hidden: true
      },
      {
        path: 'submit/:id',
        name: 'HomeworkSubmit',
        component: () => import('@/views/homework/submit.vue'),
        meta: { title: '提交作业', icon: 'el-icon-upload' },
        hidden: true
      },
      {
        path: 'statistics',
        name: 'HomeworkStatistics',
        component: () => import('@/views/homework/statistics.vue'),
        meta: { title: '作业统计', roles: ['teacher'] }
      },
      {
        path: 'student',
        component: () => import('@/views/homework/student/index.vue'),
        meta: { title: '我的作业', roles: ['student'] },
        children: [
          {
            path: '',
            name: 'StudentHomework',
            component: () => import('@/views/homework/student/list.vue'),
            meta: { title: '作业列表' }
          },
          {
            path: 'detail/:id',
            name: 'StudentHomeworkDetail',
            component: () => import('@/views/homework/detail.vue'),
            meta: { title: '作业详情' }
          }
        ]
      }
    ]
  },
  {
    path: '/grade',
    component: Layout,
    meta: { title: '成绩管理', icon: 'el-icon-data-line', roles: ['teacher', 'student'] },
    children: [
      {
        path: 'composition',
        name: 'GradeComposition',
        component: () => import('@/views/grade/teacher/composition.vue'),
        meta: { title: '成绩构成', roles: ['teacher'] }
      },
      {
        path: 'final',
        name: 'FinalGrade',
        component: () => import('@/views/grade/teacher/final.vue'),
        meta: { title: '末考成绩', roles: ['teacher'] }
      },
      {
        path: 'total',
        name: 'TotalGrade',
        component: () => import('@/views/grade/teacher/total.vue'),
        meta: { title: '总成绩分析', roles: ['teacher'] }
      },
      {
        path: 'publish',
        name: 'PublishGrade',
        component: () => import('@/views/grade/teacher/publish.vue'),
        meta: { title: '成绩发布', roles: ['teacher'] }
      },
      {
        path: 'student',
        name: 'StudentGrade',
        component: () => import('@/views/grade/student/index.vue'),
        meta: { title: '我的成绩', roles: ['student'] }
      }
    ]
  }
]
