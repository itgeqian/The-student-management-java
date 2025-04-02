<template>
  <div class="app-container">
    <div class="filter-container">
      <el-select
        v-if="hasPermission(['admin', 'teacher'])"
        v-model="queryParams.classId"
        placeholder="选择课程班级"
        style="width: 200px"
        class="filter-item"
        @change="handleQuery"
      >
        <el-option
          v-for="item in classList"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        />
      </el-select>
      <el-input
        v-if="hasPermission(['admin', 'teacher'])"
        v-model="queryParams.keyword"
        placeholder="学号/姓名"
        style="width: 200px"
        class="filter-item"
        @keyup.enter="handleQuery"
      />
      <el-button
        type="primary"
        class="filter-item"
        @click="handleQuery"
      >
        查询
      </el-button>
      <el-button
        v-if="hasPermission(['admin', 'teacher'])"
        type="success"
        class="filter-item"
        @click="handleExport"
      >
        导出成绩
      </el-button>
      <el-button
        v-if="hasPermission(['admin', 'teacher'])"
        type="warning"
        class="filter-item"
        @click="handleImport"
      >
        导入成绩
      </el-button>
      <el-button
        v-if="hasPermission(['admin', 'teacher'])"
        type="primary"
        class="filter-item"
        @click="handlePublish"
      >
        发布成绩
      </el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="gradeList"
      style="width: 100%"
      border
    >
      <el-table-column prop="studentId" label="学号" width="120" />
      <el-table-column prop="studentName" label="姓名" width="120" />
      <el-table-column prop="homeworkScore" label="作业成绩" width="100">
        <template #default="{ row }">
          {{ row.homeworkScore || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="labScore" label="实验成绩" width="100">
        <template #default="{ row }">
          {{ row.labScore || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="examScore" label="期末成绩" width="100">
        <template #default="{ row }">
          <span v-if="row.examScore !== undefined">{{ row.examScore }}</span>
          <el-input
            v-else
            v-model="row.tempExamScore"
            type="number"
            :min="0"
            :max="100"
            @blur="handleExamScoreBlur(row)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="totalScore" label="总成绩" width="100">
        <template #default="{ row }">
          {{ row.totalScore || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '已发布' : '未发布' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-if="total > 0"
      :total="total"
      v-model:page="queryParams.page"
      v-model:limit="queryParams.limit"
      @pagination="getList"
    />

    <!-- 成绩分析 -->
    <el-card class="box-card mt-4" v-if="hasPermission(['admin', 'teacher'])">
      <template #header>
        <div class="card-header">
          <span>成绩分析</span>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="statistics-item">
            <div class="label">班级平均分</div>
            <div class="value">{{ statistics.avgScore || '-' }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="statistics-item">
            <div class="label">最高分</div>
            <div class="value">{{ statistics.maxScore || '-' }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="statistics-item">
            <div class="label">最低分</div>
            <div class="value">{{ statistics.minScore || '-' }}</div>
          </div>
        </el-col>
      </el-row>

      <div class="score-distribution mt-4">
        <div class="subtitle">分数段分布</div>
        <el-row :gutter="20" class="mt-2">
          <el-col :span="4" v-for="(count, range) in statistics.distribution" :key="range">
            <div class="distribution-item">
              <div class="range">{{ range }}</div>
              <div class="count">{{ count }}人</div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 导入成绩对话框 -->
    <el-dialog
      title="导入成绩"
      v-model="importDialogVisible"
      width="400px"
    >
      <el-upload
        class="upload-demo"
        :action="uploadUrl"
        :headers="uploadHeaders"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :before-upload="beforeUpload"
      >
        <el-button type="primary">选择文件</el-button>
        <template #tip>
          <div class="el-upload__tip">
            请上传Excel文件，文件大小不超过2MB
          </div>
        </template>
      </el-upload>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getClassList } from '@/api/course'
import {
  getGradeList,
  getGradeStatistics,
  importGrades,
  exportGrades,
  publishGrades,
  updateExamScore
} from '@/api/grade'
import { getToken } from '@/utils/auth'
import { hasPermission } from '@/utils/permission'
import Pagination from '@/components/Pagination/index.vue'

const loading = ref(false)
const total = ref(0)
const gradeList = ref([])
const classList = ref([])
const importDialogVisible = ref(false)
const statistics = ref({
  avgScore: 0,
  maxScore: 0,
  minScore: 0,
  distribution: {
    '0-59': 0,
    '60-69': 0,
    '70-79': 0,
    '80-89': 0,
    '90-100': 0
  }
})

const queryParams = ref({
  page: 1,
  limit: 20,
  classId: undefined,
  keyword: ''
})

const uploadHeaders = {
  Authorization: getToken()
}

const uploadUrl = '/api/grade/import'

const getList = async () => {
  loading.value = true
  try {
    const response = await getGradeList(queryParams.value)
    gradeList.value = response.data.items
    total.value = response.data.total

    if (queryParams.value.classId) {
      const statsResponse = await getGradeStatistics(queryParams.value.classId)
      statistics.value = statsResponse.data
    }
  } catch (error) {
    console.error('获取成绩列表失败:', error)
    ElMessage.error('获取成绩列表失败')
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.value.page = 1
  getList()
}

const handleExport = async () => {
  if (!queryParams.value.classId) {
    ElMessage.warning('请先选择课程班级')
    return
  }

  try {
    await exportGrades(queryParams.value.classId)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

const handleImport = () => {
  if (!queryParams.value.classId) {
    ElMessage.warning('请先选择课程班级')
    return
  }
  importDialogVisible.value = true
}

const handlePublish = async () => {
  if (!queryParams.value.classId) {
    ElMessage.warning('请先选择课程班级')
    return
  }

  try {
    await ElMessageBox.confirm('确认发布成绩？发布后学生将可以查看成绩', '提示', {
      type: 'warning'
    })
    await publishGrades(queryParams.value.classId)
    ElMessage.success('发布成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布失败:', error)
      ElMessage.error('发布失败')
    }
  }
}

const handleExamScoreBlur = async (row) => {
  if (row.tempExamScore === undefined || row.tempExamScore === '') return

  const score = parseFloat(row.tempExamScore)
  if (isNaN(score) || score < 0 || score > 100) {
    ElMessage.warning('成绩必须在0-100之间')
    return
  }

  try {
    await updateExamScore({
      studentId: row.studentId,
      classId: queryParams.value.classId,
      examScore: score
    })
    row.examScore = score
    ElMessage.success('保存成功')
    getList() // 刷新列表以获取更新后的总成绩
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  }
}

const handleUploadSuccess = (response) => {
  ElMessage.success('导入成功')
  importDialogVisible.value = false
  getList()
}

const handleUploadError = () => {
  ElMessage.error('导入失败')
}

const beforeUpload = (file) => {
  const isExcel = file.type === 'application/vnd.ms-excel' || 
                 file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isExcel) {
    ElMessage.error('只能上传Excel文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('文件大小不能超过2MB!')
    return false
  }
  return true
}

const getClasses = async () => {
  try {
    const response = await getClassList()
    classList.value = response.data
  } catch (error) {
    console.error('获取课程班级列表失败:', error)
    ElMessage.error('获取课程班级列表失败')
  }
}

onMounted(() => {
  if (hasPermission(['admin', 'teacher'])) {
    getClasses()
  }
  getList()
})
</script>

<style scoped>
.filter-container {
  margin-bottom: 20px;
}
.filter-item {
  margin-right: 10px;
}
.mt-4 {
  margin-top: 20px;
}
.mt-2 {
  margin-top: 10px;
}
.statistics-item {
  text-align: center;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}
.statistics-item .label {
  color: #606266;
  margin-bottom: 10px;
}
.statistics-item .value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}
.score-distribution {
  margin-top: 20px;
}
.score-distribution .subtitle {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
}
.distribution-item {
  text-align: center;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}
.distribution-item .range {
  color: #606266;
  margin-bottom: 5px;
}
.distribution-item .count {
  font-size: 18px;
  font-weight: bold;
  color: #67c23a;
}
</style>
