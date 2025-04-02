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

    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>成绩录入</span>
          <div>
            <el-button type="primary" @click="handleSubmitAll">提交成绩</el-button>
            <el-button type="success" @click="handleExport">导出成绩</el-button>
            <el-button type="warning" @click="handleImport">导入成绩</el-button>
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
        <el-table-column label="作业成绩" width="150">
          <template #default="{ row }">
            {{ row.homeworkGrade || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="实验成绩" width="150">
          <template #default="{ row }">
            {{ row.practiceGrade || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="期末成绩" width="180">
          <template #default="{ row }">
            <el-input-number
              v-model="row.finalGrade"
              :min="0"
              :max="100"
              :precision="1"
              :controls="false"
              @change="calculateTotalGrade(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="总评成绩" width="150">
          <template #default="{ row }">
            {{ row.totalGrade || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 'submitted' ? 'success' : 'warning'">
              {{ row.status === 'submitted' ? '已提交' : '未提交' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="handleSubmit(row)"
              :disabled="row.status === 'submitted'"
            >
              提交
            </el-button>
            <el-button
              type="danger"
              link
              @click="handleReset(row)"
              :disabled="row.status === 'submitted'"
            >
              重置
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

    <!-- 导入成绩对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="导入成绩"
      width="500px"
    >
      <el-upload
        class="upload-demo"
        :action="uploadUrl"
        :headers="headers"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :before-upload="beforeUpload"
      >
        <el-button type="primary">选择文件</el-button>
        <template #tip>
          <div class="el-upload__tip">
            请上传Excel文件（.xlsx），文件大小不超过10MB
          </div>
        </template>
      </el-upload>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="importDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmImport">
            确认导入
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getToken } from '@/utils/auth'
import {
  getClassList,
  getGradeList,
  submitGrade,
  submitAllGrades,
  exportGrades,
  importGrades
} from '@/api/grade'

const loading = ref(false)
const total = ref(0)
const gradeList = ref([])
const classList = ref([])
const importDialogVisible = ref(false)

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  classId: undefined,
  studentId: ''
})

// 上传相关
const uploadUrl = import.meta.env.VITE_APP_BASE_API + '/grade/import'
const headers = {
  Authorization: 'Bearer ' + getToken()
}

// 获取课程班级列表
const loadClassList = async () => {
  try {
    const { data } = await getClassList()
    classList.value = data.records;
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
  } catch (error) {
    console.error('Failed to get grades:', error)
    ElMessage.error('获取成绩列表失败')
  } finally {
    loading.value = false
  }
}

// 计算总评成绩
const calculateTotalGrade = (row) => {
  if (row.finalGrade === undefined) return
  
  const weights = {
    homework: 0.3,
    practice: 0.3,
    final: 0.4
  }
  
  row.totalGrade = (
    (row.homeworkGrade || 0) * weights.homework +
    (row.practiceGrade || 0) * weights.practice +
    row.finalGrade * weights.final
  ).toFixed(1)
}

// 提交单个学生成绩
const handleSubmit = async (row) => {
  try {
    await submitGrade({
      classId: queryParams.classId,
      studentId: row.studentId,
      finalGrade: row.finalGrade,
      totalGrade: row.totalGrade
    })
    row.status = 'submitted'
    ElMessage.success('成绩提交成功')
  } catch (error) {
    console.error('Failed to submit grade:', error)
    ElMessage.error('成绩提交失败')
  }
}

// 提交全部成绩
const handleSubmitAll = async () => {
  try {
    await ElMessageBox.confirm(
      '确认提交所有学生成绩吗？提交后将无法修改',
      '提示',
      {
        type: 'warning'
      }
    )
    
    await submitAllGrades({
      classId: queryParams.classId,
      grades: gradeList.value.map(item => ({
        studentId: item.studentId,
        finalGrade: item.finalGrade,
        totalGrade: item.totalGrade
      }))
    })
    
    ElMessage.success('所有成绩提交成功')
    getGrades()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to submit all grades:', error)
      ElMessage.error('成绩提交失败')
    }
  }
}

// 重置单个学生成绩
const handleReset = (row) => {
  row.finalGrade = undefined
  row.totalGrade = undefined
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

// 导入成绩
const handleImport = () => {
  importDialogVisible.value = true
}

const beforeUpload = (file) => {
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isExcel) {
    ElMessage.error('只能上传Excel文件！')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过10MB！')
    return false
  }
  return true
}

const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    ElMessage.success('文件上传成功')
    importDialogVisible.value = false
    getGrades()
  } else {
    ElMessage.error(response.message || '文件上传失败')
  }
}

const handleUploadError = () => {
  ElMessage.error('文件上传失败')
}

const confirmImport = () => {
  importDialogVisible.value = false
  getGrades()
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

onMounted(() => {
  loadClassList()
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

.upload-demo {
  text-align: center;
}

:deep(.el-upload__tip) {
  margin-top: 10px;
  color: #666;
}
</style>
