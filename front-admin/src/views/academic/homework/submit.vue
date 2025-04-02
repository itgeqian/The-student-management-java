<template>
  <div class="app-container">
    <div class="homework-detail">
      <el-descriptions title="作业详情" :column="1" border>
        <el-descriptions-item label="作业标题">{{ homework.title }}</el-descriptions-item>
        <el-descriptions-item label="作业内容">{{ homework.content }}</el-descriptions-item>
        <el-descriptions-item label="截止时间">{{ formatDateTime(homework.deadline) }}</el-descriptions-item>
        <el-descriptions-item label="作业附件" v-if="homework.attachments?.length">
          <div v-for="file in homework.attachments" :key="file.id" class="file-item">
            <el-link type="primary" @click="downloadFile(file)">
              {{ file.name }}
            </el-link>
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="submission-form" v-if="canSubmit">
      <el-form
        ref="submissionFormRef"
        :model="submissionForm"
        :rules="submissionRules"
        label-width="100px"
        style="margin-top: 20px"
      >
        <el-form-item label="作业答案" prop="content">
          <el-input
            v-model="submissionForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入作业答案"
          />
        </el-form-item>
        <el-form-item label="作业附件">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            :file-list="fileList"
          >
            <el-button type="primary">上传附件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持任意格式文件，单个文件不超过10MB</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">提交作业</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="submission-result" v-if="submission">
      <el-divider content-position="left">提交记录</el-divider>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="提交时间">{{ formatDateTime(submission.submitTime) }}</el-descriptions-item>
        <el-descriptions-item label="作业答案">{{ submission.content }}</el-descriptions-item>
        <el-descriptions-item label="提交附件" v-if="submission.attachments?.length">
          <div v-for="file in submission.attachments" :key="file.id" class="file-item">
            <el-link type="primary" @click="downloadFile(file)">
              {{ file.name }}
            </el-link>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="批改状态">
          <el-tag :type="submission.status === 'graded' ? 'success' : submission.status === 'returned' ? 'danger' : 'info'">
            {{ getStatusText(submission.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="成绩" v-if="submission.status === 'graded'">
          {{ submission.score }}
        </el-descriptions-item>
        <el-descriptions-item label="教师评语" v-if="submission.status === 'graded' || submission.status === 'returned'">
          {{ submission.comment }}
        </el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getHomework,
  submitHomework,
  getStudentSubmission,
  downloadHomeworkFile,
  uploadHomeworkFile
} from '@/api/homework'
import { getToken } from '@/utils/auth'

const route = useRoute()
const homeworkId = route.params.homeworkId

const homework = ref({})
const submission = ref(null)
const submissionFormRef = ref(null)
const fileList = ref([])

const submissionForm = reactive({
  content: '',
  attachments: []
})

const submissionRules = {
  content: [
    { required: true, message: '请输入作业答案', trigger: 'blur' }
  ]
}

// 上传相关配置
const uploadUrl = '/api/homework/files/upload'
const uploadHeaders = {
  Authorization: getToken()
}

// 是否可以提交作业
const canSubmit = computed(() => {
  if (!homework.value || !homework.value.deadline) {
    return false
  }
  const now = new Date()
  const deadline = new Date(homework.value.deadline)
  return now <= deadline && (!submission.value || submission.value.status === 'returned')
})

// 获取作业详情
const getHomeworkDetail = async () => {
  try {
    const { data } = await getHomework(homeworkId)
    homework.value = data
  } catch (error) {
    console.error('获取作业详情失败:', error)
    ElMessage.error('获取作业详情失败')
  }
}

// 获取提交记录
const getSubmissionDetail = async () => {
  try {
    const { data } = await getStudentSubmission(homeworkId)
    submission.value = data
  } catch (error) {
    if (error.response?.status !== 404) {
      console.error('获取提交记录失败:', error)
      ElMessage.error('获取提交记录失败')
    }
  }
}

// 文件上传相关方法
const beforeUpload = (file) => {
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
    return false
  }
  return true
}

const handleUploadSuccess = (response) => {
  submissionForm.attachments.push(response.data)
  ElMessage.success('文件上传成功')
}

const handleUploadError = () => {
  ElMessage.error('文件上传失败')
}

// 提交作业
const handleSubmit = async () => {
  if (!submissionFormRef.value) return
  
  try {
    await submissionFormRef.value.validate()
    await submitHomework(homeworkId, submissionForm)
    ElMessage.success('作业提交成功')
    getSubmissionDetail()
  } catch (error) {
    console.error('提交作业失败:', error)
    ElMessage.error(error.message || '提交作业失败')
  }
}

// 下载文件
const downloadFile = async (file) => {
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

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    submitted: '已提交',
    graded: '已批改',
    returned: '已退回'
  }
  return statusMap[status] || '未知状态'
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return ''
  return new Date(datetime).toLocaleString()
}

onMounted(() => {
  if (!homeworkId) {
    ElMessage.error('参数错误')
    return
  }
  
  getHomeworkDetail()
  getSubmissionDetail()
})
</script>

<style scoped>
.homework-detail {
  margin-bottom: 20px;
}

.file-item {
  margin: 5px 0;
}

.submission-form {
  margin: 20px 0;
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.el-upload__tip {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
}
</style>
