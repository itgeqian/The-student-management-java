<template>
  <div class="assignment-stats-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="filter-card">
          <el-form :inline="true" :model="filterForm" class="filter-form">
            <el-form-item label="课程">
              <el-select
                v-model="filterForm.courseId"
                placeholder="选择课程"
                clearable
                filterable
                remote
                :remote-method="handleCourseSearch"
                :loading="courseLoading"
              >
                <el-option
                  v-for="item in courseOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="filterForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="resetFilter">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>总作业数</span>
            </div>
          </template>
          <div class="card-content">
            <div class="number">{{ stats.totalAssignments }}</div>
            <div class="trend">
              <span :class="stats.assignmentTrend >= 0 ? 'up' : 'down'">
                {{ Math.abs(stats.assignmentTrend) }}%
              </span>
              较上月
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>平均完成率</span>
            </div>
          </template>
          <div class="card-content">
            <div class="number">{{ stats.avgCompletionRate }}%</div>
            <div class="trend">
              <span :class="stats.completionTrend >= 0 ? 'up' : 'down'">
                {{ Math.abs(stats.completionTrend) }}%
              </span>
              较上月
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>平均分数</span>
            </div>
          </template>
          <div class="card-content">
            <div class="number">{{ stats.avgScore }}</div>
            <div class="trend">
              <span :class="stats.scoreTrend >= 0 ? 'up' : 'down'">
                {{ Math.abs(stats.scoreTrend) }}%
              </span>
              较上月
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>及时提交率</span>
            </div>
          </template>
          <div class="card-content">
            <div class="number">{{ stats.onTimeRate }}%</div>
            <div class="trend">
              <span :class="stats.onTimeTrend >= 0 ? 'up' : 'down'">
                {{ Math.abs(stats.onTimeTrend) }}%
              </span>
              较上月
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>作业提交趋势</span>
            </div>
          </template>
          <div class="chart-container" ref="submissionTrendChart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>分数分布</span>
            </div>
          </template>
          <div class="chart-container" ref="scoreDistributionChart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>课程作业完成情况</span>
            </div>
          </template>
          <div class="chart-container" ref="courseCompletionChart"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import * as echarts from 'echarts'

// 筛选表单
const filterForm = reactive({
  courseId: '',
  dateRange: []
})

// 课程选项
const courseOptions = ref([])
const courseLoading = ref(false)

// 统计数据
const stats = reactive({
  totalAssignments: 156,
  assignmentTrend: 12.5,
  avgCompletionRate: 85.6,
  completionTrend: -2.3,
  avgScore: 82.5,
  scoreTrend: 5.8,
  onTimeRate: 92.3,
  onTimeTrend: 1.5
})

// 图表实例
let submissionTrendChartInstance = null
let scoreDistributionChartInstance = null
let courseCompletionChartInstance = null

// 搜索课程
const handleCourseSearch = (query) => {
  if (query) {
    courseLoading.value = true
    // 模拟远程搜索
    setTimeout(() => {
      courseOptions.value = [
        { value: 1, label: 'Java程序设计' },
        { value: 2, label: '数据结构' },
        { value: 3, label: '操作系统' }
      ].filter(item => item.label.includes(query))
      courseLoading.value = false
    }, 200)
  } else {
    courseOptions.value = []
  }
}

// 搜索
const handleSearch = () => {
  fetchData()
}

// 重置筛选
const resetFilter = () => {
  filterForm.courseId = ''
  filterForm.dateRange = []
  fetchData()
}

// 获取数据
const fetchData = () => {
  // 模拟API调用
  setTimeout(() => {
    initCharts()
  }, 500)
}

// 初始化图表
const initCharts = () => {
  // 作业提交趋势图
  const submissionTrendChart = echarts.init(document.querySelector('.chart-container'))
  submissionTrendChart.setOption({
    title: {
      text: '近30天作业提交趋势'
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: Array.from({ length: 30 }, (_, i) => `12-${i + 1}`)
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '提交数量',
        type: 'line',
        smooth: true,
        data: Array.from({ length: 30 }, () => Math.floor(Math.random() * 50 + 20))
      }
    ]
  })
  submissionTrendChartInstance = submissionTrendChart

  // 分数分布图
  const scoreDistributionChart = echarts.init(document.querySelectorAll('.chart-container')[1])
  scoreDistributionChart.setOption({
    title: {
      text: '分数段分布'
    },
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center'
    },
    series: [
      {
        name: '分数分布',
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
            fontSize: 40,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: 20, name: '90-100分' },
          { value: 35, name: '80-89分' },
          { value: 25, name: '70-79分' },
          { value: 15, name: '60-69分' },
          { value: 5, name: '60分以下' }
        ]
      }
    ]
  })
  scoreDistributionChartInstance = scoreDistributionChart

  // 课程作业完成情况图
  const courseCompletionChart = echarts.init(document.querySelectorAll('.chart-container')[2])
  courseCompletionChart.setOption({
    title: {
      text: '各课程作业完成情况'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['已完成', '进行中', '未开始']
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
      data: ['Java程序设计', '数据结构', '操作系统', '计算机网络', '软件工程']
    },
    series: [
      {
        name: '已完成',
        type: 'bar',
        stack: 'total',
        label: {
          show: true
        },
        emphasis: {
          focus: 'series'
        },
        data: [15, 12, 8, 10, 7]
      },
      {
        name: '进行中',
        type: 'bar',
        stack: 'total',
        label: {
          show: true
        },
        emphasis: {
          focus: 'series'
        },
        data: [5, 3, 4, 2, 3]
      },
      {
        name: '未开始',
        type: 'bar',
        stack: 'total',
        label: {
          show: true
        },
        emphasis: {
          focus: 'series'
        },
        data: [2, 4, 3, 5, 4]
      }
    ]
  })
  courseCompletionChartInstance = courseCompletionChart
}

// 监听窗口大小变化
const handleResize = () => {
  submissionTrendChartInstance?.resize()
  scoreDistributionChartInstance?.resize()
  courseCompletionChartInstance?.resize()
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

// 组件卸载时清理
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  submissionTrendChartInstance?.dispose()
  scoreDistributionChartInstance?.dispose()
  courseCompletionChartInstance?.dispose()
})
</script>

<style scoped>
.assignment-stats-container {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.stats-cards {
  margin-bottom: 20px;
}

.card-content {
  text-align: center;
}

.number {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 10px;
}

.trend {
  font-size: 14px;
  color: #909399;
}

.trend .up {
  color: #67c23a;
}

.trend .down {
  color: #f56c6c;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  margin-bottom: 20px;
}

.chart-container {
  height: 400px;
}
</style>
