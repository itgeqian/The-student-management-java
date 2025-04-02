<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input
        v-model="queryParams.keyword"
        placeholder="关键字"
        style="width: 200px"
        class="filter-item"
        @keyup.enter="handleQuery"
      />
      <el-select
        v-if="hasPermission(['admin', 'teacher'])"
        v-model="queryParams.courseId"
        placeholder="选择课程"
        style="width: 200px"
        class="filter-item"
        @change="handleCourseChange"
      >
        <el-option
          v-for="item in courseList"
          v-if="item && item.id"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        />
      </el-select>
      <!-- <el-select
        v-if="hasPermission(['admin', 'teacher'])"
        v-model="queryParams.classId"
        placeholder="选择班级"
        style="width: 200px"
        class="filter-item"
        @change="handleQuery"
      >
        <el-option
          v-for="item in classList"
          v-if="item && item.id"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        />
      </el-select> -->
      <el-select
        v-model="queryParams.type"
        placeholder="作业类型"
        style="width: 150px"
        class="filter-item"
        clearable
      >
        <el-option
          v-for="item in homeworkTypeOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <!-- <el-select
        v-model="queryParams.status"
        placeholder="作业状态"
        style="width: 200px"
        class="filter-item"
      >
        <el-option label="全部" value="" />
        <el-option label="进行中" :value="1" />
        <el-option label="已截止" :value="0" />
      </el-select> -->
      <el-button
        type="primary"
        class="filter-item"
        @click="handleQuery"
      >
        查询
      </el-button>
      <el-button
        v-if="hasPermission(['admin', 'teacher'])"
        type="primary"
        class="filter-item"
        @click="handleAdd"
      >
        布置作业
      </el-button>
      <!-- <el-button
        v-if="hasPermission(['admin', 'teacher'])"
        type="primary"
        class="filter-item"
        @click="handleStatistics"
      >
        作业统计
      </el-button> -->
    </div>

    <el-table
      v-loading="loading"
      :data="homeworkList"
      border
    >
      <el-table-column
        prop="title"
        label="作业标题"
      />
      <el-table-column
        prop="courseClass.course.name"
        label="课程名称"
        width="150"
      />
      <el-table-column
        prop="content"
        label="作业内容"
        show-overflow-tooltip
      />
      <el-table-column
        prop="deadline"
        label="截止时间"
        width="180"
      />
      <el-table-column
        prop="type"
        label="类型"
        width="120"
      >
        <template #default="{ row }">
          {{ getHomeworkTypeText(row.type) }}
        </template>
      </el-table-column>
      <el-table-column
        prop="submitCount"
        label="已提交/总人数"
        width="120"
      >
        <template #default="{ row }">
          {{ row.submitCount }}/{{ row.totalCount }}
        </template>
      </el-table-column>
      <el-table-column
        prop="status"
        label="状态"
        width="100"
      >
        <template #default="{ row }">
          <el-tag :type="!row.isDeadlinePassed ? 'success' : 'info'">
            {{ !row.isDeadlinePassed ? '进行中' : '已截止' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        width="300"
        fixed="right"
      >
        <template #default="{ row }">
          <el-button
            v-if="hasPermission(['admin', 'teacher']) && row"
            type="primary"
            link
            @click="handleEdit(row)"
          >
            编辑
          </el-button>
          <el-button
            v-if="hasPermission(['admin', 'teacher']) && row"
            type="danger"
            link
            @click="handleDelete(row)"
          >
            删除
          </el-button>
          <el-button
            v-if="hasPermission(['student']) && row"
            type="primary"
            link
            :disabled="row.status === 0"
            @click="handleSubmit(row)"
          >
            提交作业
          </el-button>
          <el-button
            v-if="hasPermission(['admin', 'teacher']) && row"
            type="primary"
            link
            @click="handleGrade(row)"
          >
            批改作业
          </el-button>
          <!-- <el-button
            type="primary"
            link
            @click="handleView(row)"
          >
            查看成绩
          </el-button> -->
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

    <!-- 布置/编辑作业对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="600px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      @closed="handleDialogClosed"
    >
      <el-form
        ref="homeworkFormRef"
        :model="homeworkForm"
        :rules="homeworkRules"
        label-width="100px"
      >
        <el-form-item label="作业标题" prop="title">
          <el-input v-model="homeworkForm.title" />
        </el-form-item>
        <el-form-item label="作业内容" prop="content">
          <el-input
            v-model="homeworkForm.content"
            type="textarea"
            :rows="4"
          />
        </el-form-item>
        <el-form-item label="作业类型" prop="type">
          <el-select v-model="homeworkForm.type" placeholder="请选择作业类型" style="width: 100%">
            <el-option label="平时作业" value="HOMEWORK" />
            <el-option label="实验作业" value="EXPERIMENT" />
            <el-option label="期末考试" value="EXAM" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程" prop="courseId" v-if="!homeworkForm.id">
          <el-select 
            v-model="homeworkForm.courseId" 
            placeholder="请选择课程" 
            style="width: 100%"
            @change="handleCourseChangeInForm"
            :loading="loading"
            filterable
          >
            <template v-if="courseList && courseList.length > 0">
              <el-option
                v-for="course in courseList"
                :key="course.id"
                :label="course.name"
                :value="course.id"
              />
            </template>
            <template v-else>
              <el-empty description="暂无课程数据" />
            </template>
          </el-select>
        </el-form-item>
        <el-form-item label="班级" prop="courseClassId">
          <el-select v-model="homeworkForm.courseClassId" placeholder="请选择班级" style="width: 100%">
            <el-option
              v-for="item in classList"
              :key="item.id"
              :label="item.className"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="截止时间" prop="deadline">
          <el-date-picker
            v-model="homeworkForm.deadline"
            type="datetime"
            placeholder="请选择截止时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="作业附件">
          <el-upload
            ref="uploadRef"
            :action="false"
            :http-request="handleUpload"
            :before-upload="beforeUpload"
            :on-remove="handleRemoveFile"
            :file-list="homeworkForm.attachmentUrl ? [{ name: '附件', url: homeworkForm.attachmentUrl }] : []"
            :limit="1"
            :on-exceed="handleExceed"
          >
            <el-button type="primary">上传附件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                只能上传一个文件，且大小不超过10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitForm">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 提交作业对话框 -->
    <el-dialog
      title="提交作业"
      v-model="submitDialogVisible"
      width="600px"
    >
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
            :rows="4"
          />
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
            action="/api/file/upload"
            :headers="uploadHeaders"
            :on-success="handleSubmitUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
          >
            <el-button type="primary">上传附件</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="submitDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="handleSubmitHomework">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 批改作业对话框 -->
    <el-dialog
      title="批改作业"
      v-model="gradeDialogVisible"
      width="800px"
    >
      <el-form ref="gradeFormRef" :model="gradeForm" label-width="100px">
        <el-form-item label="作业答案">
          <div v-html="gradeForm.answer"></div>
        </el-form-item>
        <el-form-item label="附件" v-if="gradeForm.attachmentUrl && gradeForm.attachmentUrl.length">
          <div v-for="file in gradeForm.attachmentUrl" :key="file.id">
            <el-link :href="file.url" type="primary" target="_blank">{{ file.name }}</el-link>
          </div>
        </el-form-item>
        <el-form-item label="作业成绩" prop="score">
          <el-input-number v-model="gradeForm.score" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="评语" prop="comment">
          <el-input
            v-model="gradeForm.comment"
            type="textarea"
            rows="3"
            placeholder="请输入评语"
          />
          <div class="quick-comments">
            <el-tag
              v-for="(comment, index) in quickComments"
              :key="index"
              class="quick-comment-item"
              @click="selectQuickComment(comment)"
            >
              {{ comment }}
            </el-tag>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="gradeDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="handleReturn" v-if="hasPermission(['admin', 'teacher'])">退回作业</el-button>
        <el-button type="primary" @click="submitGrade">确定</el-button>
      </template>
    </el-dialog>

    <!-- 统计对话框 -->
    <el-dialog
      title="作业统计"
      v-model="statisticsDialogVisible"
      width="800px"
    >
      <el-tabs v-model="activeStatTab">
        <el-tab-pane label="班级统计" name="class">
          <el-table :data="classStatistics" border>
            <el-table-column prop="className" label="班级" />
            <el-table-column prop="totalCount" label="总人数" />
            <el-table-column prop="submitCount" label="已提交" />
            <el-table-column prop="avgScore" label="平均分" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="作业统计" name="homework">
          <el-table :data="homeworkStatistics" border>
            <el-table-column prop="title" label="作业" />
            <el-table-column prop="submitCount" label="提交数" />
            <el-table-column prop="avgScore" label="平均分" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getHomeworkList, 
  getHomework, 
  createHomework, 
  updateHomework, 
  deleteHomework,
  getHomeworkStats
} from '@/api/homework'
import { getCourseList, getCourseClassList } from '@/api/course'
import { getClassList } from '@/api/grade'
import { useUserStore } from '@/stores/user'
import { hasPermission } from '@/utils/permission'
import Pagination from '@/components/Pagination/index.vue'
import { uploadFile, deleteFile } from '@/api/file'
import { homeworkTypeOptions, getHomeworkTypeText } from '@/constants/options'

const userStore = useUserStore()
const router = useRouter()
const uploadRef = ref()

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  type: undefined,
  courseId: '',
  classId: '',
  status: '',
  teacherId: hasPermission(['teacher']) ? userStore.userInfo?.id : undefined
})

// 数据列表
const loading = ref(false)
const courseList = ref([])
const classList = ref([])
const homeworkList = ref([])
const total = ref(0)

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const homeworkFormRef = ref()
const homeworkForm = reactive({
  id: '',
  title: '',
  content: '',
  type: '',
  courseId: '',
  courseClassId: '',
  deadline: '',
  attachmentUrl: ''
})

// 表单校验规则
const homeworkRules = {
  title: [
    { required: true, message: '请输入作业标题', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入作业内容', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择作业类型', trigger: 'change' }
  ],
  courseId: [
    { required: true, message: '请选择课程', trigger: 'change' }
  ],
  courseClassId: [
    { required: true, message: '请选择班级', trigger: 'change' }
  ],
  deadline: [
    { required: true, message: '请选择截止时间', trigger: 'change' }
  ]
}

// 获取课程列表
const getCourses = async () => {
  loading.value = true
  try {
    const res = await getCourseList()
    console.log('课程列表响应:', res)
    if (res.code === 200) {
      const data = res.data
      let records = Array.isArray(data) ? data : (data.list || data.records || [])
      courseList.value = records.filter(item => item && item.id)
      console.log('处理后的课程列表:', courseList.value)
    } else {
      ElMessage.error(res.msg || '获取课程列表失败')
      courseList.value = []
    }
  } catch (error) {
    console.error('获取课程列表失败:', error)
    ElMessage.error('获取课程列表失败')
    courseList.value = []
  } finally {
    loading.value = false
  }
}

// 获取班级列表
const getClasses = async (courseId) => {
  if (!courseId) {
    classList.value = []
    return
  }
  try {
    const res = await getCourseClassList({ courseId })
    classList.value = Array.isArray(res.data.records) ? res.data.records.filter(item => item && item.id) : []
  } catch (error) {
    console.error('获取班级列表失败:', error)
    ElMessage.error('获取班级列表失败')
    classList.value = []
  }
}

// 课程选择改变
const handleCourseChange = async () => {
  homeworkForm.courseClassId = ''
  classList.value = []
  if (queryParams.courseId) {
    await getClasses(queryParams.courseId)
  }
  handleQuery()
}

// 监听作业表单中的课程选择
watch(() => homeworkForm.courseId, async (newVal) => {
  homeworkForm.courseClassId = ''
  classList.value = []
  if (newVal) {
    await getClasses(newVal)
  }
})

// 课程选择改变
const handleCourseChangeInForm = async () => {
  homeworkForm.courseClassId = ''
  classList.value = []
  if (homeworkForm.courseId) {
    await getClasses(homeworkForm.courseId)
  }
}

// 获取作业列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getHomeworkList({
      ...queryParams,
      teacherId: hasPermission(['teacher']) ? userStore.userInfo?.id : undefined
    })
    homeworkList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取作业列表失败:', error)
    ElMessage.error('获取作业列表失败')
  } finally {
    loading.value = false
  }
}

// 查询按钮点击
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置查询
const resetQuery = () => {
  queryParams.keyword = ''
  queryParams.type = undefined
  queryParams.courseId = ''
  queryParams.classId = ''
  queryParams.status = ''
  classList.value = []
  handleQuery()
}

// 新增按钮点击
const handleAdd = async () => {
  dialogTitle.value = '布置作业'
  Object.keys(homeworkForm).forEach(key => {
    homeworkForm[key] = key === 'attachments' ? '' : ''
  })
  // 确保课程列表已加载
  loading.value = true
  try {
    await getCourses()
  } finally {
    loading.value = false
  }
  dialogVisible.value = true
}

// 编辑按钮点击
const handleEdit = async (row) => {
  try {
    const { data } = await getHomework(row.id)
    Object.assign(homeworkForm, data)
    dialogTitle.value = '编辑作业'
    dialogVisible.value = true
  } catch (error) {
    console.error('获取作业详情失败:', error)
    ElMessage.error('获取作业详情失败')
  }
}

// 提交表单
const submitForm = async () => {
  if (!homeworkFormRef.value) return

  try {
    await homeworkFormRef.value.validate()
    
    // 处理附件上传
    const attachmentPromises = homeworkForm.attachments ? [] : []

    if (attachmentPromises.length > 0) {
      const uploadResults = await Promise.all(attachmentPromises)
      // 更新附件列表，替换临时文件为已上传的文件
      homeworkForm.attachments = uploadResults[0].data
    }

    // 保存作业
    if (homeworkForm.id) {
      await updateHomework(homeworkForm)
      ElMessage.success('更新成功')
    } else {
      await createHomework(homeworkForm)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error('提交作业失败:', error)
    ElMessage.error(error.message || '提交失败')
  }
}

// 删除按钮点击
const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该作业吗？', '警告', {
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
  })
}

// 提交作业
const handleSubmit = (row) => {
  router.push(`/homework/submit/${row.id}`)
}

// 批改作业
const handleGrade = (row) => {
  router.push({
    name: 'HomeworkGrade',
    params: { id: row.id }
  })
}

// 查看统计
const handleStats = async (row) => {
  try {
    const { data } = await getHomeworkStats(row.id)
    classStatistics.value = data.classStats
    studentStatistics.value = data.studentStats
    statsDialogVisible.value = true
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  }
}

// 上传文件
const handleUpload = async ({ file }) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('module', 'homework')
  
  try {
    const { data } = await uploadFile(formData)
    homeworkForm.attachmentUrl = data
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
    homeworkForm.attachmentUrl = ''
  } catch (error) {
    console.error('删除文件失败:', error)
    ElMessage.error('删除文件失败')
  }
}

// 对话框关闭时的处理
const handleDialogClosed = () => {
  homeworkFormRef.value?.resetFields()
  Object.keys(homeworkForm).forEach(key => {
    homeworkForm[key] = key === 'attachments' ? '' : ''
  })
  classList.value = []
}

// 初始化
onMounted(() => {
  if (hasPermission(['admin', 'teacher'])) {
    getCourses()
  }
  getList()
})

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
</script>

<style scoped>
.filter-container {
  padding-bottom: 10px;
}
.filter-item {
  margin-right: 10px;
}
.dialog-footer {
  text-align: right;
}
.quick-comments {
  margin-top: 10px;
}
.quick-comment-item {
  margin-right: 10px;
  margin-bottom: 10px;
  cursor: pointer;
}
.quick-comment-item:hover {
  opacity: 0.8;
}
</style>
