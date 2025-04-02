<template>
  <div class="app-container">
    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">作业成绩列表</span>
            <el-select v-model="filterType" placeholder="作业类型" clearable class="filter-select">
              <el-option
                v-for="item in homeworkTypes"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
            <el-select v-model="filterStatus" placeholder="提交状态" clearable class="filter-select">
              <el-option
                v-for="item in statusTypes"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
            <el-button type="primary" @click="handleQuery">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </div>
          <div class="student-info" v-if="studentInfo">
            <span class="info-item">学号：{{ studentInfo.studentNo }}</span>
            <span class="info-item">姓名：{{ studentInfo.name }}</span>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="filteredGradeList" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="课程信息" min-width="200">
          <template #default="{ row }">
            <div class="course-info">
              <div>
                <span class="label">课程班级：</span>
                <span>{{ row.homework.courseClass.name }}</span>
              </div>
              <div>
                <span class="label">学年：</span>
                <span>{{ row.homework.courseClass.year }}</span>
              </div>
              <div>
                <span class="label">学期：</span>
                <span>{{ getSemesterText(row.homework.courseClass.semester) }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="作业类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getHomeworkTagType(row.homework.type)">{{ getHomeworkType(row.homework.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="作业信息" min-width="180">
          <template #default="{ row }">
            <div class="homework-info">
              <div>
                <span class="label">标题：</span>
                <span>{{ row.homework.title }}</span>
              </div>
              <div>
                <span class="label">截止时间：</span>
                <span>{{ formatDateTime(row.homework.deadline) }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分数" width="80" align="center">
          <template #default="{ row }">
            <span :class="{ 'high-score': row.score >= 90, 'low-score': row.score < 60 }">
              {{ row.score || '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="160" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column label="评语" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.comment || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="viewDetail(row)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      title="作业详情"
      width="60%"
      destroy-on-close
    >
      <template v-if="currentHomework">
        <div class="detail-container">
          <div class="detail-item">
            <h3>课程信息</h3>
            <p><span class="label">课程班级：</span>{{ currentHomework.homework.courseClass.classroom }}</p>
            <p><span class="label">学期：</span>{{ getSemesterText(currentHomework.homework.courseClass.semester) }}</p>
          </div>
          <div class="detail-item">
            <h3>作业信息</h3>
            <p><span class="label">标题：</span>{{ currentHomework.homework.title }}</p>
            <p>
              <span class="label">类型：</span>
              <el-tag :type="getHomeworkTagType(currentHomework.homework.type)">
                {{ getHomeworkType(currentHomework.homework.type) }}
              </el-tag>
            </p>
            <p><span class="label">描述：</span>{{ currentHomework.homework.description }}</p>
            <p><span class="label">截止时间：</span>{{ formatDateTime(currentHomework.homework.deadline) }}</p>
            <div class="detail-item" v-if="currentHomework.homework.attachmentUrl">
            <h3>作业附件</h3>
            <el-button type="primary" @click="downloadAttachment(currentHomework.homework.attachmentUrl)">
              <el-icon><Download />下载</el-icon>
            </el-button>
          </div>
          </div>
          <div class="detail-item">
            <h3>提交信息</h3>
            <p><span class="label">提交时间：</span>{{ formatDateTime(currentHomework.submitTime) }}</p>
            <p>
              <span class="label">状态：</span>
              <el-tag :type="getStatusType(currentHomework.status)">{{ getStatusText(currentHomework.status) }}</el-tag>
            </p>
            <p>
              <span class="label">分数：</span>
              <span :class="{ 'high-score': currentHomework.score >= 90, 'low-score': currentHomework.score < 60 }">
                {{ currentHomework.score || '-' }}
              </span>
            </p>
            <p><span class="label">评语：</span>{{ currentHomework.comment || '-' }}</p>
          </div>
          <div class="detail-item" v-if="currentHomework.answer">
            <h3>作业答案</h3>
            <div class="answer-content">{{ currentHomework.answer }}</div>
          </div>
          <div class="detail-item" v-if="currentHomework.attachmentUrl">
            <h3>答案附件</h3>
            <el-button type="primary" @click="downloadAttachment(currentHomework.attachmentUrl)">
              <el-icon><Download /></el-icon>
              下载
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getStudentHomeworkGrades } from '@/api/homework'
import { getClassTimeText, getSemesterText } from '@/constants/options'

const userStore = useUserStore()
const loading = ref(false)
const total = ref(0)
const gradeList = ref([])
const dialogVisible = ref(false)
const currentHomework = ref(null)

// 查询参数
const queryParams = reactive({
  current: 1,
  size: 10
})

// 作业类型选项
const homeworkTypes = [
  { label: '平时作业', value: 'HOMEWORK' },
  { label: '实验作业', value: 'EXPERIMENT' },
  { label: '期末考试', value: 'EXAM' }
]

// 状态类型选项
const statusTypes = [
  { label: '已提交', value: 'SUBMITTED' },
  { label: '已批改', value: 'GRADED' },
  { label: '逾期', value: 'LATE' },
  { label: '未提交', value: 'MISSING' }
]

// 过滤类型
const filterType = ref('')
const filterStatus = ref('')

// 过滤后的列表
const filteredGradeList = computed(() => {
  let result = gradeList.value

  // 作业类型过滤
  if (filterType.value) {
    result = result.filter(item => item.homework.type === filterType.value)
  }

  // 状态过滤
  if (filterStatus.value) {
    result = result.filter(item => item.status === filterStatus.value)
  }

  return result
})

// 学生信息
const studentInfo = computed(() => {
  if (gradeList.value && gradeList.value.length > 0) {
    const firstRecord = gradeList.value[0]
    return {
      studentNo: firstRecord.student?.studentNo,
      name: firstRecord.student?.name
    }
  }
  return null
})

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'SUBMITTED':
      return 'primary'
    case 'GRADED':
      return 'success'
    case 'LATE':
      return 'warning'
    case 'MISSING':
      return 'danger'
    default:
      return 'info'
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'SUBMITTED':
      return '已提交'
    case 'GRADED':
      return '已批改'
    case 'LATE':
      return '逾期'
    case 'MISSING':
      return '未提交'
    default:
      return '未知'
  }
}

// 获取作业类型文本
const getHomeworkType = (type) => {
  switch (type) {
    case 'HOMEWORK':
      return '平时作业'
    case 'EXPERIMENT':
      return '实验作业'
    case 'EXAM':
      return '期末考试'
    default:
      return '未知'
  }
}

// 获取作业类型标签样式
const getHomeworkTagType = (type) => {
  switch (type) {
    case 'HOMEWORK':
      return 'info'
    case 'EXPERIMENT':
      return 'warning'
    case 'EXAM':
      return 'danger'
    default:
      return ''
  }
}

// 获取作业成绩列表
const getGradeList = async () => {
  if (!userStore.userInfo?.id) {
    ElMessage.warning('未获取到学生信息')
    return
  }
  
  loading.value = true
  try {
    const response = await getStudentHomeworkGrades(userStore?.userInfo?.id || 1, queryParams)
    if (response.data) {
      gradeList.value = response.data.records
      total.value = response.data.total
    }
  } catch (error) {
    console.error('获取作业成绩失败:', error)
    ElMessage.error('获取作业成绩失败')
  } finally {
    loading.value = false
  }
}

// 查询按钮点击事件
const handleQuery = () => {
  getGradeList()
}

// 重置按钮点击事件
const resetQuery = () => {
  filterType.value = ''
  filterStatus.value = ''
  getGradeList()
}

// 查看详情
const viewDetail = (row) => {
  currentHomework.value = row
  dialogVisible.value = true
}

// 下载附件
const downloadAttachment = (url) => {
  if (!url) {
    ElMessage.warning('附件地址无效')
    return
  }
  window.open(`/api/api/file/download?fileUrl=${encodeURIComponent(url)}`, '_blank')
}

// 分页处理
const handleSizeChange = (val) => {
  queryParams.size = val
  getGradeList()
}

const handleCurrentChange = (val) => {
  queryParams.current = val
  getGradeList()
}

onMounted(() => {
  getGradeList()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.list-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.title {
  font-size: 16px;
  font-weight: bold;
}

.filter-select {
  width: 120px;
}

.student-info {
  display: flex;
  gap: 20px;
}

.info-item {
  font-size: 14px;
  color: #606266;
}

.course-info,
.homework-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.label {
  font-weight: bold;
  color: #606266;
  margin-right: 8px;
}

.high-score {
  color: #67c23a;
  font-weight: bold;
}

.low-score {
  color: #f56c6c;
  font-weight: bold;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.detail-container {
  padding: 20px;
}

.detail-item {
  margin-bottom: 24px;
}

.detail-item h3 {
  margin-bottom: 16px;
  color: #409eff;
  font-size: 16px;
  font-weight: bold;
}

.detail-item p {
  margin: 8px 0;
  line-height: 1.6;
}

.answer-content {
  background-color: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  white-space: pre-wrap;
  line-height: 1.6;
}

:deep(.el-dialog__body) {
  padding: 0;
}
</style>