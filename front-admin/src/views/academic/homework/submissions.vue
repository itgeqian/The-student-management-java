<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input
        v-model="queryParams.keyword"
        placeholder="学号/姓名"
        style="width: 200px"
        class="filter-item"
        @keyup.enter="handleQuery"
      />
      <el-select v-model="queryParams.status" placeholder="提交状态" clearable class="filter-item" style="width: 150px">
        <el-option label="未提交" :value="0" />
        <el-option label="已提交" :value="1" />
        <el-option label="已批改" :value="2" />
        <el-option label="已退回" :value="3" />
      </el-select>
      <el-button type="primary" class="filter-item" @click="handleQuery">查询</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="submissionList"
      style="width: 100%"
      border
      @row-dblclick="handleGrade"
    >
      <el-table-column prop="studentId" label="学号" width="120" />
      <el-table-column prop="studentName" label="姓名" width="120" />
      <el-table-column prop="submitTime" label="提交时间" width="180">
        <template #default="{ row }">
          {{ row.submitTime ? formatDateTime(row.submitTime) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="answer" label="作业答案" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.answer || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="attachments" label="附件" width="120">
        <template #default="{ row }">
          <template v-if="row.attachments?.length">
            <el-button v-for="file in row.attachments" :key="file.url" type="primary" link @click="handleDownload(file)">
              {{ file.name }}
            </el-button>
          </template>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="score" label="成绩" width="100">
        <template #default="{ row }">
          {{ row.score || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="comment" label="评语" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.comment || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="120">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" type="primary" link @click="handleGrade(row)">批改</el-button>
          <el-button v-if="row.status === 2" type="warning" link @click="handleGrade(row)">重改</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="queryParams.page"
      v-model:page-size="queryParams.limit"
      :total="total"
      :page-sizes="[10, 20, 30, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />

    <!-- 批改作业对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="批改作业"
      width="800px"
    >
      <template v-if="currentSubmission">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="学号">{{ currentSubmission.studentId }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ currentSubmission.studentName }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ formatDateTime(currentSubmission.submitTime) }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ getStatusText(currentSubmission.status) }}</el-descriptions-item>
        </el-descriptions>

        <div class="submission-content">
          <h3>作业答案：</h3>
          <div class="answer-text">{{ currentSubmission.answer }}</div>
          
          <template v-if="currentSubmission.attachments?.length">
            <h3>附件：</h3>
            <div class="attachments-list">
              <el-button 
                v-for="file in currentSubmission.attachments" 
                :key="file.url"
                type="primary"
                link
                @click="handleDownload(file)"
              >
                {{ file.name }}
              </el-button>
            </div>
          </template>
        </div>

        <el-form
          ref="gradeFormRef"
          :model="gradeForm"
          :rules="gradeRules"
          label-width="100px"
          class="grade-form"
        >
          <el-form-item label="成绩" prop="score">
            <el-input-number v-model="gradeForm.score" :min="0" :max="100" />
          </el-form-item>
          <el-form-item label="评语" prop="comment">
            <el-input
              v-model="gradeForm.comment"
              type="textarea"
              :rows="3"
            />
            <div class="quick-comments">
              <el-button
                v-for="(comment, index) in quickComments"
                :key="index"
                type="primary"
                link
                @click="insertQuickComment(comment)"
              >
                {{ comment }}
              </el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-radio-group v-model="gradeForm.status">
              <el-radio :label="2">通过</el-radio>
              <el-radio :label="3">退回</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </template>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitGrade">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getHomeworkSubmissions,
  gradeHomework,
  downloadHomeworkFile,
  getQuickComments
} from '@/api/homework'

const route = useRoute()
const router = useRouter()
const courseId = route.params.courseId
const classId = route.params.classId
const homeworkId = route.params.homeworkId

const loading = ref(false)
const dialogVisible = ref(false)
const submissionList = ref([])
const total = ref(0)
const currentSubmission = ref(null)
const gradeFormRef = ref(null)
const quickComments = ref([])

const queryParams = reactive({
  page: 1,
  limit: 10,
  keyword: '',
  status: undefined
})

const gradeForm = reactive({
  score: 0,
  comment: '',
  status: 2
})

const gradeRules = {
  score: [
    { required: true, message: '请输入成绩', trigger: 'blur' },
    { type: 'number', min: 0, max: 100, message: '成绩必须在0-100之间', trigger: 'blur' }
  ],
  comment: [
    { required: true, message: '请输入评语', trigger: 'blur' }
  ]
}

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '未提交',
    1: '已提交',
    2: '已批改',
    3: '已退回'
  }
  return statusMap[status] || '未知状态'
}

// 获取作业提交列表
const getList = async () => {
  loading.value = true
  try {
    const { data } = await getHomeworkSubmissions(homeworkId, queryParams)
    submissionList.value = data.list
    total.value = data.total
  } catch (error) {
    console.error('获取作业提交列表失败:', error)
    ElMessage.error('获取作业提交列表失败')
  } finally {
    loading.value = false
  }
}

// 加载快捷评语
const loadQuickComments = async () => {
  try {
    const { data } = await getQuickComments(homeworkId)
    quickComments.value = data
  } catch (error) {
    console.error('获取快捷评语失败:', error)
    ElMessage.error('获取快捷评语失败')
  }
}

// 查询
const handleQuery = () => {
  queryParams.page = 1
  getList()
}

// 批改作业
const handleGrade = (row) => {
  currentSubmission.value = row
  gradeForm.score = row.score || 0
  gradeForm.comment = row.comment || ''
  gradeForm.status = row.status === 3 ? 3 : 2
  dialogVisible.value = true
}

// 下载文件
const handleDownload = async (file) => {
  try {
    const response = await downloadHomeworkFile(file.id)
    const blob = new Blob([response.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = file.name
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (error) {
    console.error('下载文件失败:', error)
    ElMessage.error('下载文件失败')
  }
}

// 插入快捷评语
const insertQuickComment = (comment) => {
  gradeForm.comment = gradeForm.comment
    ? `${gradeForm.comment}\n${comment}`
    : comment
}

// 提交评分
const handleSubmitGrade = async () => {
  if (!gradeFormRef.value) return

  try {
    await gradeFormRef.value.validate()
    await gradeHomework(currentSubmission.value.id, {
      score: gradeForm.score,
      comment: gradeForm.comment
    })
    ElMessage.success('评分成功')
    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error('评分失败:', error)
    ElMessage.error(error.message || '评分失败')
  }
}

// 分页
const handleSizeChange = (val) => {
  queryParams.limit = val
  getList()
}

const handleCurrentChange = (val) => {
  queryParams.page = val
  getList()
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return ''
  return new Date(datetime).toLocaleString()
}

onMounted(() => {
  if (!courseId || !classId || !homeworkId) {
    ElMessage.error('参数错误')
    router.push('/academic/course')
    return
  }
  
  getList()
  loadQuickComments()
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}

.filter-container {
  padding-bottom: 10px;
  .filter-item {
    margin-right: 10px;
  }
}

.submission-content {
  margin: 20px 0;
  
  h3 {
    margin: 10px 0;
    font-size: 16px;
    font-weight: bold;
  }

  .answer-text {
    padding: 10px;
    background-color: #f5f7fa;
    border-radius: 4px;
    min-height: 100px;
    white-space: pre-wrap;
  }

  .attachments-list {
    margin-top: 10px;
  }
}

.grade-form {
  margin-top: 20px;

  .quick-comments {
    margin-top: 10px;
    
    .el-button {
      margin: 0 10px 10px 0;
    }
  }
}
</style>
