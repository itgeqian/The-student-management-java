<template>
  <div class="app-container">
    <div class="filter-container">
      <el-button @click="goBack">返回</el-button>
    </div>

    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>{{ homework.title }}</span>
        </div>
      </template>
      <div class="homework-content">
        <div class="content-item">
          <label>作业内容：</label>
          <div v-html="homework.content"></div>
        </div>
        <div class="content-item">
          <label>截止时间：</label>
          <span>{{ homework.deadline }}</span>
        </div>
        <div class="content-item">
          <label>附件：</label>
          <div v-if="homework.attachmentUrl">
            <el-link type="primary" @click="handleDownload(homework.attachmentUrl)">{{ homework.attachmentUrl }}</el-link>
          </div>
          <span v-else>无</span>
        </div>
      </div>
    </el-card>

    <el-card class="box-card submission-form">
      <template #header>
        <div class="card-header">
          <span>提交作业</span>
        </div>
      </template>
      
      <el-form
        ref="submitFormRef"
        :model="submitForm"
        :rules="submitRules"
        label-width="100px"
      >
        <el-form-item label="作业答案" prop="answer">
          <el-input
            v-model="submitForm.answer"
            type="textarea"
            :rows="6"
            placeholder="请输入作业答案"
          />
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
            ref="uploadRef"
            :action="false"
            :http-request="handleUpload"
            :before-upload="beforeUpload"
            :on-remove="handleRemoveFile"
            :file-list="fileList"
            :limit="1"
            :on-exceed="handleExceed"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                只能上传一个文件，且大小不超过10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">提交</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getHomework, submitHomework } from '@/api/homework'
import { uploadFile, deleteFile } from '@/api/file'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 作业信息
const homework = ref({})
const submitFormRef = ref(null)
const uploadRef = ref(null)

// 提交表单
const submitForm = reactive({
  answer: '',
  attachmentUrl: ''
})

// 文件列表
const fileList = ref([])

// 表单验证规则
const submitRules = {
  answer: [
    { required: true, message: '请输入作业答案', trigger: 'blur' }
  ]
}

// 获取作业信息
const getHomeworkInfo = async () => {
  try {
    const { data } = await getHomework(route.params.id)
    homework.value = data;
  } catch (error) {
    console.error('获取作业信息失败:', error)
    ElMessage.error('获取作业信息失败')
  }
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 下载文件
const handleDownload = async (url) => {
  try {
    window.open(`/api/api/file/download?fileUrl=${encodeURIComponent(url)}`, '_blank')
  } catch (error) {
    console.error('下载文件失败:', error)
    ElMessage.error('下载文件失败')
  }
}

// 上传前检查
const beforeUpload = (file) => {
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
    return false
  }
  return true
}

// 超出文件数量限制
const handleExceed = () => {
  ElMessage.warning('只能上传一个文件!')
}

// 上传文件
const handleUpload = async ({ file }) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('module', 'homework')
  
  try {
    const { data } = await uploadFile(formData)
    submitForm.attachmentUrl = data
    fileList.value = [{
      name: file.name,
      url: data
    }]
  } catch (error) {
    console.error('上传文件失败:', error)
    ElMessage.error('上传文件失败')
  }
}

// 移除文件
const handleRemoveFile = async (file) => {
  try {
    if (file.url) {
      await deleteFile({ fileUrl: file.url })
    }
    submitForm.attachmentUrl = ''
    fileList.value = []
  } catch (error) {
    console.error('删除文件失败:', error)
    ElMessage.error('删除文件失败')
  }
}

// 提交作业
const handleSubmit = async () => {
  if (!submitFormRef.value) return

  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  if (!userStore.userInfo) {
    try {
      await userStore.getUserInfo()
    } catch (error) {
      console.error('获取用户信息失败:', error)
      ElMessage.error('获取用户信息失败')
      return
    }
  }

  if (!userStore.userInfo?.id) {
    ElMessage.warning('用户信息不完整，请重新登录')
    router.push('/login')
    return
  }

  try {
    await submitFormRef.value.validate()
    await submitHomework({
      homeworkId: route.params.id,
      studentId: userStore?.userInfo?.id || 1,
      answer: submitForm.answer,
      attachmentUrl: submitForm.attachmentUrl
    })
    ElMessage.success('提交成功')
    router.go(-1)
  } catch (error) {
    console.error('提交作业失败:', error)
    ElMessage.error(error.message || '提交失败')
  }
}

onMounted(async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    await userStore.getUserInfo()
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
    return
  }

  if (!userStore.userInfo?.id) {
    ElMessage.warning('用户信息不完整，请重新登录')
    router.push('/login')
    return
  }

  getHomeworkInfo()
})
</script>

<style scoped>
.box-card {
  margin-bottom: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.homework-content {
  padding: 20px;
}
.content-item {
  margin-bottom: 15px;
}
.content-item label {
  font-weight: bold;
  margin-right: 10px;
}
.file-item {
  margin: 5px 0;
}
.submission-form {
  margin-top: 20px;
}
</style>
