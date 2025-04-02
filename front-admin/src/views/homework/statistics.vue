<template>
  <div class="app-container">
    <div class="filter-container">
      <el-select v-model="queryParams.courseId" placeholder="选择课程" clearable @change="handleCourseChange">
        <el-option
          v-for="item in courseList"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        />
      </el-select>
      <el-select
        v-model="queryParams.classId"
        placeholder="选择班级"
        clearable
        :disabled="!queryParams.courseId"
      >
        <el-option
          v-for="item in classList"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        />
      </el-select>
      <el-button type="primary" @click="handleQuery">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>作业统计</span>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="homeworkList"
        style="width: 100%"
      >
        <el-table-column prop="title" label="作业标题" min-width="200" />
        <el-table-column prop="deadline" label="截止时间" width="180" />
        <el-table-column prop="totalStudents" label="总人数" width="100" align="center" />
        <el-table-column prop="submittedCount" label="已提交" width="100" align="center" />
        <el-table-column prop="gradedCount" label="已批改" width="100" align="center" />
        <el-table-column prop="avgScore" label="平均分" width="100" align="center">
          <template #default="{ row }">
            {{ row.avgScore ? row.avgScore.toFixed(1) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      :title="detailTitle"
      v-model="dialogVisible"
      width="800px"
    >
      <el-tabs v-model="activeTab">
        <el-tab-pane label="班级统计" name="class">
          <div class="statistics-container">
            <div class="chart-container">
              <div ref="submissionChartRef" style="width: 100%; height: 300px"></div>
            </div>
            <div class="chart-container">
              <div ref="scoreChartRef" style="width: 100%; height: 300px"></div>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="学生成绩" name="student">
          <el-table :data="studentList" style="width: 100%">
            <el-table-column prop="studentName" label="姓名" width="120" />
            <el-table-column prop="studentNumber" label="学号" width="120" />
            <el-table-column prop="submitTime" label="提交时间" width="180" />
            <el-table-column prop="score" label="分数" width="100">
              <template #default="{ row }">
                {{ row.score || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="comment" label="评语" min-width="200" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getCourseList } from '@/api/course'
import { getHomeworkList, getHomeworkStats } from '@/api/homework'
import Pagination from '@/components/Pagination/index.vue'

// 数据
const loading = ref(false)
const courseList = ref([])
const classList = ref([])
const homeworkList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const detailTitle = ref('')
const activeTab = ref('class')
const studentList = ref([])

// 图表实例
let submissionChart = null
let scoreChart = null

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  courseId: undefined,
  classId: undefined
})

// 获取课程列表
const getCourses = async () => {
  try {
    const { data } = await getCourseList()
    courseList.value = data
  } catch (error) {
    console.error('获取课程列表失败:', error)
    ElMessage.error('获取课程列表失败')
  }
}

// 课程选择改变
const handleCourseChange = () => {
  queryParams.classId = undefined
  if (queryParams.courseId) {
    getClasses(queryParams.courseId)
  } else {
    classList.value = []
  }
}

// 获取作业列表
const getList = async () => {
  loading.value = true
  try {
    const { data } = await getHomeworkList(queryParams)
    homeworkList.value = data.records
    total.value = data.total
  } catch (error) {
    console.error('获取作业列表失败:', error)
    ElMessage.error('获取作业列表失败')
  } finally {
    loading.value = false
  }
}

// 查询按钮点击
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置查询
const resetQuery = () => {
  queryParams.courseId = undefined
  queryParams.classId = undefined
  queryParams.pageNum = 1
  classList.value = []
  getList()
}

// 查看详情
const handleDetail = async (row) => {
  try {
    detailTitle.value = row.title
    const { data } = await getHomeworkStats(row.id)
    studentList.value = data.studentStats
    dialogVisible.value = true
    
    // 等待 DOM 更新后初始化图表
    await nextTick()
    initCharts(data.classStats)
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  }
}

// 初始化图表
const initCharts = (stats) => {
  // 提交情况图表
  if (submissionChart) {
    submissionChart.dispose()
  }
  submissionChart = echarts.init(document.querySelector('#submissionChartRef'))
  submissionChart.setOption({
    title: {
      text: '作业提交情况'
    },
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
          { value: stats.submittedCount, name: '已提交' },
          { value: stats.totalStudents - stats.submittedCount, name: '未提交' }
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
  })

  // 分数分布图表
  if (scoreChart) {
    scoreChart.dispose()
  }
  scoreChart = echarts.init(document.querySelector('#scoreChartRef'))
  scoreChart.setOption({
    title: {
      text: '分数分布'
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['0-60', '60-70', '70-80', '80-90', '90-100']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        data: stats.scoreDistribution,
        type: 'bar'
      }
    ]
  })
}

// 监听对话框关闭
watch(() => dialogVisible.value, (val) => {
  if (!val) {
    activeTab.value = 'class'
    if (submissionChart) {
      submissionChart.dispose()
      submissionChart = null
    }
    if (scoreChart) {
      scoreChart.dispose()
      scoreChart = null
    }
  }
})

// 监听窗口大小变化
window.addEventListener('resize', () => {
  if (submissionChart) {
    submissionChart.resize()
  }
  if (scoreChart) {
    scoreChart.resize()
  }
})

onMounted(() => {
  getCourses()
  getList()
})
</script>

<style scoped>
.filter-container {
  margin-bottom: 20px;
}
.filter-container > * {
  margin-right: 10px;
}
.statistics-container {
  padding: 20px;
}
.chart-container {
  margin-bottom: 20px;
}
</style>
