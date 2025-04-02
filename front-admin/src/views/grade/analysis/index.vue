<template>
  <div class="grade-analysis">
    <el-card class="filter-container">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="课程">
          <el-select v-model="queryParams.courseId" placeholder="选择课程" clearable>
            <el-option
              v-for="item in courseOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="班级">
          <el-select v-model="queryParams.classId" placeholder="选择班级" clearable>
            <el-option
              v-for="item in classOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学期">
          <el-select v-model="queryParams.semester" placeholder="选择学期" clearable>
            <el-option
              v-for="item in semesterOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="8">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>成绩分布</span>
            </div>
          </template>
          <div class="chart-container">
            <div ref="pieChartRef" style="width: 100%; height: 300px"></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>成绩趋势</span>
            </div>
          </template>
          <div class="chart-container">
            <div ref="lineChartRef" style="width: 100%; height: 300px"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="mt-20">
      <template #header>
        <div class="card-header">
          <span>成绩详情</span>
          <div class="header-operations">
            <el-button type="primary" @click="handleExport">导出分析报告</el-button>
          </div>
        </div>
      </template>
      <el-table :data="gradeList" style="width: 100%" border>
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="studentId" label="学号" width="120" />
        <el-table-column prop="className" label="班级" width="150" />
        <el-table-column prop="totalScore" label="总分" width="100" />
        <el-table-column prop="homeworkScore" label="作业成绩" width="100" />
        <el-table-column prop="examScore" label="考试成绩" width="100" />
        <el-table-column prop="rank" label="排名" width="100" />
        <el-table-column prop="gradeLevel" label="等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getGradeLevelType(row.gradeLevel)">
              {{ row.gradeLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">
              查看详情
            </el-button>
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
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'

// 查询参数
const queryParams = ref({
  courseId: undefined,
  classId: undefined,
  semester: undefined,
  pageNum: 1,
  pageSize: 10
})

// 数据列表
const gradeList = ref([])
const total = ref(0)

// 图表实例
let pieChart = null
let lineChart = null
const pieChartRef = ref(null)
const lineChartRef = ref(null)

// 下拉选项
const courseOptions = ref([
  { value: '1', label: '高等数学' },
  { value: '2', label: 'Java程序设计' },
  { value: '3', label: '数据结构' }
])

const classOptions = ref([
  { value: '1', label: '计算机科学1班' },
  { value: '2', label: '计算机科学2班' },
  { value: '3', label: '软件工程1班' }
])

const semesterOptions = ref([
  { value: '2023-1', label: '2023-2024学年第一学期' },
  { value: '2023-2', label: '2023-2024学年第二学期' }
])

// 获取等级标签类型
const getGradeLevelType = (level) => {
  const typeMap = {
    'A': 'success',
    'B': 'warning',
    'C': 'info',
    'D': 'danger'
  }
  return typeMap[level] || ''
}

// 初始化饼图
const initPieChart = () => {
  if (pieChart) {
    pieChart.dispose()
  }
  pieChart = echarts.init(pieChartRef.value)
  const option = {
    title: {
      text: '成绩分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '成绩分布',
        type: 'pie',
        radius: '50%',
        data: [
          { value: 20, name: '优秀(90-100)' },
          { value: 30, name: '良好(80-89)' },
          { value: 25, name: '中等(70-79)' },
          { value: 15, name: '及格(60-69)' },
          { value: 10, name: '不及格(<60)' }
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
  pieChart.setOption(option)
}

// 初始化折线图
const initLineChart = () => {
  if (lineChart) {
    lineChart.dispose()
  }
  lineChart = echarts.init(lineChartRef.value)
  const option = {
    title: {
      text: '成绩趋势',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['平均分', '最高分', '最低分'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['第1次', '第2次', '第3次', '第4次', '期末']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '平均分',
        type: 'line',
        data: [85, 82, 88, 86, 85]
      },
      {
        name: '最高分',
        type: 'line',
        data: [95, 92, 98, 96, 95]
      },
      {
        name: '最低分',
        type: 'line',
        data: [65, 62, 68, 66, 65]
      }
    ]
  }
  lineChart.setOption(option)
}

// 模拟获取数据
const fetchData = () => {
  // 这里应该调用后端API获取实际数据
  gradeList.value = [
    {
      studentName: '张三',
      studentId: '2021001',
      className: '计算机科学1班',
      totalScore: 88,
      homeworkScore: 90,
      examScore: 86,
      rank: 1,
      gradeLevel: 'A'
    },
    {
      studentName: '李四',
      studentId: '2021002',
      className: '计算机科学1班',
      totalScore: 85,
      homeworkScore: 88,
      examScore: 82,
      rank: 2,
      gradeLevel: 'B'
    },
    {
      studentName: '王五',
      studentId: '2021003',
      className: '计算机科学1班',
      totalScore: 75,
      homeworkScore: 78,
      examScore: 72,
      rank: 3,
      gradeLevel: 'C'
    }
  ]
  total.value = 50
}

// 查询
const handleQuery = () => {
  queryParams.value.pageNum = 1
  fetchData()
}

// 重置
const resetQuery = () => {
  queryParams.value = {
    courseId: undefined,
    classId: undefined,
    semester: undefined,
    pageNum: 1,
    pageSize: 10
  }
  fetchData()
}

// 导出
const handleExport = () => {
  // 实现导出逻辑
  console.log('导出分析报告')
}

// 查看详情
const handleDetail = (row) => {
  console.log('查看详情', row)
}

// 分页
const handleSizeChange = (val) => {
  queryParams.value.pageSize = val
  fetchData()
}

const handleCurrentChange = (val) => {
  queryParams.value.pageNum = val
  fetchData()
}

// 监听窗口大小变化
const handleResize = () => {
  if (pieChart) {
    pieChart.resize()
  }
  if (lineChart) {
    lineChart.resize()
  }
}

onMounted(() => {
  fetchData()
  nextTick(() => {
    initPieChart()
    initLineChart()
  })
  window.addEventListener('resize', handleResize)
})

// 组件卸载时移除事件监听
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (pieChart) {
    pieChart.dispose()
  }
  if (lineChart) {
    lineChart.dispose()
  }
})
</script>

<style scoped>
.grade-analysis {
  padding: 20px;
}

.mt-20 {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  margin: 20px 0;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

:deep(.el-card__header) {
  padding: 15px 20px;
}
</style>
