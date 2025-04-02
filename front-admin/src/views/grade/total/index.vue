<template>
  <div class="grade-total-container">
    <!-- 搜索过滤区 -->
    <el-card class="filter-container">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="学生姓名">
          <el-input
            v-model="queryParams.studentName"
            placeholder="请输入学生姓名"
            clearable
          />
        </el-form-item>
        <el-form-item label="学号">
          <el-input
            v-model="queryParams.studentId"
            placeholder="请输入学号"
            clearable
          />
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

    <!-- 成绩表格区 -->
    <el-card class="mt-20">
      <template #header>
        <div class="card-header">
          <span>总成绩列表</span>
          <div class="header-operations">
            <el-button type="success" @click="handleExport">导出成绩</el-button>
          </div>
        </div>
      </template>

      <el-table :data="gradeList" style="width: 100%" border>
        <el-table-column type="expand">
          <template #default="props">
            <el-table :data="props.row.courseGrades" border>
              <el-table-column prop="courseName" label="课程名称" min-width="150" />
              <el-table-column prop="credit" label="学分" width="80" />
              <el-table-column prop="grade" label="成绩" width="80">
                <template #default="{ row }">
                  <span :class="getGradeClass(row.grade)">{{ row.grade }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="gradePoint" label="绩点" width="80" />
              <el-table-column prop="teacherName" label="任课教师" width="120" />
              <el-table-column prop="examTime" label="考试时间" width="180" />
              <el-table-column prop="remark" label="备注" min-width="150" />
            </el-table>
          </template>
        </el-table-column>
        <el-table-column prop="studentName" label="学生姓名" min-width="120" />
        <el-table-column prop="studentId" label="学号" width="120" />
        <el-table-column prop="className" label="班级" width="150" />
        <el-table-column prop="totalCredit" label="总学分" width="100" />
        <el-table-column prop="averageGrade" label="平均分" width="100">
          <template #default="{ row }">
            <span :class="getGradeClass(row.averageGrade)">
              {{ row.averageGrade }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="gpa" label="GPA" width="100" />
        <el-table-column prop="rank" label="排名" width="100" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">
              详情
            </el-button>
            <el-button type="primary" link @click="handlePrint(row)">
              打印
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

    <!-- 成绩详情对话框 -->
    <el-dialog
      title="成绩详情"
      v-model="detailVisible"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学生姓名">{{ currentStudent.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentStudent.studentId }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ currentStudent.className }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ currentStudent.majorName }}</el-descriptions-item>
        <el-descriptions-item label="总学分">{{ currentStudent.totalCredit }}</el-descriptions-item>
        <el-descriptions-item label="GPA">{{ currentStudent.gpa }}</el-descriptions-item>
        <el-descriptions-item label="平均分">{{ currentStudent.averageGrade }}</el-descriptions-item>
        <el-descriptions-item label="排名">{{ currentStudent.rank }}</el-descriptions-item>
      </el-descriptions>

      <div class="grade-charts mt-20">
        <el-row :gutter="20">
          <el-col :span="12">
            <div ref="gradeDistributionChart" style="height: 300px"></div>
          </el-col>
          <el-col :span="12">
            <div ref="gradeTrendChart" style="height: 300px"></div>
          </el-col>
        </el-row>
      </div>

      <el-table :data="currentStudent.courseGrades" border class="mt-20">
        <el-table-column prop="courseName" label="课程名称" min-width="150" />
        <el-table-column prop="credit" label="学分" width="80" />
        <el-table-column prop="grade" label="成绩" width="80">
          <template #default="{ row }">
            <span :class="getGradeClass(row.grade)">{{ row.grade }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="gradePoint" label="绩点" width="80" />
        <el-table-column prop="teacherName" label="任课教师" width="120" />
        <el-table-column prop="examTime" label="考试时间" width="180" />
        <el-table-column prop="remark" label="备注" min-width="150" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

// 查询参数
const queryParams = ref({
  studentName: '',
  studentId: '',
  classId: undefined,
  semester: undefined,
  pageNum: 1,
  pageSize: 10
})

// 班级选项
const classOptions = ref([
  { value: 1, label: '计算机科学1班' },
  { value: 2, label: '计算机科学2班' },
  { value: 3, label: '软件工程1班' },
  { value: 4, label: '软件工程2班' }
])

// 学期选项
const semesterOptions = ref([
  { value: '2023-1', label: '2023-2024学年第一学期' },
  { value: '2023-2', label: '2023-2024学年第二学期' }
])

// 成绩列表数据
const gradeList = ref([
  {
    studentName: '张三',
    studentId: '2023001',
    className: '计算机科学1班',
    totalCredit: 25,
    averageGrade: 88.5,
    gpa: 3.7,
    rank: 5,
    courseGrades: [
      {
        courseName: '高等数学',
        credit: 5,
        grade: 90,
        gradePoint: 4.0,
        teacherName: '李老师',
        examTime: '2023-12-25 14:00',
        remark: ''
      },
      {
        courseName: 'Java程序设计',
        credit: 4,
        grade: 87,
        gradePoint: 3.7,
        teacherName: '王老师',
        examTime: '2023-12-27 09:30',
        remark: ''
      }
    ]
  }
])

const total = ref(50)
const detailVisible = ref(false)
const currentStudent = ref({})
let gradeDistributionChart = null
let gradeTrendChart = null

// 获取成绩样式
const getGradeClass = (grade) => {
  if (grade >= 90) return 'text-success'
  if (grade >= 80) return 'text-primary'
  if (grade >= 60) return 'text-warning'
  return 'text-danger'
}

// 查询
const handleQuery = () => {
  queryParams.value.pageNum = 1
  fetchData()
}

// 重置查询
const resetQuery = () => {
  queryParams.value = {
    studentName: '',
    studentId: '',
    classId: undefined,
    semester: undefined,
    pageNum: 1,
    pageSize: 10
  }
  fetchData()
}

// 获取数据
const fetchData = () => {
  // 这里应该调用后端API获取数据
  console.log('获取成绩列表数据')
}

// 导出成绩
const handleExport = () => {
  // 实现导出逻辑
  console.log('导出成绩数据')
}

// 查看详情
const handleDetail = (row) => {
  currentStudent.value = { ...row }
  detailVisible.value = true
  // 在下一个tick渲染图表
  setTimeout(() => {
    initCharts()
  }, 100)
}

// 打印成绩单
const handlePrint = (row) => {
  // 实现打印逻辑
  console.log('打印成绩单', row)
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

// 初始化图表
const initCharts = () => {
  // 成绩分布图
  if (gradeDistributionChart) {
    gradeDistributionChart.dispose()
  }
  const distributionDom = document.querySelector('.grade-charts .el-row .el-col:first-child div')
  if (distributionDom) {
    gradeDistributionChart = echarts.init(distributionDom)
    gradeDistributionChart.setOption({
      title: {
        text: '成绩分布'
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
          type: 'pie',
          radius: '50%',
          data: [
            { value: 5, name: '90分以上' },
            { value: 8, name: '80-89分' },
            { value: 4, name: '70-79分' },
            { value: 2, name: '60-69分' },
            { value: 1, name: '60分以下' }
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
  }

  // 成绩趋势图
  if (gradeTrendChart) {
    gradeTrendChart.dispose()
  }
  const trendDom = document.querySelector('.grade-charts .el-row .el-col:last-child div')
  if (trendDom) {
    gradeTrendChart = echarts.init(trendDom)
    gradeTrendChart.setOption({
      title: {
        text: '成绩趋势'
      },
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: ['第一学期', '第二学期', '第三学期', '第四学期']
      },
      yAxis: {
        type: 'value',
        min: 0,
        max: 100
      },
      series: [
        {
          data: [85, 88, 86, 90],
          type: 'line',
          smooth: true
        }
      ]
    })
  }
}

// 监听窗口大小变化，重绘图表
window.addEventListener('resize', () => {
  if (gradeDistributionChart) {
    gradeDistributionChart.resize()
  }
  if (gradeTrendChart) {
    gradeTrendChart.resize()
  }
})

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.grade-total-container {
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

.header-operations {
  display: flex;
  gap: 10px;
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

.text-danger {
  color: #f56c6c;
}

.grade-charts {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
}
</style>
