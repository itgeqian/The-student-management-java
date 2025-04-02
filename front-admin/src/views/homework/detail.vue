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
        <!-- 基本信息 -->
        <div class="section">
          <h3>基本信息</h3>
          <div class="content-item">
            <label>课程班级：</label>
            <span>{{ homework.courseClass?.name }}</span>
          </div>
          <div class="content-item">
            <label>作业内容：</label>
            <div v-html="homework.content"></div>
          </div>
          <div class="content-item">
            <label>截止时间：</label>
            <span>{{ formatDateTime(homework.deadline) }}</span>
          </div>
          <div class="content-item">
            <label>创建时间：</label>
            <span>{{ formatDateTime(homework.createTime) }}</span>
          </div>
          <div class="content-item">
            <label>更新时间：</label>
            <span>{{ formatDateTime(homework.updateTime) }}</span>
          </div>
          <div class="content-item">
            <label>作业附件：</label>
            <div v-if="homework.attachmentUrl">
              <el-link type="primary" @click="handleDownload(homework.attachmentUrl)">
                {{ getFileName(homework.attachmentUrl) }}
              </el-link>
            </div>
            <span v-else>无</span>
          </div>
        </div>

        <!-- 提交信息 -->
        <div class="section">
          <h3>提交信息</h3>
          <div v-if="homework.submissionContent || homework.submissionAttachment">
            <div class="content-item">
              <label>提交状态：</label>
              <el-tag :type="getStatusType(homework.submissionStatus)">
                {{ getStatusText(homework.submissionStatus) }}
              </el-tag>
            </div>
            <div class="content-item">
              <label>提交时间：</label>
              <span>{{ formatDateTime(homework.submissionTime) }}</span>
            </div>
            <div class="content-item">
              <label>最后更新：</label>
              <span>{{ formatDateTime(homework.submissionUpdateTime) }}</span>
            </div>
            <div class="content-item">
              <label>作答内容：</label>
              <div v-html="homework.submissionContent"></div>
            </div>
            <div class="content-item">
              <label>提交附件：</label>
              <div v-if="homework.submissionAttachment">
                <el-link type="primary" @click="handleDownload(homework.submissionAttachment)">
                  {{ getFileName(homework.submissionAttachment) }}
                </el-link>
              </div>
              <span v-else>无</span>
            </div>
            <div v-if="homework.submissionStatus === 'GRADED'" class="content-item">
              <label>得分：</label>
              <span class="score">{{ homework.submissionScore }}</span>
            </div>
            <div v-if="homework.submissionComment" class="content-item">
              <label>教师评语：</label>
              <div v-html="homework.submissionComment"></div>
            </div>
          </div>
          <div v-else class="content-item">
            <el-empty description="尚未提交作业" />
            <div class="submit-button">
              <el-button type="primary" @click="handleSubmit">立即提交</el-button>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getHomework } from '@/api/homework'
import { formatDateTime } from '@/utils/format'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 作业信息
const homework = ref({})

// 获取作业信息
const getHomeworkInfo = async () => {
  try {
    const { data } = await getHomework(route.params.id)
    console.log('获取到的作业信息:', data)
    homework.value = data
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
const handleDownload = (url) => {
  if (!url) {
    ElMessage.warning('附件地址为空')
    return
  }
  window.open(`/api/api/file/download?fileUrl=${encodeURIComponent(url)}`, '_blank')
}

// 从URL中获取文件名
const getFileName = (url) => {
  if (!url) return ''
  return url.split('/').pop()
}

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    SUBMITTED: 'warning',
    RESUBMITTED: 'warning',
    GRADED: 'success',
    RETURNED: 'danger'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    SUBMITTED: '已提交',
    RESUBMITTED: '已重新提交',
    GRADED: '已批改',
    RETURNED: '已退回'
  }
  return texts[status] || status
}

// 提交作业
const handleSubmit = () => {
  router.push(`/homework/submit/${route.params.id}`)
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
.filter-container {
  margin-bottom: 20px;
}

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

.section {
  margin-bottom: 30px;
}

.section h3 {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
  color: #303133;
}

.content-item {
  margin-bottom: 15px;
}

.content-item label {
  font-weight: bold;
  color: #606266;
  margin-right: 10px;
}

.score {
  font-size: 18px;
  color: #f56c6c;
  font-weight: bold;
}

.submit-button {
  margin-top: 20px;
  text-align: center;
}
</style>