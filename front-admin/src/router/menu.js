// 基础路由 - 所有用户可见
export const baseMenus = [
  {
    path: 'dashboard',
    name: 'Dashboard',
    meta: { title: '首页', icon: 'House' }
  },
  {
    path: 'profile',
    name: 'Profile',
    meta: { title: '个人中心', icon: 'User' }
  }
]

// 管理员菜单
export const adminMenus = [
  {
    path: 'system',
    name: 'System',
    meta: { title: '系统管理', icon: 'Setting' },
    children: [
      {
        path: 'user',
        name: 'User',
        meta: { title: '用户管理' }
      },
      {
        path: 'department',
        name: 'Department',
        meta: { title: '学院部门管理' }
      },
      {
        path: 'teacher',
        name: 'Teacher',
        meta: { title: '教师信息管理' }
      },
      {
        path: 'student',
        name: 'Student',
        meta: { title: '学生信息管理' }
      }
    ]
  },
  {
    path: 'academic',
    name: 'Academic',
    meta: { title: '教务管理', icon: 'School' },
    children: [
      {
        path: 'course',
        name: 'Course',
        meta: { title: '课程管理' }
      },
      {
        path: 'class',
        name: 'Class',
        meta: { title: '课程班级管理' }
      },
      {
        path: 'selection',
        name: 'Selection',
        meta: { title: '选课管理' }
      }
    ]
  }
]

// 教师菜单
export const teacherMenus = [
  {
    path: 'homework',
    name: 'Homework',
    meta: { title: '作业管理', icon: 'Edit' },
    children: [
      {
        path: 'teacher/list',
        name: 'list',
        meta: { title: '作业列表' }
      }
    ]
  },
  {
    path: 'academic',
    name: 'Academic',
    meta: { title: '教务管理', icon: 'School' },
    children: [
      {
        path: 'selection',
        name: 'Selection',
        meta: { title: '选课管理' }
      }
    ]
  },
  {
    path: 'grade',
    name: 'Grade',
    meta: { title: '成绩管理', icon: 'DataLine' },
    children: [
      {
        path: 'composition',
        name: 'GradeComposition',
        meta: { title: '成绩构成' }
      },
      {
        path: 'final',
        name: 'FinalGrade',
        meta: { title: '末考成绩' }
      },
      {
        path: 'total',
        name: 'TotalGrade',
        meta: { title: '总成绩分析' }
      },
      {
        path: 'publish',
        name: 'PublishGrade',
        meta: { title: '成绩发布' }
      }
    ]
  }
]

// 学生菜单
export const studentMenus = [
  {
    path: '/homework',
    name: 'Homework',
    meta: { title: '作业管理', icon: 'Edit' },
    children: [
      {
        path: 'student',
        name: 'StudentHomework',
        meta: { title: '我的作业' }
      }
    ]
  },
  {
    path: 'academic',
    name: 'Academic',
    meta: { title: '教务管理', icon: 'School' },
    children: [
      {
        path: 'selection',
        name: 'Selection',
        meta: { title: '选课管理' }
      }
    ]
  },
  {
    path: '/grade',
    name: 'Grade',
    meta: { title: '成绩管理', icon: 'DataLine' },
    children: [
      {
        path: 'student',
        name: 'StudentGrade',
        meta: { title: '我的成绩' }
      }
    ]
  }
]

// 根据角色获取菜单
export function getMenusByRole(role = '') {
  console.log('Getting menus for role:', role)
  const menus = [...baseMenus]

  // 转换角色为小写并去除空格
  const normalizedRole = (role || '').toLowerCase().trim()
  console.log('Normalized role:', normalizedRole)

  switch (normalizedRole) {
    case 'admin':
      console.log('Returning admin menus')
      return menus.concat(adminMenus)
    case 'teacher':
      console.log('Returning teacher menus')
      return menus.concat(teacherMenus)
    case 'student':
      console.log('Returning student menus')
      return menus.concat(studentMenus)
    default:
      console.log('Returning base menus')
      return menus
  }
}
