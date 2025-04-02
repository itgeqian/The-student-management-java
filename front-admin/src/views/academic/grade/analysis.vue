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
              <span>成绩详情</span>
              <div class="statistics">
                <span>班级平均分：{{ statistics.average || '-' }}</span>
                <span>最高分：{{ statistics.highest || '-' }}</span>
                <span>最低分：{{ statistics.lowest || '-' }}</span>
              </div>
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
            <el-table-column prop="homeworkGrade" label="作业成绩" width="120">
              <template #default="{ row }">
                {{ row.homeworkGrade || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="practiceGrade" label="实验成绩" width="120">
              <template #default="{ row }">
                {{ row.practiceGrade || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="finalGrade" label="期末成绩" width="120">
              <template #default="{ row }">
                {{ row.finalGrade || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="totalGrade" label="总评成绩" width="120">
              <template #default="{ row }">
                {{ row.totalGrade || '-' }}
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
              <span>成绩分布</span>
            </div>
          </template>
          <div class="chart-container" ref="distributionChartRef"></div>
        </el-card>

        <el-card class="box-card mt-20">
          <template #header>
            <div class="card-header">
              <span>成绩分段统计</span>
            </div>
          </template>
          <el-table :data="gradeRanges" border style="width: 100%">
            <el-table-column prop="range" label="分数段" />
            <el-table-column prop="count" label="人数" />
            <el-table-column prop="percentage" label="占比">
              <template #default="{ row }">
                {{ row.percentage }}%
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getClassList } from '@/api/grade'
import { getGradeList, getGradeStatistics } from '@/api/grade'

const loading = ref(false)
const distributionChartRef = ref(null)
let distributionChart = null

const classList = ref([])
const gradeList = ref([])
const total = ref(0)

const statistics = reactive({
  average: null,
  highest: null,
  lowest: null
})

const gradeRanges = ref([
  { range: '90-100', count: 0, percentage: 0 },
  { range: '80-89', count: 0, percentage: 0 },
  { range: '70-79', count: 0, percentage: 0 },
  { range: '60-69', count: 0, percentage: 0 },
  { range: '0-59', count: 0, percentage: 0 }
])

const queryParams = reactive({
  classId: undefined,
  pageNum: 1,
  pageSize: 10
})

// 获取课程班级列表
const loadClassList = async () => {
  try {
    const response = await getClassList()
    classList.value = response.data
  } catch (error) {
    console.error('获取课程班级列表失败：', error)
    ElMessage.error('获取课程班级列表失败')
  }
}

// 获取成绩列表
const getGrades = async () => {
  if (!queryParams.classId) return
  
  loading.value = true
  try {
    const response = await getGradeList(queryParams)
    gradeList.value = response.data.list
    total.value = response.data.total
    
    // 获取成绩统计数据
    const statsResponse = await getGradeStatistics(queryParams.classId)
    const { data } = statsResponse
    
    statistics.average = data.average?.toFixed(1)
    statistics.highest = data.highest
    statistics.lowest = data.lowest
    
    // 更新成绩分布数据
    gradeRanges.value = [
      { range: '90-100', count: data.ranges['90-100'], percentage: ((data.ranges['90-100'] / total.value) * 100).toFixed(1) },
      { range: '80-89', count: data.ranges['80-89'], percentage: ((data.ranges['80-89'] / total.value) * 100).toFixed(1) },
      { range: '70-79', count: data.ranges['70-79'], percentage: ((data.ranges['70-79'] / total.value) * 100).toFixed(1) },
      { range: '60-69', count: data.ranges['60-69'], percentage: ((data.ranges['60-69'] / total.value) * 100).toFixed(1) },
      { range: '0-59', count: data.ranges['0-59'], percentage: ((data.ranges['0-59'] / total.value) * 100).toFixed(1) }
    ]
    
    updateDistributionChart()
  } catch (error) {
    console.error('获取成绩数据失败：', error)
    ElMessage.error('获取成绩数据失败')
  } finally {
    loading.value = false
  }
}

// 更新分布图表
const updateDistributionChart = () => {
  if (!distributionChartRef.value) return
  
  if (!distributionChart) {
    distributionChart = echarts.init(distributionChartRef.value)
  }
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}人 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: gradeRanges.value.map(item => item.range)
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
        data: gradeRanges.value.map(item => ({
          name: item.range,
          value: item.count
        }))
      }
    ]
  }
  
  distributionChart.setOption(option)
}

// 监听窗口大小变化
window.addEventListener('resize', () => {
  distributionChart?.resize()
})

const handleClassChange = () => {
  queryParams.pageNum = 1
  getGrades()
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getGrades()
}

const resetQuery = () => {
  queryParams.classId = undefined
  queryParams.pageNum = 1
  gradeList.value = []
  total.value = 0
  statistics.average = null
  statistics.highest = null
  statistics.lowest = null
  gradeRanges.value.forEach(item => {
    item.count = 0
    item.percentage = 0
  })
  updateDistributionChart()
}

const handleSizeChange = (val) => {
  queryParams.pageSize = val
  getGrades()
}

const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  getGrades()
}

onMounted(() => {
  loadClassList()
})
</script>

<style scoped>
.statistics {
  display: flex;
  gap: 20px;
}

.statistics span {
  color: #666;
}

.mt-20 {
  margin-top: 20px;
}

.chart-container {
  height: 300px;
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
</style>
