<template>
  <div class="assignment-grade-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>作业评分</span>
        </div>
      </template>

      <div class="search-section">
        <el-form :inline="true" :model="searchForm" class="demo-form-inline">
          <el-form-item label="课程">
            <el-select
              v-model="searchForm.courseId"
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
          <el-form-item label="作业">
            <el-select
              v-model="searchForm.assignmentId"
              placeholder="选择作业"
              clearable
              :disabled="!searchForm.courseId"
            >
              <el-option
                v-for="item in assignmentOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="提交状态" clearable>
              <el-option label="已提交" value="submitted" />
              <el-option label="未提交" value="pending" />
              <el-option label="已批改" value="graded" />
              <el-option label="已逾期" value="overdue" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table
        v-loading="tableLoading"
        :data="tableData"
        style="width: 100%"
        border
      >
        <el-table-column prop="studentId" label="学号" width="120" />
        <el-table-column prop="studentName" label="姓名" width="120" />
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分数" width="100">
          <template #default="scope">
            <span v-if="scope.row.status === 'graded'">{{ scope.row.score }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 'submitted'"
              type="primary"
              size="small"
              @click="handleGrade(scope.row)"
            >
              评分
            </el-button>
            <el-button
              v-if="scope.row.status === 'graded'"
              type="info"
              size="small"
              @click="handleViewDetail(scope.row)"
            >
              查看
            </el-button>
            <el-button
              type="primary"
              link
              size="small"
              @click="downloadSubmission(scope.row)"
              :disabled="!scope.row.hasSubmission"
            >
              下载作业
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 评分对话框 -->
    <el-dialog
      v-model="gradeDialogVisible"
      title="作业评分"
      width="600px"
      :before-close="handleGradeDialogClose"
    >
      <el-form
        ref="gradeFormRef"
        :model="gradeForm"
        :rules="gradeRules"
        label-width="100px"
      >
        <el-form-item label="学生信息">
          {{ currentStudent?.studentName }} ({{ currentStudent?.studentId }})
        </el-form-item>
        <el-form-item label="提交时间">
          {{ currentStudent?.submitTime }}
        </el-form-item>
        <el-form-item label="分数" prop="score">
          <el-input-number
            v-model="gradeForm.score"
            :min="0"
            :max="100"
            :step="5"
          />
        </el-form-item>
        <el-form-item label="评语" prop="comment">
          <el-input
            v-model="gradeForm.comment"
            type="textarea"
            :rows="4"
            placeholder="请输入评语"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleGradeDialogClose">取消</el-button>
          <el-button type="primary" @click="submitGrade">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

// 搜索表单
const searchForm = reactive({
  courseId: '',
  assignmentId: '',
  status: ''
})

// 课程选项
const courseOptions = ref([])
const courseLoading = ref(false)

// 作业选项
const assignmentOptions = ref([])

// 表格数据
const tableData = ref([])
const tableLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 评分对话框
const gradeDialogVisible = ref(false)
const currentStudent = ref(null)
const gradeFormRef = ref(null)
const gradeForm = reactive({
  score: 0,
  comment: ''
})

const gradeRules = {
  score: [
    { required: true, message: '请输入分数', trigger: 'blur' }
  ],
  comment: [
    { required: true, message: '请输入评语', trigger: 'blur' }
  ]
}

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

// 状态样式
const getStatusType = (status) => {
  const map = {
    submitted: 'warning',
    pending: 'info',
    graded: 'success',
    overdue: 'danger'
  }
  return map[status]
}

const getStatusText = (status) => {
  const map = {
    submitted: '已提交',
    pending: '未提交',
    graded: '已批改',
    overdue: '已逾期'
  }
  return map[status]
}

// 搜索
const handleSearch = () => {
  fetchTableData()
}

// 重置搜索
const resetSearch = () => {
  searchForm.courseId = ''
  searchForm.assignmentId = ''
  searchForm.status = ''
  fetchTableData()
}

// 获取表格数据
const fetchTableData = () => {
  tableLoading.value = true
  // 模拟API调用
  setTimeout(() => {
    tableData.value = [
      {
        studentId: '2021001',
        studentName: '张三',
        submitTime: '2023-12-01 14:30:00',
        status: 'submitted',
        score: null,
        hasSubmission: true
      },
      {
        studentId: '2021002',
        studentName: '李四',
        submitTime: '2023-12-01 15:20:00',
        status: 'graded',
        score: 85,
        hasSubmission: true
      }
    ]
    total.value = 2
    tableLoading.value = false
  }, 500)
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchTableData()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchTableData()
}

// 评分
const handleGrade = (row) => {
  currentStudent.value = row
  gradeDialogVisible.value = true
}

// 查看详情
const handleViewDetail = (row) => {
  console.log('查看详情', row)
}

// 下载作业
const downloadSubmission = (row) => {
  console.log('下载作业', row)
}

// 关闭评分对话框
const handleGradeDialogClose = () => {
  gradeDialogVisible.value = false
  gradeForm.score = 0
  gradeForm.comment = ''
}

// 提交评分
const submitGrade = async () => {
  if (!gradeFormRef.value) return
  await gradeFormRef.value.validate((valid) => {
    if (valid) {
      // 这里应该调用API保存评分
      ElMessage.success('评分成功')
      gradeDialogVisible.value = false
      fetchTableData()
    }
  })
}

// 初始化
fetchTableData()
</script>

<style scoped>
.assignment-grade-container {
  padding: 20px;
}

.search-section {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
