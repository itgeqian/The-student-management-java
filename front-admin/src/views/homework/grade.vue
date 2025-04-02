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
            <el-link type="primary" @click="handleDownload(homework.attachmentUrl)">
                {{ getDisplayFileName(homework.attachmentUrl) }}
                <el-icon class="el-icon--right"><Download /></el-icon>
              </el-link>
          </div>
          <span v-else>无</span>
        </div>
      </div>
    </el-card>

    <el-card class="box-card submission-list">
      <template #header>
        <div class="card-header">
          <span>提交列表</span>
          <div class="header-operations">
            <el-button type="primary" @click="handleBatchGrade" :disabled="!selectedSubmissions.length">
              批量评分
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="submissionList"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="student.name" label="学生姓名" width="120">
          <template #default="{ row }">
            <span>{{ row.student?.name || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="student.studentNo" label="学号" width="120">
          <template #default="{ row }">
            <span>{{ row.student?.studentNo || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="student.email" label="邮箱" width="180">
          <template #default="{ row }">
            <span>{{ row.student?.email || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180">
          <template #default="{ row }">
            <span>{{ formatDateTime(row.submitTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="answer" label="作业内容" min-width="300">
          <template #default="{ row }">
            <div v-html="row.answer"></div>
          </template>
        </el-table-column>
        <el-table-column prop="attachmentUrl" label="附件" width="200">
          <template #default="{ row }">
            <div v-if="row.attachmentUrl">
              <el-link type="primary" @click="handleDownload(row.attachmentUrl)">
                {{ getDisplayFileName(row.attachmentUrl) }}
                <el-icon class="el-icon--right"><Download /></el-icon>
              </el-link>
            </div>
            <span v-else>无</span>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分数" width="100">
          <template #default="{ row }">
            <span v-if="row.score !== null">{{ row.score }}</span>
            <span v-else class="no-score">未评分</span>
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="评语" width="200">
          <template #default="{ row }">
            <span v-if="row.comment">{{ row.comment }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleGrade(row)">
              评分
            </el-button>
            <el-button type="warning" link @click="returnGrade(row)">
              退回
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>

    <!-- 评分对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="gradeFormRef"
        :model="gradeForm"
        :rules="gradeRules"
        label-width="100px"
      >
        <el-form-item label="学生答案">
          <div v-html="gradeForm.answer"></div>
        </el-form-item>
        <el-form-item label="附件">
          <div v-if="gradeForm.attachmentUrl">
            <div class="file-item">
              <el-link type="primary" @click="handleDownload(gradeForm.attachmentUrl)">
                {{ getDisplayFileName(gradeForm.attachmentUrl) }}
                <el-icon class="el-icon--right"><Download /></el-icon>
              </el-link>
            </div>
          </div>
          <span v-else>无</span>
        </el-form-item>
        <el-form-item label="分数" prop="score">
          <el-input-number
            v-model="gradeForm.score"
            :min="0"
            :max="100"
            :precision="1"
            :step="0.5"
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="评语" prop="comment">
          <el-input
            v-model="gradeForm.comment"
            type="textarea"
            :rows="3"
            placeholder="请输入评语"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitGrade">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 批量评分对话框 -->
    <el-dialog
      title="批量评分"
      v-model="batchDialogVisible"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="batchGradeFormRef"
        :model="batchGradeForm"
        :rules="gradeRules"
        label-width="100px"
      >
        <el-form-item label="分数" prop="score">
          <el-input-number
            v-model="batchGradeForm.score"
            :min="0"
            :max="100"
            :precision="1"
            :step="0.5"
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="评语" prop="comment">
          <el-input
            v-model="batchGradeForm.comment"
            type="textarea"
            :rows="3"
            placeholder="请输入评语"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="batchDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitBatchGrade">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { getHomework, getHomeworkSubmissions, gradeHomework, returnHomework, batchGradeHomework, downloadHomeworkFile } from '@/api/homework'
import Pagination from '@/components/Pagination/index.vue'

const route = useRoute()
const router = useRouter()

// 作业信息
const homework = ref({})
const loading = ref(false)
const submissionList = ref([])
const total = ref(0)
const selectedSubmissions = ref([])

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  homeworkId: route.params.id
})

// 评分表单
const dialogVisible = ref(false)
const dialogTitle = ref('')
const gradeFormRef = ref(null)
const gradeForm = reactive({
  id: '',
  answer: '',
  attachmentUrl: '',
  score: null,
  comment: ''
})

// 批量评分表单
const batchDialogVisible = ref(false)
const batchGradeFormRef = ref(null)
const batchGradeForm = reactive({
  score: null,
  comment: ''
})

// 表单验证规则
const gradeRules = {
  score: [
    { required: true, message: '请输入分数', trigger: 'blur' },
    { type: 'number', min: 0, max: 100, message: '分数必须在0-100之间', trigger: 'blur' }
  ]
}

// 获取作业信息
const getHomeworkInfo = async () => {
  try {
    const { data } = await getHomework(route.params.id)
    homework.value = data
  } catch (error) {
    console.error('获取作业信息失败:', error)
    ElMessage.error('获取作业信息失败')
  }
}

// 获取提交列表
const getList = async () => {
  loading.value = true
  try {
    const { data } = await getHomeworkSubmissions(route.params.id, {
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize
    })
    submissionList.value = data.records
    total.value = data.total
  } catch (error) {
    console.error('获取提交列表失败:', error)
    ElMessage.error('获取提交列表失败')
  } finally {
    loading.value = false
  }
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 下载文件
const handleDownload = async (fileUrl) => {
  if (!fileUrl) {
    ElMessage.warning('附件地址为空')
    return
  }
  
  window.open(`/api/api/file/download?fileUrl=${encodeURIComponent(fileUrl)}`, '_blank')
}

// 表格选择改变
const handleSelectionChange = (selection) => {
  selectedSubmissions.value = selection
}

// 评分按钮点击
const handleGrade = (row) => {
  dialogTitle.value = `评分 - ${row.student.name}`
  Object.assign(gradeForm, row)
  dialogVisible.value = true
}

// 批量评分按钮点击
const handleBatchGrade = () => {
  batchGradeForm.score = null
  batchGradeForm.comment = ''
  batchDialogVisible.value = true
}

// 提交评分
const handleSubmitGrade = async () => {
  if (!gradeFormRef.value) return

  try {
    await gradeFormRef.value.validate()
    await gradeHomework(gradeForm.id, {
      score: gradeForm.score,
      comment: gradeForm.comment
    })
    ElMessage.success('评分成功')
    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error('提交评分失败:', error)
    ElMessage.error(error.message || '提交失败')
  }
}

const returnGrade = async (row) => {
  ElMessageBox.confirm(
    '确认要退回该提交吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
    await returnHomework(row.id)
    ElMessage.success('退回成功')
    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error('退回失败:', error)
    ElMessage.error(error.message || '退回失败')
  }
    })
    .catch(() => {})
}

// 提交批量评分
const handleSubmitBatchGrade = async () => {
  if (!batchGradeFormRef.value) return

  try {
    await batchGradeFormRef.value.validate()
    const batchData = selectedSubmissions.value.map(submission => ({
      submissionId: submission.id,
      score: batchGradeForm.score,
      comment: batchGradeForm.comment
    }))
    await batchGradeHomework(batchData)
    ElMessage.success('批量评分成功')
    batchDialogVisible.value = false
    getList()
  } catch (error) {
    console.error('批量评分失败:', error)
    ElMessage.error(error.message || '提交失败')
  }
}

// 格式化时间
const formatDateTime = (date) => {
  const dateTime = new Date(date)
  const year = dateTime.getFullYear()
  const month = dateTime.getMonth() + 1
  const day = dateTime.getDate()
  const hour = dateTime.getHours()
  const minute = dateTime.getMinutes()
  const second = dateTime.getSeconds()
  return `${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')} ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}:${second.toString().padStart(2, '0')}`
}

// 从URL中获取文件名（仅显示）
const getDisplayFileName = (url) => {
  if (!url) return ''
  const fullName = url.split('\\').pop()
  return decodeURIComponent(fullName)
}

onMounted(() => {
  getHomeworkInfo()
  getList()
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
.no-score {
  color: #909399;
}
.dialog-footer {
  text-align: right;
}
</style>
