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
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="已提交" value="SUBMITTED" />
            <el-option label="已发布" value="PUBLISHED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 成绩列表 -->
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>成绩列表</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="gradeList" border>
        <el-table-column prop="courseClassId" label="课程班级ID" width="100" />
        <el-table-column prop="studentId" label="学生ID" width="100" />
        <el-table-column prop="examScore" label="期末考试成绩" width="120">
          <template #default="{ row }">
            <span :class="getScoreClass(row.examScore)">{{ row.examScore }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="homeworkScore" label="作业平均成绩" width="120">
          <template #default="{ row }">
            <span :class="getScoreClass(row.homeworkScore)">{{ row.homeworkScore }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="experimentScore" label="实验平均成绩" width="120">
          <template #default="{ row }">
            <span :class="getScoreClass(row.experimentScore)">{{ row.experimentScore }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="总成绩" width="100">
          <template #default="{ row }">
            <span :class="getScoreClass(row.totalScore)">{{ row.totalScore }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="isPublished" label="发布状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isPublished ? 'success' : 'info'">
              {{ row.isPublished ? '已发布' : '未发布' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isSubmitted" label="提交状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isSubmitted ? 'success' : 'warning'">
              {{ row.isSubmitted ? '已提交' : '未提交' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="!row.isPublished && row.isSubmitted" 
                      type="primary" 
                      size="small" 
                      @click="handlePublish(row)">
              发布
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 成绩详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="成绩详情"
      width="80%"
      append-to-body
    >
      <div v-loading="detailLoading">
        <!-- 成绩统计 -->
        <el-row :gutter="20" class="stats-row">
          <el-col :span="6">
            <div class="stats-card">
              <div class="stats-label">平均分</div>
              <div class="stats-value">{{ currentGrade?.avgScore || '-' }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stats-card">
              <div class="stats-label">最高分</div>
              <div class="stats-value">{{ currentGrade?.maxScore || '-' }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stats-card">
              <div class="stats-label">最低分</div>
              <div class="stats-value">{{ currentGrade?.minScore || '-' }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stats-card">
              <div class="stats-label">及格率</div>
              <div class="stats-value">{{ currentGrade?.passRate || '-' }}%</div>
            </div>
          </el-col>
        </el-row>

        <!-- 成绩分布图 -->
        <div ref="detailChartRef" class="chart-container"></div>

        <!-- 学生成绩列表 -->
        <el-table :data="studentGrades" border style="width: 100%">
          <el-table-column prop="studentNo" label="学号" width="120" />
          <el-table-column prop="studentName" label="姓名" width="120" />
          <el-table-column prop="className" label="班级" width="150" />
          <el-table-column label="作业成绩" width="100" align="center">
            <template #default="{ row }">
              {{ row.homeworkScore || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="实验成绩" width="100" align="center">
            <template #default="{ row }">
              {{ row.labScore || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="期末考试" width="100" align="center">
            <template #default="{ row }">
              {{ row.examScore || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="总评成绩" width="100" align="center">
            <template #default="{ row }">
              <span :class="getScoreClass(row.totalScore)">
                {{ row.totalScore || '-' }}
              </span>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button
            v-if="currentGrade?.status === 'SUBMITTED'"
            type="primary"
            @click="handlePublish(currentGrade)"
          >
            发布成绩
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import {
  getGradeList,
  getGradeDetail,
  approveAndPublishGrades,
  getClassTotalGrades
} from '@/api/grade'
import { getAllCourseClasses } from '@/api/course'
import { formatDateTime } from '@/utils/format'

// 查询参数
const queryParams = ref({
  courseClassId: undefined,
  status: undefined,
  pageNum: 1,
  pageSize: 10
})

// 数据列表
const loading = ref(false)
const detailLoading = ref(false)
const total = ref(0)
const courseClassList = ref([])
const gradeList = ref([])

// 详情相关
const dialogVisible = ref(false)
const currentGrade = ref(null)
const studentGrades = ref([])
const detailChartRef = ref(null)
let detailChart = null

// 获取课程班级列表
const getCourseClasses = async () => {
  try {
    const { data } = await getAllCourseClasses()
    courseClassList.value = data
  } catch (error) {
    console.error('获取课程班级列表失败:', error)
    ElMessage.error('获取课程班级列表失败')
  }
}

// 获取成绩列表
const getGrades = async () => {
  loading.value = true
  try {
    const { data } = await getClassTotalGrades({
      ...queryParams.value,
      current: queryParams.value.pageNum
    })
    gradeList.value = data.records
    total.value = data.total
  } catch (error) {
    console.error('获取成绩列表失败:', error)
    ElMessage.error('获取成绩列表失败')
  } finally {
    loading.value = false
  }
}

// 查看详情
const handleView = async (row) => {
  currentGrade.value = row
  dialogVisible.value = true
  detailLoading.value = true

  try {
    const { data } = await getGradeDetail(row.id)
    studentGrades.value = data.students

    nextTick(() => {
      initDetailChart()
      updateDetailChart(data.distribution)
    })
  } catch (error) {
    console.error('获取成绩详情失败:', error)
    ElMessage.error('获取成绩详情失败')
  } finally {
    detailLoading.value = false
  }
}

// 发布成绩
const handlePublish = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要发布该课程班级的成绩吗？发布后学生将可以查看成绩',
      '提示',
      {
        type: 'warning'
      }
    )

    await approveAndPublishGrades(row.id)
    ElMessage.success('发布成功')
    getGrades()
    dialogVisible.value = false
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布成绩失败:', error)
      ElMessage.error('发布成绩失败')
    }
  }
}

// 查询操作
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getGrades()
}

// 重置操作
const resetQuery = () => {
  queryParams.value = {
    courseClassId: undefined,
    status: undefined,
    pageNum: 1,
    pageSize: 10
  }
  getGrades()
}

// 分页操作
const handleSizeChange = (val) => {
  queryParams.value.pageSize = val
  getGrades()
}

const handleCurrentChange = (val) => {
  queryParams.value.pageNum = val
  getGrades()
}

// 获取成绩状态
const getStatusType = (status) => {
  const statusMap = {
    SUBMITTED: 'warning',
    PUBLISHED: 'success'
  }
  return statusMap[status]
}

const getStatusText = (status) => {
  const statusMap = {
    SUBMITTED: '已提交',
    PUBLISHED: '已发布'
  }
  return statusMap[status]
}

// 获取成绩样式
const getScoreClass = (score) => {
  if (!score) return ''
  if (score >= 90) return 'text-success'
  if (score >= 80) return 'text-primary'
  if (score >= 70) return 'text-warning'
  if (score >= 60) return 'text-info'
  return 'text-danger'
}

// 图表相关
const initDetailChart = () => {
  if (detailChart) {
    detailChart.dispose()
  }
  detailChart = echarts.init(detailChartRef.value)
}

const updateDetailChart = (data) => {
  const option = {
    title: {
      text: '成绩分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}人 ({d}%)'
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
  detailChart.setOption(option)
}

onMounted(async () => {
  await getCourseClasses()
  getGrades()
})

onBeforeUnmount(() => {
  if (detailChart) {
    detailChart.dispose()
    detailChart = null
  }
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

.stats-row {
  margin-bottom: 20px;
}

.stats-card {
  background-color: #f5f7fa;
  border-radius: 4px;
  padding: 20px;
  text-align: center;
}

.stats-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stats-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.chart-container {
  height: 400px;
  margin: 20px 0;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.text-success {
  color: #67c23a;
}

.text-primary {
  color: #409eff;
}

.text-warning {
  color: #e6a23c;
}

.text-info {
  color: #909399;
}

.text-danger {
  color: #f56c6c;
}

.dialog-footer {
  text-align: right;
}

.score-excellent {
  color: #67C23A;
  font-weight: bold;
}

.score-good {
  color: #409EFF;
}

.score-pass {
  color: #E6A23C;
}

.score-fail {
  color: #F56C6C;
  font-weight: bold;
}
</style>
