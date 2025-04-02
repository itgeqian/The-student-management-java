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
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 成绩构成设置 -->
    <el-card class="box-card" v-if="selectedCourse">
      <template #header>
        <div class="card-header">
          <span>成绩构成设置</span>
          <div>
            <el-button type="primary" @click="handleSaveStructure" :loading="saving">
              保存设置
            </el-button>
          </div>
        </div>
      </template>
      <el-form :model="gradeStructure" label-width="120px">
        <el-form-item label="作业成绩占比">
          <el-input-number
            v-model="gradeStructure.homeworkWeight"
            :min="0"
            :max="100"
            :step="5"
          />
          <span class="weight-unit">%</span>
        </el-form-item>
        <el-form-item label="实验成绩占比">
          <el-input-number
            v-model="gradeStructure.labWeight"
            :min="0"
            :max="100"
            :step="5"
          />
          <span class="weight-unit">%</span>
        </el-form-item>
        <el-form-item label="期末考试占比">
          <el-input-number
            v-model="gradeStructure.examWeight"
            :min="0"
            :max="100"
            :step="5"
          />
          <span class="weight-unit">%</span>
        </el-form-item>
        <el-form-item>
          <div class="weight-total">
            总计：{{ totalWeight }}%
            <span
              v-if="totalWeight !== 100"
              class="weight-warning"
            >（各项占比之和必须为100%）</span>
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 成绩列表 -->
    <el-card class="box-card" style="margin-top: 20px;" v-if="selectedCourse">
      <template #header>
        <div class="card-header">
          <span>学生成绩列表</span>
          <div>
            <el-button
              type="success"
              @click="handleSubmitToAdmin"
              :disabled="!canSubmit"
            >
              提交给教务
            </el-button>
            <el-button type="primary" @click="handleExport">导出Excel</el-button>
          </div>
        </div>
      </template>

      <!-- 成绩统计 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="6">
          <div class="stats-card">
            <div class="stats-label">平均分</div>
            <div class="stats-value">{{ gradeStats.avgScore || '-' }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stats-card">
            <div class="stats-label">最高分</div>
            <div class="stats-value">{{ gradeStats.maxScore || '-' }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stats-card">
            <div class="stats-label">最低分</div>
            <div class="stats-value">{{ gradeStats.minScore || '-' }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stats-card">
            <div class="stats-label">及格率</div>
            <div class="stats-value">{{ gradeStats.passRate || '-' }}%</div>
          </div>
        </el-col>
      </el-row>

      <!-- 成绩分布图 -->
      <div ref="gradeChartRef" class="chart-container"></div>

      <!-- 成绩表格 -->
      <el-table
        v-loading="loading"
        :data="gradeList"
        style="width: 100%"
        border
      >
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="studentName" label="姓名" width="120" />
        <el-table-column prop="className" label="班级" width="150" />
        <el-table-column label="作业成绩" width="120" align="center">
          <template #default="{ row }">
            {{ row.homeworkScore || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="实验成绩" width="120" align="center">
          <template #default="{ row }">
            {{ row.labScore || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="期末考试" width="120" align="center">
          <template #default="{ row }">
            <el-input-number
              v-model="row.examScore"
              :min="0"
              :max="100"
              :precision="1"
              controls-position="right"
              size="small"
              @change="handleExamScoreChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="总评成绩" width="120" align="center">
          <template #default="{ row }">
            <span :class="getScoreClass(row.totalScore)">
              {{ row.totalScore || '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
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
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import {
  getTeacherCourseClasses,
  getGradeStructure,
  setGradeStructure,
  getCourseGradeSummary,
  getGradeDistribution,
  submitGradesToAdmin,
  updateGrade
} from '@/api/grade'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 查询参数
const queryParams = ref({
  current: 1,
  size: 10,
  courseClassId: undefined
})

// 数据列表
const loading = ref(false)
const saving = ref(false)
const total = ref(0)
const courseClassList = ref([])
const gradeList = ref([])

// 选中的课程
const selectedCourse = computed(() => {
  return courseClassList.value.find(item => item.id === queryParams.value.courseClassId)
})

// 成绩构成
const gradeStructure = ref({
  homeworkWeight: 30,
  labWeight: 30,
  examWeight: 40
})

// 总权重
const totalWeight = computed(() => {
  return (
    gradeStructure.value.homeworkWeight +
    gradeStructure.value.labWeight +
    gradeStructure.value.examWeight
  )
})

// 是否可以提交给教务
const canSubmit = computed(() => {
  return gradeList.value.every(item => item.totalScore !== null)
})

// 成绩统计
const gradeStats = ref({
  avgScore: 0,
  maxScore: 0,
  minScore: 0,
  passRate: 0
})

// 图表相关
const gradeChartRef = ref(null)
let gradeChart = null

// 初始化图表
const initChart = () => {
  if (gradeChart) {
    gradeChart.dispose()
  }
  gradeChart = echarts.init(gradeChartRef.value)
}

// 更新图表数据
const updateChart = async () => {
  if (!queryParams.value.courseClassId) return

  try {
    const { data } = await getGradeDistribution(queryParams.value.courseClassId)
    const option = {
      title: {
        text: '成绩分布',
        left: 'center'
      },
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
      xAxis: [
        {
          type: 'category',
          data: ['不及格', '及格', '中等', '良好', '优秀'],
          axisTick: {
            alignWithLabel: true
          }
        }
      ],
      yAxis: [
        {
          type: 'value',
          name: '人数'
        }
      ],
      series: [
        {
          name: '人数',
          type: 'bar',
          barWidth: '60%',
          data: [
            data.fail,
            data.pass,
            data.fair,
            data.good,
            data.excellent
          ]
        }
      ]
    }
    gradeChart.setOption(option)
  } catch (error) {
    console.error('获取成绩分布数据失败:', error)
  }
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

// 获取成绩构成
const getStructure = async () => {
  if (!queryParams.value.courseClassId) return

  try {
    const { data } = await getGradeStructure(queryParams.value.courseClassId)
    gradeStructure.value = data
  } catch (error) {
    console.error('获取成绩构成失败:', error)
    ElMessage.error('获取成绩构成失败')
  }
}

// 保存成绩构成
const handleSaveStructure = async () => {
  if (totalWeight.value !== 100) {
    ElMessage.warning('各项占比之和必须为100%')
    return
  }

  saving.value = true
  try {
    await setGradeStructure({
      courseClassId: queryParams.value.courseClassId,
      ...gradeStructure.value
    })
    ElMessage.success('保存成功')
    getGradeList()
  } catch (error) {
    console.error('保存成绩构成失败:', error)
    ElMessage.error('保存成绩构成失败')
  } finally {
    saving.value = false
  }
}

// 获取成绩列表
const getGradeList = async () => {
  if (!queryParams.value.courseClassId) return

  loading.value = true
  try {
    const { data } = await getCourseGradeSummary(queryParams.value.courseClassId)
    gradeList.value = data.grades
    gradeStats.value = data.stats
    total.value = data.total

    nextTick(() => {
      updateChart()
    })
  } catch (error) {
    console.error('获取成绩列表失败:', error)
    ElMessage.error('获取成绩列表失败')
  } finally {
    loading.value = false
  }
}

// 处理期末成绩变化
const handleExamScoreChange = async (row) => {
  try {
    await updateGrade({
      id: row.id,
      examScore: row.examScore
    })
    getGradeList()
  } catch (error) {
    console.error('更新期末成绩失败:', error)
    ElMessage.error('更新期末成绩失败')
  }
}

// 提交给教务
const handleSubmitToAdmin = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要提交成绩给教务吗？提交后将无法修改',
      '提示',
      {
        type: 'warning'
      }
    )

    await submitGradesToAdmin(queryParams.value.courseClassId)
    ElMessage.success('提交成功')
    getGradeList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交成绩失败:', error)
      ElMessage.error('提交成绩失败')
    }
  }
}

// 导出Excel
const handleExport = () => {
  // TODO: 实现导出功能
  ElMessage.success('导出成功')
}

// 查询操作
const handleQuery = () => {
  // queryParams.value.current = 1
  getGradeList()
}

// 重置操作
const resetQuery = () => {
  queryParams.value = {
    current: 1,
    size: 10,
    courseClassId: undefined
  }
}

// 分页操作
const handleSizeChange = (val) => {
  queryParams.value.size = val
  getGradeList()
}

const handleCurrentChange = (val) => {
  queryParams.value.current = val
  getGradeList()
}

// 获取成绩状态
const getStatusType = (status) => {
  const statusMap = {
    DRAFT: '',
    SUBMITTED: 'warning',
    PUBLISHED: 'success'
  }
  return statusMap[status]
}

const getStatusText = (status) => {
  const statusMap = {
    DRAFT: '草稿',
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

// 监听课程班级变化
watch(
  () => queryParams.value.courseClassId,
  () => {
    if (queryParams.value.courseClassId) {
      getStructure()
      getGradeList()
    }
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

.weight-unit {
  margin-left: 8px;
  color: #909399;
}

.weight-total {
  font-size: 14px;
  color: #606266;
}

.weight-warning {
  color: #f56c6c;
  margin-left: 8px;
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
</style>
