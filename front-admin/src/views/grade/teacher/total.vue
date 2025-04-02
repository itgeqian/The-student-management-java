<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>总成绩分析</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="课程班级">
          <el-select 
            v-model="queryParams.courseClassId" 
            placeholder="请选择课程班级" 
            clearable 
            @change="handleClassChange"
            style="width: 300px"
          >
            <el-option
              v-for="item in courseClassList"
              :key="item.id"
              :label="item?.course ? item?.course?.name + '-' + item.className : item.className"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <!-- 成绩分布统计 -->
      <div v-loading="loading" class="analysis-container">
        <div class="stats-cards">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-card shadow="hover">
                <div class="stat-item">
                  <div class="stat-title">班级平均分</div>
                  <div class="stat-value">{{ stats.averageScore || '0.0' }}</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover">
                <div class="stat-item">
                  <div class="stat-title">最高分</div>
                  <div class="stat-value">{{ stats.maxScore || '0.0' }}</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover">
                <div class="stat-item">
                  <div class="stat-title">最低分</div>
                  <div class="stat-value">{{ stats.minScore || '0.0' }}</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover">
                <div class="stat-item">
                  <div class="stat-title">及格率</div>
                  <div class="stat-value">{{ stats.passRate || '0.0' }}%</div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 成绩分布图表 -->
        <div class="charts-container">
          <el-row :gutter="20">
            <el-col :span="12">
              <div ref="distributionChartRef" class="chart-item"></div>
            </el-col>
            <el-col :span="12">
              <div ref="componentChartRef" class="chart-item"></div>
            </el-col>
          </el-row>
        </div>

        <!-- 成绩明细表格 -->
        <div class="detail-table">
          <el-table
            :data="gradeList"
            style="width: 100%"
            :max-height="400"
          >
            <el-table-column prop="studentNo" label="学号" width="120" />
            <el-table-column prop="studentName" label="姓名" width="120" />
            <el-table-column prop="homeworkScore" label="平时作业" width="100" align="center">
              <template #default="{ row }">
                <span :class="getScoreClass(row.homeworkScore)">{{ row.homeworkScore }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="experimentScore" label="实验" width="100" align="center">
              <template #default="{ row }">
                <span :class="getScoreClass(row.experimentScore)">{{ row.experimentScore }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="examScore" label="期末考试" width="100" align="center">
              <template #default="{ row }">
                <span :class="getScoreClass(row.examScore)">{{ row.examScore }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="totalScore" label="总评成绩" width="100" align="center">
              <template #default="{ row }">
                <span :class="getScoreClass(row.totalScore)">{{ row.totalScore }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="rank" label="排名" width="80" align="center" />
          </el-table>
          <div class="pagination">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="total"
              :page-sizes="[10, 20, 50, 100]"
              :page-size="queryParams.size"
              :current-page="queryParams.current"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getCourseClassList } from '@/api/course'
import { getPublishedGrades } from '@/api/grade'

const loading = ref(false)
const courseClassList = ref([])
const stats = ref({
  averageScore: 0,
  maxScore: 0,
  minScore: 0,
  passRate: 0
})
const gradeList = ref([])
const total = ref(0)

const queryParams = ref({
  courseClassId: undefined,
  current: 1,
  size: 10
})

// 图表实例
let distributionChart = null
let componentChart = null
const distributionChartRef = ref(null)
const componentChartRef = ref(null)

// 获取课程班级列表
const getCourseClasses = async () => {
  try {
    const { data } = await getCourseClassList()
    courseClassList.value = data.records
  } catch (error) {
    console.error('获取课程班级列表失败:', error)
    ElMessage.error('获取课程班级列表失败')
  }
}

// 加载成绩数据
const loadGradeData = async (classId) => {
  if (!classId) {
    resetData()
    return
  }

  loading.value = true
  try {
    const { data } = await getPublishedGrades(classId, queryParams.value)
    const records = data.records || []
    total.value = data.total

    // 更新成绩列表
    gradeList.value = records.map(item => ({
      studentNo: item.student?.studentNo,
      studentName: item.student?.name,
      homeworkScore: item.homeworkScore,
      experimentScore: item.experimentScore,
      examScore: item.examScore,
      totalScore: item.totalScore,
      rank: item.rank
    }))

    // 计算统计数据
    if (records.length > 0) {
      const totalScores = records.map(item => item.totalScore).filter(score => score !== null)
      stats.value = {
        averageScore: (totalScores.reduce((a, b) => a + b, 0) / totalScores.length).toFixed(1),
        maxScore: Math.max(...totalScores).toFixed(1),
        minScore: Math.min(...totalScores).toFixed(1),
        passRate: ((totalScores.filter(score => score >= 60).length / totalScores.length) * 100).toFixed(1)
      }

      // 计算分数段分布
      const distribution = [0, 0, 0, 0, 0] // <60, 60-69, 70-79, 80-89, 90-100
      totalScores.forEach(score => {
        if (score < 60) distribution[0]++
        else if (score < 70) distribution[1]++
        else if (score < 80) distribution[2]++
        else if (score < 90) distribution[3]++
        else distribution[4]++
      })

      // 更新图表
      initDistributionChart(distribution)

      // 获取权重
      const weights = records[0].courseClass || {}
      initComponentChart([
        { value: weights.homeworkWeight || 30, name: '平时作业' },
        { value: weights.experimentWeight || 30, name: '实验' },
        { value: weights.finalExamWeight || 40, name: '期末考试' }
      ])
    } else {
      resetData()
    }
  } catch (error) {
    console.error('获取成绩数据失败:', error)
    ElMessage.error('获取成绩数据失败')
    resetData()
  } finally {
    loading.value = false
  }
}

// 处理班级变更
const handleClassChange = (value) => {
  // queryParams.value.current = 1
  loadGradeData(value)
}

// 处理分页变化
const handleSizeChange = (val) => {
  queryParams.value.size = val
  loadGradeData(queryParams.value.courseClassId)
}

const handleCurrentChange = (val) => {
  queryParams.value.current = val
  loadGradeData(queryParams.value.courseClassId)
}

// 初始化成绩分布图表
const initDistributionChart = (data) => {
  if (!distributionChartRef.value) return

  if (!distributionChart) {
    distributionChart = echarts.init(distributionChartRef.value)
  }

  const option = {
    title: {
      text: '成绩分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['<60', '60-69', '70-79', '80-89', '90-100']
    },
    yAxis: {
      type: 'value',
      name: '人数'
    },
    series: [
      {
        name: '学生人数',
        type: 'bar',
        data: data,
        itemStyle: {
          color: '#409EFF'
        }
      }
    ]
  }

  distributionChart.setOption(option)
}

// 初始化成绩构成图表
const initComponentChart = (data) => {
  if (!componentChartRef.value) return

  if (!componentChart) {
    componentChart = echarts.init(componentChartRef.value)
  }

  const option = {
    title: {
      text: '成绩构成',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}%'
    },
    series: [
      {
        type: 'pie',
        radius: ['50%', '70%'],
        avoidLabelOverlap: false,
        label: {
          show: true,
          position: 'outside',
          formatter: '{b}: {c}%'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '16',
            fontWeight: 'bold'
          }
        },
        data: data
      }
    ]
  }

  componentChart.setOption(option)
}

// 重置数据
const resetData = () => {
  stats.value = {
    averageScore: 0,
    maxScore: 0,
    minScore: 0,
    passRate: 0
  }
  gradeList.value = []
  initDistributionChart([])
  initComponentChart([])
}

// 获取成绩样式类
const getScoreClass = (score) => {
  if (score === undefined || score === null) return ''
  if (score >= 90) return 'score-excellent'
  if (score >= 80) return 'score-good'
  if (score >= 70) return 'score-fair'
  if (score >= 60) return 'score-pass'
  return 'score-fail'
}

// 监听窗口大小变化
const handleResize = () => {
  distributionChart?.resize()
  componentChart?.resize()
}

onMounted(() => {
  getCourseClasses()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  distributionChart?.dispose()
  componentChart?.dispose()
})
</script>

<style scoped>
.analysis-container {
  padding: 20px 0;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  padding: 10px;
}

.stat-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.charts-container {
  margin: 20px 0;
}

.chart-item {
  height: 400px;
  width: 100%;
}

.detail-table {
  margin-top: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

.score-excellent {
  color: #67C23A;
  font-weight: bold;
}

.score-good {
  color: #409EFF;
  font-weight: bold;
}

.score-fair {
  color: #E6A23C;
  font-weight: bold;
}

.score-pass {
  color: #909399;
}

.score-fail {
  color: #F56C6C;
  font-weight: bold;
}

.demo-form-inline {
  margin-bottom: 20px;
}

:deep(.el-select) {
  width: 300px;
}

:deep(.el-select-dropdown__item) {
  white-space: normal;
  height: auto;
  padding: 8px 12px;
  line-height: 1.5;
}
</style>
