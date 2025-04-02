&lt;template>
  &lt;div class="app-container">
    &lt;div class="filter-container">
      &lt;el-input
        v-model="queryParams.title"
        placeholder="作业标题"
        style="width: 200px;"
        class="filter-item"
        @keyup.enter="handleQuery"
      />
      &lt;el-select
        v-model="queryParams.courseId"
        placeholder="课程"
        style="width: 200px"
        class="filter-item"
      >
        &lt;el-option
          v-for="item in courseOptions"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        />
      &lt;/el-select>
      &lt;el-button
        type="primary"
        class="filter-item"
        @click="handleQuery"
      >
        查询
      &lt;/el-button>
      &lt;el-button
        class="filter-item"
        @click="resetQuery"
      >
        重置
      &lt;/el-button>
      &lt;el-button
        type="success"
        class="filter-item"
        @click="handleAdd"
      >
        新增
      &lt;/el-button>
    &lt;/div>

    &lt;el-table
      v-loading="loading"
      :data="assignmentList"
      style="width: 100%"
      border
    >
      &lt;el-table-column
        prop="title"
        label="作业标题"
        width="200"
      />
      &lt;el-table-column
        prop="description"
        label="作业描述"
        show-overflow-tooltip
      />
      &lt;el-table-column
        prop="courseId"
        label="所属课程"
        width="180"
      >
        &lt;template #default="{ row }">
          {{ getCourseName(row.courseId) }}
        &lt;/template>
      &lt;/el-table-column>
      &lt;el-table-column
        prop="deadline"
        label="截止日期"
        width="180"
      />
      &lt;el-table-column
        label="操作"
        width="200"
        fixed="right"
      >
        &lt;template #default="{ row }">
          &lt;el-button
            type="primary"
            link
            @click="handleEdit(row)"
          >
            编辑
          &lt;/el-button>
          &lt;el-button
            type="danger"
            link
            @click="handleDelete(row)"
          >
            删除
          &lt;/el-button>
        &lt;/template>
      &lt;/el-table-column>
    &lt;/el-table>

    &lt;pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.page"
      v-model:limit="queryParams.limit"
      @pagination="getList"
    />

    &lt;el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增作业' : '编辑作业'"
      width="500px"
    >
      &lt;el-form
        ref="assignmentFormRef"
        :model="assignmentForm"
        :rules="rules"
        label-width="100px"
      >
        &lt;el-form-item label="作业标题" prop="title">
          &lt;el-input v-model="assignmentForm.title" />
        &lt;/el-form-item>
        &lt;el-form-item label="作业描述" prop="description">
          &lt;el-input
            v-model="assignmentForm.description"
            type="textarea"
            :rows="4"
          />
        &lt;/el-form-item>
        &lt;el-form-item label="所属课程" prop="courseId">
          &lt;el-select
            v-model="assignmentForm.courseId"
            placeholder="请选择课程"
            style="width: 100%"
          >
            &lt;el-option
              v-for="item in courseOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          &lt;/el-select>
        &lt;/el-form-item>
        &lt;el-form-item label="截止日期" prop="deadline">
          &lt;el-date-picker
            v-model="assignmentForm.deadline"
            type="datetime"
            placeholder="选择日期时间"
            style="width: 100%"
          />
        &lt;/el-form-item>
      &lt;/el-form>
      &lt;template #footer>
        &lt;span class="dialog-footer">
          &lt;el-button @click="dialogVisible = false">取消&lt;/el-button>
          &lt;el-button type="primary" @click="handleSubmit">确定&lt;/el-button>
        &lt;/span>
      &lt;/template>
    &lt;/el-dialog>
  &lt;/div>
&lt;/template>

&lt;script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from '@/components/Pagination/index.vue'
// TODO: 导入作业相关的 API
// import { getAssignmentList, createAssignment, updateAssignment, deleteAssignment } from '@/api/assignment'
import { getCourseList } from '@/api/course'

const loading = ref(false)
const total = ref(0)
const assignmentList = ref([])
const courseOptions = ref([])
const dialogVisible = ref(false)
const dialogType = ref('add')
const assignmentFormRef = ref(null)

const queryParams = reactive({
  page: 1,
  limit: 10,
  title: '',
  courseId: undefined
})

const assignmentForm = reactive({
  id: undefined,
  title: '',
  description: '',
  courseId: undefined,
  deadline: ''
})

const rules = {
  title: [
    { required: true, message: '请输入作业标题', trigger: 'blur' }
  ],
  courseId: [
    { required: true, message: '请选择所属课程', trigger: 'change' }
  ],
  deadline: [
    { required: true, message: '请选择截止日期', trigger: 'change' }
  ]
}

// 获取作业列表
const getList = async () => {
  loading.value = true
  try {
    // TODO: 实现获取作业列表的逻辑
    // const response = await getAssignmentList(queryParams)
    // assignmentList.value = response.data.records
    // total.value = response.data.total
  } catch (error) {
    console.error('获取作业列表失败:', error)
    ElMessage.error('获取作业列表失败')
  } finally {
    loading.value = false
  }
}

// 获取课程列表
const getCourses = async () => {
  try {
    const response = await getCourseList({ page: 1, limit: 1000 })
    courseOptions.value = response.data?.list || response.data?.records || []
  } catch (error) {
    console.error('获取课程列表失败:', error)
    ElMessage.error('获取课程列表失败')
  }
}

// 根据课程ID获取课程名称
const getCourseName = (courseId) => {
  if (!courseId) return ''
  const course = courseOptions.value.find(c => c.id === courseId)
  return course ? course.name : ''
}

// 查询按钮点击
const handleQuery = () => {
  queryParams.page = 1
  getList()
}

// 重置查询
const resetQuery = () => {
  queryParams.title = ''
  queryParams.courseId = undefined
  handleQuery()
}

// 新增按钮点击
const handleAdd = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
  assignmentForm.id = undefined
  assignmentForm.title = ''
  assignmentForm.description = ''
  assignmentForm.courseId = undefined
  assignmentForm.deadline = ''
}

// 编辑按钮点击
const handleEdit = (row) => {
  dialogType.value = 'edit'
  dialogVisible.value = true
  Object.assign(assignmentForm, row)
}

// 提交表单
const handleSubmit = async () => {
  if (!assignmentFormRef.value) return
  
  try {
    await assignmentFormRef.value.validate()
    // TODO: 实现提交作业的逻辑
    // if (dialogType.value === 'add') {
    //   await createAssignment(assignmentForm)
    // } else {
    //   await updateAssignment(assignmentForm)
    // }
    ElMessage.success(dialogType.value === 'add' ? '新增成功' : '修改成功')
    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

// 删除按钮点击
const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确认要删除该作业吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // TODO: 实现删除作业的逻辑
      // await deleteAssignment(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除作业失败:', error)
      ElMessage.error('删除作业失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  getList()
  getCourses()
})
&lt;/script>

&lt;style scoped>
.filter-container {
  padding-bottom: 10px;
}
.filter-item {
  margin-right: 10px;
}
&lt;/style>
