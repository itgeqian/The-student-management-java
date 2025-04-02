<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input
        v-model="queryParams.keyword"
        placeholder="作业标题"
        style="width: 200px"
        class="filter-item"
        @keyup.enter="handleQuery"
      />
      <el-select v-model="queryParams.status" placeholder="作业状态" clearable class="filter-item" style="width: 150px">
        <el-option label="进行中" :value="1" />
        <el-option label="已截止" :value="2" />
      </el-select>
      <el-button type="primary" class="filter-item" @click="handleQuery">查询</el-button>
      <el-button type="primary" class="filter-item" @click="handleAdd">布置作业</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="homeworkList"
      style="width: 100%"
      border
    >
      <el-table-column prop="title" label="作业标题" width="200" />
      <el-table-column prop="content" label="作业内容" show-overflow-tooltip />
      <el-table-column prop="deadline" label="截止时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.deadline) }}
        </template>
      </el-table-column>
      <el-table-column prop="submitCount" label="已提交/总人数" width="120">
        <template #default="{ row }">
          {{ row.submitCount }}/{{ row.totalCount }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '进行中' : '已截止' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="300">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button type="success" link @click="handleViewSubmissions(row)">查看提交</el-button>
          <el-button type="warning" link @click="handleStats(row)">统计</el-button>
          <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <!-- 作业表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '布置作业' : '编辑作业'"
      width="700px"
    >
      <el-form
        ref="homeworkFormRef"
        :model="homeworkForm"
        :rules="homeworkRules"
        label-width="100px"
      >
        <el-form-item label="作业模板">
          <el-select v-model="selectedTemplate" placeholder="选择作业模板" clearable @change="handleTemplateChange">
            <el-option
              v-for="item in templates"
              :key="item.id"
              :label="item.title"
              :value="item.id"
            />
          </el-select>
          <el-button type="primary" link @click="handleSaveTemplate">保存为模板</el-button>
        </el-form-item>
        <el-form-item label="作业标题" prop="title">
          <el-input v-model="homeworkForm.title" />
        </el-form-item>
        <el-form-item label="作业内容" prop="content">
          <el-input v-model="homeworkForm.content" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="截止时间" prop="deadline">
          <el-date-picker
            v-model="homeworkForm.deadline"
            type="datetime"
            placeholder="选择截止时间"
            value-format="YYYY-MM-DD HH:mm:ss"
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
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 作业统计对话框 -->
    <el-dialog
      v-model="statsDialogVisible"
      title="作业统计"
      width="800px"
    >
      <el-tabs v-model="activeStatsTab">
        <el-tab-pane label="基础统计" name="basic">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="作业标题">{{ currentHomework?.title }}</el-descriptions-item>
            <el-descriptions-item label="截止时间">{{ formatDateTime(currentHomework?.deadline) }}</el-descriptions-item>
            <el-descriptions-item label="总人数">{{ currentHomework?.totalCount }}</el-descriptions-item>
            <el-descriptions-item label="已提交人数">{{ currentHomework?.submitCount }}</el-descriptions-item>
            <el-descriptions-item label="未提交人数">{{ currentHomework?.totalCount - currentHomework?.submitCount }}</el-descriptions-item>
            <el-descriptions-item label="平均分">{{ currentHomework?.averageScore || '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="最高分">{{ currentHomework?.maxScore || '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="最低分">{{ currentHomework?.minScore || '暂无' }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
        <el-tab-pane label="成绩分布" name="distribution">
          <div class="stats-chart" ref="chartRef"></div>
        </el-tab-pane>
        <el-tab-pane label="提交时间分析" name="timeline">
          <div class="timeline-chart" ref="timelineChartRef"></div>
        </el-tab-pane>
      </el-tabs>
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
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import {
  getHomeworkList,
  createHomework,
  updateHomework,
  deleteHomework,
  getHomeworkStats,
  getHomeworkTemplates,
  saveHomeworkTemplate,
  getQuickComments,
  addQuickComment,
  deleteQuickComment
} from '@/api/homework'
import { getToken } from '@/utils/auth'

const route = useRoute()
const router = useRouter()
const courseId = route.params.courseId
const loading = ref(false)
const total = ref(0)
const homeworkList = ref([])
const dialogVisible = ref(false)
const dialogType = ref('add')
const statsDialogVisible = ref(false)
const activeStatsTab = ref('basic')
const quickCommentsVisible = ref(false)
const quickComments = ref([])
const templates = ref([])
const selectedTemplate = ref(null)
const chartRef = ref(null)
const timelineChartRef = ref(null)
const currentHomework = ref(null)
const homeworkFormRef = ref(null)
const fileList = ref([])

// 查询参数
const queryParams = reactive({
  page: 1,
  limit: 10,
  keyword: '',
  status: undefined,
  courseId: route.params.courseId
})

// 作业表单
const homeworkForm = reactive({
  id: '',
  title: '',
  content: '',
  deadline: '',
  courseId: route.params.courseId,
  attachments: []
})

// 表单校验规则
const homeworkRules = {
  title: [
    { required: true, message: '请输入作业标题', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入作业内容', trigger: 'blur' }
  ],
  deadline: [
    { required: true, message: '请选择截止时间', trigger: 'change' }
  ]
}

// 上传配置
const uploadUrl = '/api/homework/files/upload'
const uploadHeaders = {
  Authorization: getToken()
}

// 获取作业列表
const getList = async () => {
  loading.value = true
  try {
    const { data } = await getHomeworkList(queryParams)
    homeworkList.value = data.list
    total.value = data.total
  } catch (error) {
    console.error('获取作业列表失败:', error)
    ElMessage.error('获取作业列表失败')
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  queryParams.page = 1
  getList()
}

// 重置查询
const resetQuery = () => {
  queryParams.keyword = ''
  queryParams.status = undefined
  handleQuery()
}

// 新增作业
const handleAdd = () => {
  dialogType.value = 'add'
  homeworkForm.id = ''
  homeworkForm.title = ''
  homeworkForm.content = ''
  homeworkForm.deadline = ''
  homeworkForm.attachments = []
  fileList.value = []
  selectedTemplate.value = null
  dialogVisible.value = true
}

// 编辑作业
const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.assign(homeworkForm, row)
  fileList.value = row.attachments || []
  dialogVisible.value = true
}

// 删除作业
const handleDelete = (row) => {
  ElMessageBox.confirm('确认要删除该作业吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteHomework(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除作业失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 查看提交情况
const handleViewSubmissions = (row) => {
  router.push({
    path: `/academic/homework/${row.id}/submissions`
  })
}

// 查看统计
const handleStats = async (row) => {
  try {
    currentHomework.value = row
    const { data } = await getHomeworkStats(row.id)
    statsDialogVisible.value = true
    // 等待 DOM 更新后初始化图表
    await nextTick()
    initCharts(data)
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  }
}

// 文件上传相关方法
const beforeUpload = (file) => {
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('上传文件大小不能超过 10MB!')
    return false
  }
  return true
}

const handleUploadSuccess = (response) => {
  homeworkForm.attachments.push(response.data)
  ElMessage.success('上传成功')
}

const handleUploadError = () => {
  ElMessage.error('上传失败')
}

// 提交表单
const handleSubmit = async () => {
  if (!homeworkFormRef.value) return
  
  try {
    await homeworkFormRef.value.validate()
    
    if (dialogType.value === 'add') {
      await createHomework(homeworkForm)
      ElMessage.success('布置作业成功')
    } else {
      await updateHomework(homeworkForm)
      ElMessage.success('更新作业成功')
    }
    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error('保存作业失败:', error)
    ElMessage.error(error.message || '保存失败')
  }
}

// 分页方法
const handleSizeChange = (val) => {
  queryParams.limit = val
  getList()
}

const handleCurrentChange = (val) => {
  queryParams.page = val
  getList()
}

// 获取作业模板列表
const getTemplates = async () => {
  try {
    const { data } = await getHomeworkTemplates()
    templates.value = data
  } catch (error) {
    console.error('获取作业模板失败:', error)
    ElMessage.error('获取作业模板失败')
  }
}

// 处理模板选择
const handleTemplateChange = (templateId) => {
  if (!templateId) {
    homeworkForm.title = ''
    homeworkForm.content = ''
    return
  }
  
  const template = templates.value.find(t => t.id === templateId)
  if (template) {
    homeworkForm.title = template.title
    homeworkForm.content = template.content
  }
}

// 保存作业模板
const handleSaveTemplate = async () => {
  try {
    await saveHomeworkTemplate({
      title: homeworkForm.title,
      content: homeworkForm.content
    })
    ElMessage.success('保存模板成功')
    getTemplates()
  } catch (error) {
    console.error('保存模板失败:', error)
    ElMessage.error('保存模板失败')
  }
}

// 快捷评语相关方法
const addQuickComment = () => {
  quickComments.value.push({ content: '' })
}

const removeQuickComment = (index) => {
  quickComments.value.splice(index, 1)
}

const saveQuickComments = async () => {
  try {
    const comments = quickComments.value.map(item => item.content).filter(Boolean)
    await addQuickComment(currentHomework.value.id, { comments })
    ElMessage.success('保存评语成功')
    quickCommentsVisible.value = false
  } catch (error) {
    console.error('保存评语失败:', error)
    ElMessage.error('保存评语失败')
  }
}

// 加载快捷评语
const loadQuickComments = async () => {
  try {
    const { data } = await getQuickComments(currentHomework.value.id)
    quickComments.value = data.map(item => ({ content: item }))
  } catch (error) {
    console.error('获取快捷评语失败:', error)
    ElMessage.error('获取快捷评语失败')
  }
}

// 增强统计图表
const initCharts = (data) => {
  // 成绩分布图表
  const chart = echarts.init(chartRef.value)
  const option = {
    title: {
      text: '成绩分布统计'
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['0-59', '60-69', '70-79', '80-89', '90-100']
    },
    yAxis: {
      type: 'value',
      name: '人数'
    },
    series: [{
      data: data.scoreDistribution || [],
      type: 'bar',
      label: {
        show: true,
        position: 'top'
      }
    }]
  }
  chart.setOption(option)

  // 提交时间分析图表
  if (timelineChartRef.value) {
    const timelineChart = echarts.init(timelineChartRef.value)
    const timelineOption = {
      title: {
        text: '作业提交时间分布'
      },
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'time',
        name: '提交时间'
      },
      yAxis: {
        type: 'value',
        name: '提交数量'
      },
      series: [{
        name: '提交数量',
        type: 'line',
        data: data.submitTimeline || [],
        smooth: true
      }]
    }
    timelineChart.setOption(timelineOption)
  }
}

onMounted(() => {
  if (!courseId) {
    ElMessage.error('参数错误')
    router.push('/academic/course')
    return
  }
  getList()
  getTemplates()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.filter-container {
  padding-bottom: 10px;
  .filter-item {
    margin-right: 10px;
  }
}

.stats-chart,
.timeline-chart {
  height: 400px;
  margin-top: 20px;
}

.quick-comments-container {
  max-height: 400px;
  overflow-y: auto;
}

.quick-comment-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  gap: 10px;
}
</style>
