<template>
  <div class="app-container">
    <div class="filter-container">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="课程班级">
          <el-select
            v-model="queryParams.classId"
            placeholder="请选择课程班级"
            @change="handleClassChange"
          >
            <el-option
              v-for="item in classList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学号">
          <el-input
            v-model="queryParams.studentId"
            placeholder="请输入学号"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>成绩列表</span>
              <el-button type="success" @click="handleExport">导出成绩</el-button>
            </div>
          </template>

          <el-table
            v-loading="loading"
            :data="gradeList"
            border
            style="width: 100%"
          >
            <el-table-column prop="studentId" label="学号" width="120" />
            <el-table-column prop="studentName" label="姓名" width="120" />
            <el-table-column label="作业成绩" width="120">
              <template #default="{ row }">
                {{ row.homeworkGrade || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="实验成绩" width="120">
              <template #default="{ row }">
                {{ row.practiceGrade || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="期末成绩" width="120">
              <template #default="{ row }">
                {{ row.finalGrade || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="总评成绩" width="120">
              <template #default="{ row }">
                {{ row.totalGrade || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="等级" width="100">
              <template #default="{ row }">
                <el-tag :type="getGradeTagType(row.totalGrade)">
                  {{ getGradeLevel(row.totalGrade) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-container">
            <el-pagination
              v-model:current-page="queryParams.pageNum"
              v-model:page-size="queryParams.pageSize"
              :page-sizes="[10, 20, 30, 50]"
              :total="total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>成绩统计</span>
            </div>
          </template>

          <!-- 成绩分布饼图 -->
          <div class="chart-container">
            <div ref="pieChartRef" class="chart"></div>
          </div>

          <!-- 成绩区间统计 -->
          <div class="statistics-container">
            <h4>成绩区间分布</h4>
            <el-table :data="gradeRanges" border style="width: 100%">
              <el-table-column prop="range" label="分数段" width="120" />
              <el-table-column prop="count" label="人数" width="80" />
              <el-table-column prop="percentage" label="占比">
                <template #default="{ row }">
                  <el-progress
                    :percentage="row.percentage"
                    :color="row.color"
                  />
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 基础统计信息 -->
          <div class="statistics-info">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="平均分">
                {{ statistics.average || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="最高分">
                {{ statistics.highest || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="最低分">
                {{ statistics.lowest || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="及格率">
                {{ statistics.passRate || '-' }}%
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getClassList, getGradeList, exportGrades } from '@/api/grade'

const loading = ref(false)
const total = ref(0)
const gradeList = ref([])
const classList = ref([])
const pieChartRef = ref(null)
let pieChart = null

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  classId: undefined,
  studentId: ''
})

// 成绩统计数据
const statistics = reactive({
  average: 0,
  highest: 0,
  lowest: 0,
  passRate: 0
})

// 成绩区间定义
const gradeRanges = ref([
  { range: '90-100', count: 0, percentage: 0, color: '#67C23A' },
  { range: '80-89', count: 0, percentage: 0, color: '#409EFF' },
  { range: '70-79', count: 0, percentage: 0, color: '#E6A23C' },
  { range: '60-69', count: 0, percentage: 0, color: '#F56C6C' },
  { range: '0-59', count: 0, percentage: 0, color: '#909399' }
])

// 获取课程班级列表
const loadClassList = async () => {
  try {
    const { data } = await getClassList()
    classList.value = data
  } catch (error) {
    console.error('Failed to load class list:', error)
    ElMessage.error('获取课程班级列表失败')
  }
}

// 获取成绩列表
const getGrades = async () => {
  if (!queryParams.classId) return
  
  loading.value = true
  try {
    const { data } = await getGradeList(queryParams)
    gradeList.value = data.list
    total.value = data.total
    calculateStatistics()
    updateCharts()
  } catch (error) {
    console.error('Failed to get grades:', error)
    ElMessage.error('获取成绩列表失败')
  } finally {
    loading.value = false
  }
}

// 计算统计数据
const calculateStatistics = () => {
  if (!gradeList.value.length) {
    Object.assign(statistics, {
      average: 0,
      highest: 0,
      lowest: 0,
      passRate: 0
    })
    return
  }

  const grades = gradeList.value
    .map(item => item.totalGrade)
    .filter(grade => grade !== undefined && grade !== null)

  if (!grades.length) return

  statistics.average = (grades.reduce((a, b) => a + b, 0) / grades.length).toFixed(1)
  statistics.highest = Math.max(...grades)
  statistics.lowest = Math.min(...grades)
  statistics.passRate = ((grades.filter(grade => grade >= 60).length / grades.length) * 100).toFixed(1)

  // 计算成绩区间分布
  const totalStudents = grades.length
  gradeRanges.value.forEach(range => {
    const [min, max] = range.range.split('-').map(Number)
    range.count = grades.filter(grade => grade >= min && grade <= max).length
    range.percentage = Number(((range.count / totalStudents) * 100).toFixed(1))
  })
}

// 更新图表
const updateCharts = () => {
  if (!pieChart) {
    pieChart = echarts.init(pieChartRef.value)
  }

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
        data: gradeRanges.value.map(item => ({
          name: item.range,
          value: item.count,
          itemStyle: {
            color: item.color
          }
        })),
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

  pieChart.setOption(option)
}

// 获取成绩等级
const getGradeLevel = (grade) => {
  if (!grade) return '-'
  if (grade >= 90) return '优秀'
  if (grade >= 80) return '良好'
  if (grade >= 70) return '中等'
  if (grade >= 60) return '及格'
  return '不及格'
}

// 获取成绩等级标签类型
const getGradeTagType = (grade) => {
  if (!grade) return ''
  if (grade >= 90) return 'success'
  if (grade >= 80) return 'primary'
  if (grade >= 70) return 'warning'
  if (grade >= 60) return ''
  return 'danger'
}

// 导出成绩
const handleExport = async () => {
  try {
    await exportGrades(queryParams.classId)
    ElMessage.success('成绩导出成功')
  } catch (error) {
    console.error('Failed to export grades:', error)
    ElMessage.error('成绩导出失败')
  }
}

// 查询和重置
const handleQuery = () => {
  queryParams.pageNum = 1
  getGrades()
}

const resetQuery = () => {
  queryParams.studentId = ''
  handleQuery()
}

const handleClassChange = () => {
  handleQuery()
}

const handleSizeChange = (val) => {
  queryParams.pageSize = val
  getGrades()
}

const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  getGrades()
}

// 监听窗口大小变化
window.addEventListener('resize', () => {
  pieChart?.resize()
})

onMounted(() => {
  loadClassList()
})

// 组件卸载时清理图表实例
onBeforeUnmount(() => {
  pieChart?.dispose()
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}

.filter-container {
  padding-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.chart-container {
  margin: 20px 0;

  .chart {
    height: 300px;
  }
}

.statistics-container {
  margin: 20px 0;

  h4 {
    margin-bottom: 15px;
    font-weight: normal;
    color: #606266;
  }
}

.statistics-info {
  margin-top: 20px;
}

:deep(.el-descriptions__cell) {
  padding: 8px !important;
}
</style>
