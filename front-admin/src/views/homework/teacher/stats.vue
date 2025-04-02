<template>
  <div class="app-container">
    <!-- 查询表单 -->
    <div class="filter-container">
      <el-form :inline="true" :model="queryParams" class="form-inline">
        <el-form-item label="课程班级">
          <el-select v-model="queryParams.courseClassId" placeholder="请选择课程班级" clearable>
            <el-option
              v-for="item in courseClassList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="作业">
          <el-select v-model="queryParams.homeworkId" placeholder="请选择作业" clearable>
            <el-option
              v-for="item in homeworkList"
              :key="item.id"
              :label="item.title"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 统计数据展示 -->
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>提交情况</span>
            </div>
          </template>
          <div v-loading="loading">
            <el-progress
              type="dashboard"
              :percentage="submitStats.submitRate"
              :color="customColors"
            >
              <template #default="{ percentage }">
                <span class="percentage-value">{{ percentage }}%</span>
                <span class="percentage-label">提交率</span>
              </template>
            </el-progress>
            <div class="stats-info">
              <p>应交人数：{{ submitStats.totalCount }}</p>
              <p>已交人数：{{ submitStats.submittedCount }}</p>
              <p>未交人数：{{ submitStats.unsubmittedCount }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>成绩分布</span>
            </div>
          </template>
          <div v-loading="loading" ref="gradeChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>基本统计</span>
            </div>
          </template>
          <div v-loading="loading" class="stats-info">
            <p>最高分：{{ gradeStats.maxScore }}</p>
            <p>最低分：{{ gradeStats.minScore }}</p>
            <p>平均分：{{ gradeStats.avgScore }}</p>
            <p>及格率：{{ gradeStats.passRate }}%</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 学生列表 -->
    <el-card class="box-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>学生成绩明细</span>
          <el-button type="primary" link @click="handleExport">导出Excel</el-button>
        </div>
      </template>
      <el-table
        v-loading="loading"
        :data="studentList"
        style="width: 100%"
        border
      >
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="studentName" label="姓名" width="120" />
        <el-table-column prop="className" label="班级" width="150" />
        <el-table-column prop="submitTime" label="提交时间" width="180">
          <template #default="{ row }">
            <span>{{ formatDateTime(row.submitTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="成绩" width="100" align="center" />
        <el-table-column prop="comment" label="评语" min-width="200" show-overflow-tooltip />
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getHomeworkStats } from '@/api/grade'
import { getTeacherCourseClasses } from '@/api/course'
import { getHomeworkList } from '@/api/homework'
import { formatDateTime } from '@/utils/format'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 查询参数
const queryParams = ref({
  current: 1,
  size: 10,
  courseClassId: undefined,
  homeworkId: undefined
})

// 数据列表
const loading = ref(false)
const total = ref(0)
const courseClassList = ref([])
const homeworkList = ref([])
const studentList = ref([])

// 统计数据
const submitStats = ref({
  totalCount: 0,
  submittedCount: 0,
  unsubmittedCount: 0,
  submitRate: 0
})

const gradeStats = ref({
  maxScore: 0,
  minScore: 0,
  avgScore: 0,
  passRate: 0
})

// 图表相关
const gradeChartRef = ref(null)
let gradeChart = null

const customColors = [
  { color: '#f56c6c', percentage: 60 },
  { color: '#e6a23c', percentage: 80 },
  { color: '#67c23a', percentage: 100 }
]

// 初始化图表
const initChart = () => {
  if (gradeChart) {
    gradeChart.dispose()
  }
  gradeChart = echarts.init(gradeChartRef.value)
}

// 更新图表数据
const updateChart = (data) => {
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        type: 'pie',
        radius: '50%',
        data: [
          { value: data.excellent, name: '优秀(90-100)' },
          { value: data.good, name: '良好(80-89)' },
          { value: data.fair, name: '中等(70-79)' },
          { value: data.pass, name: '及格(60-69)' },
          { value: data.fail, name: '不及格(0-59)' }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  gradeChart.setOption(option)
}

// 获取课程班级列表
const getCourseClasses = async () => {
  try {
    const { data } = await getTeacherCourseClasses(userStore?.userInfo?.id || 1)
    courseClassList.value = data
  } catch (error) {
    console.error('获取课程班级列表失败:', error)
    ElMessage.error('获取课程班级列表失败')
  }
}

// 获取作业列表
const getHomeworks = async () => {
  if (!queryParams.value.courseClassId) {
    homeworkList.value = []
    return
  }

  try {
    const { data } = await getHomeworkList({
      courseClassId: queryParams.value.courseClassId
    })
    homeworkList.value = data
  } catch (error) {
    console.error('获取作业列表失败:', error)
    ElMessage.error('获取作业列表失败')
  }
}

// 获取统计数据
const getStats = async () => {
  loading.value = true
  try {
    const { data } = await getHomeworkStats(queryParams.value)
    submitStats.value = data.submitStats
    gradeStats.value = data.gradeStats
    studentList.value = data.students
    total.value = data.total

    nextTick(() => {
      updateChart(data.gradeDistribution)
    })
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  } finally {
    loading.value = false
  }
}

// 查询操作
const handleQuery = () => {
  // queryParams.value.current = 1
  getStats()
}

// 重置操作
const resetQuery = () => {
  queryParams.value = {
    current: 1,
    size: 10,
    courseClassId: undefined,
    homeworkId: undefined
  }
  getStats()
}

// 分页操作
const handleSizeChange = (val) => {
  queryParams.value.size = val
  getStats()
}

const handleCurrentChange = (val) => {
  queryParams.value.current = val
  getStats()
}

// 导出Excel
const handleExport = () => {
  // TODO: 实现导出功能
  ElMessage.success('导出成功')
}

// 监听课程班级变化
watch(
  () => queryParams.value.courseClassId,
  () => {
    queryParams.value.homeworkId = undefined
    getHomeworks()
  }
)

onMounted(async () => {
  // 初始化图表
  initChart()
  
  // 获取初始数据
  await getCourseClasses()
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    gradeChart?.resize()
  })
})

onBeforeUnmount(() => {
  // 清理图表实例
  if (gradeChart) {
    gradeChart.dispose()
    gradeChart = null
  }
  
  // 移除事件监听
  window.removeEventListener('resize', () => {
    gradeChart?.resize()
  })
})
</script>

<style scoped>
.filter-container {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 300px;
}

.stats-info {
  margin-top: 20px;
  text-align: center;
}

.stats-info p {
  margin: 10px 0;
  font-size: 14px;
}

.percentage-value {
  display: block;
  font-size: 28px;
  font-weight: bold;
}

.percentage-label {
  display: block;
  font-size: 14px;
  color: #909399;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>
