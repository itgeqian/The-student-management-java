<template>
  <div class="app-container">
    <div class="content-container">
      <div class="filter-container">
        <el-form :inline="true" :model="queryParams" class="demo-form-inline">
          <el-input
            v-model="queryParams.keyword"
            placeholder="课程名称/课程代码"
            style="width: 200px"
            class="filter-item"
            @keyup.enter="handleQuery"
          />
          <el-select
            v-model="queryParams.semester"
            placeholder="学期"
            style="width: 200px"
            class="filter-item"
          >
            <el-option
              v-for="item in semesterOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-select
            v-model="queryParams.departmentId"
            placeholder="开课院系"
            style="width: 200px"
            class="filter-item"
            clearable
          >
            <el-option
              v-for="dept in departmentOptions"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
          <el-button
            type="primary"
            class="filter-item"
            @click="handleQuery"
          >
            查询
          </el-button>
          <el-button
            type="primary"
            class="filter-item"
            @click="handleAdd"
          >
            新增
          </el-button>
          <!-- <el-button
            type="primary"
            class="filter-item"
            @click="handleImport"
          >
            导入
          </el-button>
          <el-button
            type="primary"
            class="filter-item"
            @click="handleExport"
          >
            导出
          </el-button> -->
        </el-form>
      </div>

      <div class="table-container">
        <el-table
          v-loading="loading"
          :data="courseList"
          style="width: 100%"
          border
        >
          <el-table-column
            prop="name"
            label="课程名称"
            width="200"
          />
          <el-table-column
            prop="courseCode"
            label="课程代码"
            width="120"
          />
          <el-table-column
            prop="credit"
            label="学分"
            width="80"
          />
          <el-table-column
            prop="maxStudents"
            label="最大学生数"
            width="100"
          />
          <el-table-column
            prop="semester"
            label="学期"
            width="180"
          >
            <template #default="{ row }">
              {{ getSemesterText(row.semester) }}
            </template>
          </el-table-column>
          <el-table-column
            prop="description"
            label="课程描述"
          />
          <el-table-column
            prop="departmentId"
            label="开课院系"
            width="180"
          >
            <template #default="{ row }">
              {{ getDepartmentName(row.departmentId) }}
            </template>
          </el-table-column>
          <el-table-column
            label="操作"
            width="200"
            fixed="right"
          >
            <template #default="{ row }">
              <el-button
                type="primary"
                link
                @click="handleEdit(row)"
              >
                编辑
              </el-button>
              <el-button
                type="danger"
                link
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="pagination-container">
        <el-pagination
          v-show="total > 0"
          :total="total"
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 新增/编辑课程对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增课程' : '编辑课程'"
      width="500px"
      append-to-body
    >
      <el-form
        ref="courseFormRef"
        :model="courseForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="courseForm.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="课程代码" prop="courseCode">
          <el-input v-model="courseForm.courseCode" placeholder="请输入课程代码" />
        </el-form-item>
        <el-form-item label="学分" prop="credit">
          <el-input-number
            v-model="courseForm.credit"
            :min="0.5"
            :max="10"
            :step="0.5"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="最大学生数" prop="maxStudents">
          <el-input-number
            v-model="courseForm.maxStudents"
            :min="1"
            :max="500"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="任课教师" prop="teacherId">
          <el-select
            v-model="courseForm.teacherId"
            placeholder="请选择任课教师"
            style="width: 100%"
            clearable
          >
            <el-option
              v-for="teacher in teacherOptions"
              :key="teacher.id"
              :label="teacher.name"
              :value="teacher.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="开课院系" prop="departmentId">
          <el-select
            v-model="courseForm.departmentId"
            placeholder="请选择开课院系"
            style="width: 100%"
          >
            <el-option
              v-for="dept in departmentOptions"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学期" prop="semester">
          <el-select
            v-model="courseForm.semester"
            placeholder="请选择学期"
            style="width: 100%"
          >
            <el-option
              v-for="item in semesterOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input
            v-model="courseForm.description"
            type="textarea"
            placeholder="请输入课程描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitForm">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 导入课程对话框 -->
    <el-dialog
      title="导入课程"
      v-model="importDialogVisible"
      width="500px"
    >
      <el-upload
        ref="uploadRef"
        :action="uploadUrl"
        :headers="uploadHeaders"
        :on-success="handleImportSuccess"
        :on-error="handleImportError"
        :limit="1"
        :auto-upload="false"
      >
        <el-button slot="trigger" size="small" type="primary">选取文件</el-button>
        <el-button style="margin-left: 10px;" size="small" type="success" @click="handleImport">导入</el-button>
        <div slot="tip" class="el-upload__tip">只能上传xls/xlsx文件</div>
      </el-upload>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getCourseList, 
  createCourse, 
  updateCourse, 
  deleteCourse,
  importCourses,
  exportCourses
} from '@/api/course'
import { getDepartmentList } from '@/api/department'
import { getTeacherListNoPage } from '@/api/teacher'
import { getToken } from '@/utils/auth'
import { semesterOptions, getSemesterText } from '@/constants/options'

const loading = ref(false)
const dialogVisible = ref(false)
const importDialogVisible = ref(false)
const dialogType = ref('add')
const courseList = ref([])
const departmentOptions = ref([])
const teacherOptions = ref([])
const total = ref(0)
const courseFormRef = ref(null)
const uploadRef = ref(null)

// 上传相关配置
const uploadUrl = `${import.meta.env.VITE_APP_BASE_API}/api/courses/import`
const uploadHeaders = {
  Authorization: 'Bearer ' + getToken()
}

// 查询参数
const queryParams = reactive({
  current: 1,
  size: 10,
  keyword: '',
  semester: '',
  departmentId: undefined,
  teacherId: undefined
})

// 课程表单
const courseForm = reactive({
  id: '',
  name: '',
  courseCode: '',
  credit: 2,
  maxStudents: 50,
  teacherId: '',
  semester: '',
  departmentId: '',
  description: ''
})

// 表单校验规则
const rules = {
  name: [
    { required: true, message: '请输入课程名称', trigger: 'blur' }
  ],
  courseCode: [
    { required: true, message: '请输入课程代码', trigger: 'blur' }
  ],
  credit: [
    { required: true, message: '请输入学分', trigger: 'blur' },
    { type: 'number', message: '学分必须为数字', trigger: 'blur' }
  ],
  maxStudents: [
    { required: true, message: '请输入最大学生数', trigger: 'blur' },
    { type: 'number', message: '最大学生数必须为数字', trigger: 'blur' }
  ],
  teacherId: [
    { required: true, message: '请选择任课教师', trigger: 'change' }
  ],
  semester: [
    { required: true, message: '请选择学期', trigger: 'change' }
  ],
  departmentId: [
    { required: true, message: '请选择开课院系', trigger: 'change' }
  ]
}

// 根据院系ID获取院系名称
const getDepartmentName = (departmentId) => {
  if (!departmentId) return ''
  if (!Array.isArray(departmentOptions.value)) {
    console.warn('departmentOptions is not an array:', departmentOptions.value)
    return ''
  }
  const dept = departmentOptions.value.find(d => d.id === departmentId)
  return dept ? dept.name : ''
}

// 根据教师ID获取教师名称
const getTeacherName = (teacherId) => {
  if (!teacherId) return ''
  if (!Array.isArray(teacherOptions.value)) {
    console.warn('teacherOptions is not an array:', teacherOptions.value)
    return ''
  }
  const teacher = teacherOptions.value.find(t => t.id === teacherId)
  return teacher ? teacher.name : ''
}

// 获取课程列表
const getList = async () => {
  loading.value = true
  try {
    const response = await getCourseList(queryParams)
    const data = response.data
    courseList.value = data?.list || data?.records || []
    total.value = data?.total || 0
    console.log('课程列表:', courseList.value) // 添加日志
  } catch (error) {
    console.error('获取课程列表失败:', error)
    ElMessage.error('获取课程列表失败')
  } finally {
    loading.value = false
  }
}

// 查询按钮点击
const handleQuery = () => {
  // queryParams.current = 1
  getList()
}

// 重置查询
const resetQuery = () => {
  queryParams.name = ''
  queryParams.courseCode = ''
  queryParams.semester = ''
  queryParams.departmentId = undefined
  queryParams.teacherId = undefined
  handleQuery()
}

// 新增按钮点击
const handleAdd = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
  courseForm.id = ''
  courseForm.name = ''
  courseForm.courseCode = ''
  courseForm.credit = 2
  courseForm.maxStudents = 50
  courseForm.teacherId = ''
  courseForm.semester = ''
  courseForm.departmentId = ''
  courseForm.description = ''
  if (courseFormRef.value) {
    courseFormRef.value.resetFields()
  }
}

// 编辑按钮点击
const handleEdit = (row) => {
  dialogType.value = 'edit'
  dialogVisible.value = true
  Object.assign(courseForm, row)
}

// 提交表单
const submitForm = async () => {
  if (!courseFormRef.value) return
  await courseFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (dialogType.value === 'add') {
          await createCourse(courseForm)
          ElMessage.success('添加成功')
        } else {
          await updateCourse(courseForm)
          ElMessage.success('修改成功')
        }
        dialogVisible.value = false
        getList()
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error('操作失败')
      }
    }
  })
}

// 删除按钮点击
const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该课程吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteCourse(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  })
}

// 导入课程
const handleImport = async () => {
  try {
    const formData = new FormData()
    formData.append('file', uploadRef.value.uploadFiles[0].raw)
    await importCourses(formData)
    ElMessage.success('导入成功')
    importDialogVisible.value = false
    getList()
  } catch (error) {
    console.error('导入失败:', error)
    ElMessage.error('导入失败')
  }
}

// 导出课程
const handleExport = async () => {
  try {
    const response = await exportCourses(queryParams)
    const blob = new Blob([response], { type: 'application/vnd.ms-excel' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', '课程信息.xlsx')
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 分页大小改变
const handleSizeChange = (val) => {
  queryParams.size = val
  getList()
}

// 页码改变
const handleCurrentChange = (val) => {
  queryParams.current = val
  getList()
}

// 初始化
onMounted(() => {
  getList()
  getDepartmentList().then(response => {
    console.log('部门数据:', response.data) // 添加日志
    departmentOptions.value = response.data?.list || response.data?.records || []
  }).catch(error => {
    console.error('获取院系列表失败:', error)
    ElMessage.error('获取院系列表失败')
  })
  getTeacherListNoPage().then(response => {
    console.log('教师数据:', response.data) // 添加日志
    teacherOptions.value = response.data?.list || response.data?.records || []
  }).catch(error => {
    console.error('获取教师列表失败:', error)
    ElMessage.error('获取教师列表失败')
  })
})
</script>

<style scoped>
/* 移除所有局部样式，使用全局统一样式 */
</style>
