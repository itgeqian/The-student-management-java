<template>
  <div class="app-container">
    <div class="homework-detail">
      <el-descriptions title="作业详情" :column="1" border>
        <el-descriptions-item label="作业标题">{{ homework.title }}</el-descriptions-item>
        <el-descriptions-item label="作业内容">{{ homework.content }}</el-descriptions-item>
        <el-descriptions-item label="截止时间">{{ formatDateTime(homework.deadline) }}</el-descriptions-item>
        <el-descriptions-item label="作业附件" v-if="homework.attachmentUrl">
          <el-link type="primary" @click="downloadFile(homework.attachmentUrl)">
              {{ file.name }}
            </el-link>
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="filter-container">
      <el-input
        v-model="queryParams.keyword"
        placeholder="学号/姓名"
        style="width: 200px"
        class="filter-item"
        @keyup.enter="handleQuery"
      />
      <el-select v-model="queryParams.status" placeholder="批改状态" clearable class="filter-item" style="width: 150px">
        <el-option label="未批改" value="submitted" />
        <el-option label="已批改" value="graded" />
        <el-option label="已退回" value="returned" />
      </el-select>
      <el-button type="primary" class="filter-item" @click="handleQuery">查询</el-button>
      <el-button type="success" class="filter-item" @click="handleBatchGrade">批量评分</el-button>
      <el-button type="warning" class="filter-item" @click="quickCommentsVisible = true">快捷评语</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="submissionList"
      style="width: 100%"
      border
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="studentId" label="学号" width="120" />
      <el-table-column prop="studentName" label="姓名" width="120" />
      <el-table-column prop="submitTime" label="提交时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.submitTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="content" label="作业答案" show-overflow-tooltip>
        <template #default="{ row }">
          <el-button type="primary" link @click="handleViewSubmission(row)">查看答案</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'graded' ? 'success' : row.status === 'returned' ? 'danger' : 'info'">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="score" label="成绩" width="100">
        <template #default="{ row }">
          {{ row.status === 'graded' ? row.score : '-' }}
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="200">
        <template #default="{ row }">
          <el-button
            v-if="row.status !== 'graded'"
            type="primary"
            link
            @click="handleGrade(row)"
          >
            评分
          </el-button>
          <el-button
            v-if="row.status === 'graded'"
            type="warning"
            link
            @click="handleGrade(row)"
          >
            重新评分
          </el-button>
          <el-button
            v-if="row.status !== 'returned'"
            type="danger"
            link
            @click="handleReturn(row)"
          >
            退回
          </el-button>
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

    <!-- 查看作业答案对话框 -->
    <el-dialog
      v-model="submissionDialogVisible"
      title="作业答案"
      width="600px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="学生">{{ currentSubmission?.studentName }} ({{ currentSubmission?.studentId }})</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ formatDateTime(currentSubmission?.submitTime) }}</el-descriptions-item>
        <el-descriptions-item label="作业答案">{{ currentSubmission?.content }}</el-descriptions-item>
        <el-descriptions-item label="提交附件" v-if="currentSubmission?.attachments?.length">
          <div v-for="file in currentSubmission.attachments" :key="file.id" class="file-item">
            <el-link type="primary" @click="downloadFile(file)">
              {{ file.name }}
            </el-link>
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 评分对话框 -->
    <el-dialog
      v-model="gradeDialogVisible"
      :title="currentSubmission?.status === 'graded' ? '重新评分' : '评分'"
      width="500px"
    >
      <el-form
        ref="gradeFormRef"
        :model="gradeForm"
        :rules="gradeRules"
        label-width="100px"
      >
        <el-form-item label="成绩" prop="score">
          <el-input-number
            v-model="gradeForm.score"
            :min="0"
            :max="100"
            :precision="0"
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="评语" prop="comment">
          <el-input
            v-model="gradeForm.comment"
            type="textarea"
            :rows="4"
          />
          <div class="quick-comments">
            <el-button
              v-for="(comment, index) in quickComments"
              :key="index"
              link
              type="primary"
              @click="appendQuickComment(comment)"
            >
              {{ comment.content }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="gradeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitGrade">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 批量评分对话框 -->
    <el-dialog
      v-model="batchGradeDialogVisible"
      title="批量评分"
      width="500px"
    >
      <el-form
        ref="batchGradeFormRef"
        :model="batchGradeForm"
        :rules="gradeRules"
        label-width="100px"
      >
        <el-form-item label="成绩" prop="score">
          <el-input-number
            v-model="batchGradeForm.score"
            :min="0"
            :max="100"
            :precision="0"
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="评语" prop="comment">
          <el-input
            v-model="batchGradeForm.comment"
            type="textarea"
            :rows="4"
          />
          <div class="quick-comments">
            <el-button
              v-for="(comment, index) in quickComments"
              :key="index"
              link
              type="primary"
              @click="appendQuickComment(comment, true)"
            >
              {{ comment.content }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchGradeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitBatchGrade">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 快捷评语管理对话框 -->
    <el-dialog
      v-model="quickCommentsVisible"
      title="快捷评语管理"
      width="500px"
    >
      <div class="quick-comments-container">
        <div v-for="(comment, index) in quickComments" :key="index" class="quick-comment-item">
          <el-input v-model="comment.content" placeholder="请输入评语内容" />
          <el-button type="danger" link @click="removeQuickComment(index)">删除</el-button>
        </div>
        <el-button type="primary" @click="addQuickComment">添加评语</el-button>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="quickCommentsVisible = false">取消</el-button>
          <el-button type="primary" @click="saveQuickComments">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getHomework,
  getHomeworkSubmissions,
  gradeHomework,
  batchGradeHomework,
  getQuickComments,
  addQuickComment,
  deleteQuickComment,
  downloadHomeworkFile
} from '@/api/homework'

const route = useRoute()
const homeworkId = route.params.homeworkId

const loading = ref(false)
const total = ref(0)
const homework = ref({})
const submissionList = ref([])
const selectedSubmissions = ref([])
const currentSubmission = ref(null)
const submissionDialogVisible = ref(false)
const gradeDialogVisible = ref(false)
const batchGradeDialogVisible = ref(false)
const quickCommentsVisible = ref(false)
const quickComments = ref([])
const gradeFormRef = ref(null)
const batchGradeFormRef = ref(null)

// 查询参数
const queryParams = reactive({
  page: 1,
  limit: 10,
  keyword: '',
  status: undefined
})

// 评分表单
const gradeForm = reactive({
  score: 0,
  comment: ''
})

// 批量评分表单
const batchGradeForm = reactive({
  score: 0,
  comment: ''
})

// 表单校验规则
const gradeRules = {
  score: [
    { required: true, message: '请输入成绩', trigger: 'blur' },
    { type: 'number', min: 0, max: 100, message: '成绩必须在0-100之间', trigger: 'blur' }
  ],
  comment: [
    { required: true, message: '请输入评语', trigger: 'blur' }
  ]
}

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

// 获取提交列表
const getList = async () => {
  loading.value = true
  try {
    const { data } = await getHomeworkSubmissions(homeworkId, queryParams)
    submissionList.value = data.list
    total.value = data.total
  } catch (error) {
    console.error('获取提交列表失败:', error)
    ElMessage.error('获取提交列表失败')
  } finally {
    loading.value = false
  }
}

// 加载快捷评语
const loadQuickComments = async () => {
  try {
    const { data } = await getQuickComments(homeworkId)
    quickComments.value = data.map(item => ({ content: item }))
  } catch (error) {
    console.error('获取快捷评语失败:', error)
    ElMessage.error('获取快捷评语失败')
  }
}

// 查看作业答案
const handleViewSubmission = (row) => {
  currentSubmission.value = row
  submissionDialogVisible.value = true
}

// 评分
const handleGrade = (row) => {
  currentSubmission.value = row
  gradeForm.score = row.score || 0
  gradeForm.comment = row.comment || ''
  gradeDialogVisible.value = true
}

// 提交评分
const submitGrade = async () => {
  if (!gradeFormRef.value) return
  
  try {
    await gradeFormRef.value.validate()
    await gradeHomework(currentSubmission.value.id, {
      score: gradeForm.score,
      comment: gradeForm.comment
    })
    ElMessage.success('评分成功')
    gradeDialogVisible.value = false
    getList()
  } catch (error) {
    console.error('评分失败:', error)
    ElMessage.error(error.message || '评分失败')
  }
}

// 退回作业
const handleReturn = (row) => {
  ElMessageBox.confirm('确认退回该作业吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await gradeHomework(row.id, {
        status: 'returned',
        comment: '作业被退回，请修改后重新提交'
      })
      ElMessage.success('退回成功')
      getList()
    } catch (error) {
      console.error('退回作业失败:', error)
      ElMessage.error('退回失败')
    }
  })
}

// 批量评分
const handleBatchGrade = () => {
  if (selectedSubmissions.value.length === 0) {
    ElMessage.warning('请选择需要评分的作业')
    return
  }
  batchGradeDialogVisible.value = true
}

// 提交批量评分
const submitBatchGrade = async () => {
  if (!batchGradeFormRef.value) return
  
  try {
    await batchGradeFormRef.value.validate()
    const submissionIds = selectedSubmissions.value.map(item => item.id)
    await batchGradeHomework({
      submissionIds,
      ...batchGradeForm
    })
    ElMessage.success('批量评分成功')
    batchGradeDialogVisible.value = false
    getList()
  } catch (error) {
    console.error('批量评分失败:', error)
    ElMessage.error(error.message || '批量评分失败')
  }
}

// 表格选择
const handleSelectionChange = (selection) => {
  selectedSubmissions.value = selection
}

// 快捷评语相关方法
const addQuickComment = () => {
  quickComments.value.push({ content: '' })
}

const removeQuickComment = (index) => {
  quickComments.value.splice(index, 1)
}

const appendQuickComment = (comment, isBatch = false) => {
  const target = isBatch ? batchGradeForm : gradeForm
  target.comment = target.comment ? `${target.comment}\n${comment.content}` : comment.content
}

const saveQuickComments = async () => {
  try {
    const comments = quickComments.value.map(item => item.content).filter(Boolean)
    await addQuickComment(homeworkId, { comments })
    ElMessage.success('保存评语成功')
    quickCommentsVisible.value = false
  } catch (error) {
    console.error('保存评语失败:', error)
    ElMessage.error('保存评语失败')
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

// 查询
const handleQuery = () => {
  queryParams.page = 1
  getList()
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

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    submitted: '未批改',
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
  getList()
  loadQuickComments()
})
</script>

<style scoped>
.homework-detail {
  margin-bottom: 20px;
}

.filter-container {
  margin-bottom: 20px;
}

.filter-item {
  margin-right: 10px;
}

.file-item {
  margin: 5px 0;
}

.quick-comments {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.el-button + .el-button {
  margin-left: 0;
}
</style>
