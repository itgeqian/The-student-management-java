<template>
  <div class="page-container">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <h1 class="welcome-title">
        {{ getWelcomeText() }}
        <span class="user-name">{{ userInfo?.name || userInfo?.username || '未知用户' }}</span>
      </h1>
      <p class="welcome-subtitle">{{ getCurrentTime() }}</p>
      <p class="welcome-message">{{ getMotivationalText() }}</p>
      <div class="user-info">
        <el-tag :type="getRoleType()">{{ formatRole(userInfo?.role) }}</el-tag>
        <span class="login-info">上次登录: {{ formatDate(userInfo.lastLoginTime) }}</span>
      </div>
    </div>

    <!-- 数据统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stats-card">
            <div class="stats-icon course-icon">
              <el-icon><Reading /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-title">课程总数</div>
              <div class="stats-number">{{ stats.courseCount }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stats-card">
            <div class="stats-icon class-icon">
              <el-icon><OfficeBuilding /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-title">班级总数</div>
              <div class="stats-number">{{ stats.classCount }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stats-card">
            <div class="stats-icon homework-icon">
              <el-icon><Notebook /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-title">作业总数</div>
              <div class="stats-number">{{ stats.homeworkCount }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stats-card">
            <div class="stats-icon student-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-title">学生总数</div>
              <div class="stats-number">{{ stats.studentCount }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="hover" class="chart-card">
            <div class="chart-title">课程分布</div>
            <div ref="courseChartRef" style="height: 300px"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover" class="chart-card">
            <div class="chart-title">作业提交情况</div>
            <div ref="homeworkChartRef" style="height: 300px"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, reactive } from 'vue'
import { useUserStore } from '@/stores/user'
import * as echarts from 'echarts'
import { Reading, OfficeBuilding, Notebook, User } from '@element-plus/icons-vue'
import { getCourseList, getCourseClassList } from '@/api/course'
import { getHomeworkList, getStudentHomeworks } from '@/api/homework'
import { getStudentList } from '@/api/student'

const userStore = useUserStore()
const userInfo = userStore.userInfo
const courseChartRef = ref(null)
const homeworkChartRef = ref(null)
let courseChart = null
let homeworkChart = null

const stats = reactive({
  courseCount: 0,
  classCount: 0,
  homeworkCount: 0,
  studentCount: 0
})

// 获取欢迎文本
const getWelcomeText = () => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 17) return '下午好'
  if (hour < 19) return '傍晚好'
  return '晚上好'
}

// 获取当前时间
const getCurrentTime = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  const date = now.getDate()
  const day = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'][now.getDay()]
  return `${year}年${month}月${date}日 ${day}`
}

// 获取激励文本
const getMotivationalText = () => {
  const texts = [
    '今天也要继续加油哦！',
    '新的一天，新的开始！',
    '愿你的付出都有收获！',
    '保持热爱，奋斗不止！'
  ]
  return texts[Math.floor(Math.random() * texts.length)]
}

// 格式化角色
const formatRole = (role) => {
  const roleMap = {
    admin: '管理员',
    teacher: '教师',
    student: '学生'
  }
  return roleMap[role] || '未知角色'
}

// 获取角色标签类型
const getRoleType = () => {
  const typeMap = {
    admin: 'danger',
    teacher: 'warning',
    student: 'success'
  }
  return typeMap[userInfo?.role] || 'info'
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '暂无记录'
  return new Date(date).toLocaleString()
}

// 初始化课程分布图表
const initCourseChart = (data) => {
  if (!courseChartRef.value) return
  
  courseChart = echarts.init(courseChartRef.value)
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center'
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '20',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: data
      }
    ]
  }
  courseChart.setOption(option)
}

// 初始化作业提交图表
const initHomeworkChart = (data) => {
  if (!homeworkChartRef.value) return
  
  homeworkChart = echarts.init(homeworkChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: data.map(item => item.name)
    },
    series: [
      {
        name: '提交数量',
        type: 'bar',
        data: data.map(item => item.value)
      }
    ]
  }
  homeworkChart.setOption(option)
}

// 获取统计数据
const fetchStats = async () => {
  try {
    // 根据用户角色获取作业列表
    let getHomeworks = userStore.userInfo.role === 'teacher' 
      ? () => getHomeworkList({ teacherId: userStore?.userInfo?.teacherInfo?.id })
      : () => getHomeworkList({ teacherId: userStore?.userInfo?.id })

    let [courses, classes, homework, students] = await Promise.all([
      getCourseList(),
      getCourseClassList(),
      getHomeworks(),
      getStudentList()
    ])
    
    courses = courses.data.records || []
    classes = classes.data.records || []
    homework = homework.data.records || []
    students = students.data.records || []
    // 更新统计数据
    stats.courseCount = courses.total || courses?.length || 0
    stats.classCount = classes.total || classes?.length || 0
    stats.homeworkCount = homework.total || homework?.length || 0
    stats.studentCount = students.total || students?.length || 0
    
    // 处理课程分布数据
    let courseData = courses?.slice(0, 5).map(course => ({
      name: course.name,
      value: course.studentCount || 1
    })) || []
    
    // 处理作业提交数据
    let homeworkData = homework?.slice(0, 5).map(hw => ({
      name: hw.title,
      value: hw.submitCount || 0
    })) || []
    
    // 初始化图表
    initCourseChart(courseData)
    initHomeworkChart(homeworkData)
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 监听窗口大小变化
const handleResize = () => {
  courseChart?.resize()
  homeworkChart?.resize()
}

onMounted(() => {
  fetchStats()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  courseChart?.dispose()
  homeworkChart?.dispose()
})
</script>

<style lang="scss" scoped>
.page-container {
  padding: 20px;
  
  .welcome-section {
    background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
    color: white;
    padding: 24px;
    border-radius: 8px;
    margin-bottom: 24px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    
    .welcome-title {
      font-size: 24px;
      margin: 0;
      .user-name {
        font-weight: bold;
        margin-left: 8px;
      }
    }
    
    .welcome-subtitle {
      margin: 8px 0;
      opacity: 0.8;
    }
    
    .welcome-message {
      font-size: 16px;
      margin: 12px 0;
    }
    
    .user-info {
      margin-top: 16px;
      .login-info {
        margin-left: 12px;
        opacity: 0.8;
      }
    }
  }
  
  .stats-cards {
    margin-bottom: 24px;
    
    .stats-card {
      height: 120px;
      display: flex;
      align-items: center;
      padding: 20px;
      transition: all 0.3s;
      
      &:hover {
        transform: translateY(-5px);
      }
      
      .stats-icon {
        width: 48px;
        height: 48px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 16px;
        
        :deep(.el-icon) {
          font-size: 24px;
          color: white;
        }
        
        &.course-icon {
          background-color: #1890ff;
        }
        
        &.class-icon {
          background-color: #52c41a;
        }
        
        &.homework-icon {
          background-color: #faad14;
        }
        
        &.student-icon {
          background-color: #f5222d;
        }
      }
      
      .stats-info {
        flex: 1;
        
        .stats-title {
          font-size: 14px;
          color: #666;
          margin-bottom: 8px;
        }
        
        .stats-number {
          font-size: 24px;
          font-weight: bold;
          color: #333;
        }
      }
    }
  }
  
  .charts-section {
    .chart-card {
      margin-bottom: 24px;
      
      .chart-title {
        font-size: 16px;
        color: #333;
        margin-bottom: 16px;
        padding-left: 8px;
        border-left: 4px solid #1890ff;
      }
    }
  }
}
</style>