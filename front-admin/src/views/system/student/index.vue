<template>
  <div class="app-container">
    <div class="content-container">
      <div class="filter-container">
        <el-form :inline="true" :model="queryParams" class="demo-form-inline">
          <el-input
            v-model="queryParams.keyword"
            placeholder="学号/姓名"
            style="width: 200px"
            class="filter-item"
            @keyup.enter="handleQuery"
          />
          <el-select v-model="queryParams.departmentId" placeholder="所属院系" clearable class="filter-item" style="width: 200px">
            <el-option
              v-for="item in departmentList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
          <el-select v-model="queryParams.grade" placeholder="年级" clearable class="filter-item" style="width: 120px">
            <el-option
              v-for="item in gradeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-button type="primary" class="filter-item" @click="handleQuery">查询</el-button>
          <el-button type="primary" @click="handleAdd">新增</el-button>
        </el-form>
      </div>

      <div class="table-container">

        <el-table
          v-loading="loading"
          :data="studentList"
          style="width: 100%"
          border
        >
          <el-table-column prop="studentNo" label="学号" width="120" />
          <el-table-column prop="name" label="姓名" width="120" />
          <el-table-column prop="gender" label="性别" width="80">
            <template #default="{ row }">
              {{ row.gender === 1 ? '男' : '女' }}
            </template>
          </el-table-column>
          <el-table-column label="所属院系">
            <template #default="{ row }">
              {{ departmentList.find(d => d.id === row.departmentId)?.name || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="grade" label="年级" width="100" />
          <el-table-column prop="className" label="班级" width="120" />
          <el-table-column prop="email" label="邮箱" />
          <el-table-column prop="phone" label="联系电话" width="120" />
          <el-table-column fixed="right" label="操作" width="150">
            <template #default="{ row }">
              <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
              <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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
          :page-sizes="[2, 10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 学生信息表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增学生' : '编辑学生'"
      width="600px"
    >
      <el-form
        ref="studentFormRef"
        :model="studentForm"
        :rules="studentRules"
        label-width="100px"
      >
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="studentForm.studentNo" :disabled="dialogType === 'edit'" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="studentForm.name" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="studentForm.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="所属院系" prop="departmentId">
          <el-select v-model="studentForm.departmentId" placeholder="请选择院系">
            <el-option
              v-for="item in departmentList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-select v-model="studentForm.grade" placeholder="请选择年级">
            <el-option
              v-for="item in gradeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="班级" prop="className">
          <el-input v-model="studentForm.className" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="studentForm.email" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="studentForm.phone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="导入学生信息"
      width="400px"
    >
      <el-upload
        class="upload-demo"
        :action="uploadUrl"
        :headers="uploadHeaders"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :before-upload="beforeUpload"
      >
        <el-button type="primary">选择文件</el-button>
        <template #tip>
          <div class="el-upload__tip">
            只能上传xlsx文件，且不超过10MB
          </div>
        </template>
      </el-upload>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="importDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="downloadTemplate">下载模板</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStudentList, createStudent, updateStudent, deleteStudent, importStudents, exportStudents } from '@/api/student'
import { getDepartmentList } from '@/api/system'
import { getToken } from '@/utils/auth'

const loading = ref(false)
const dialogVisible = ref(false)
const importDialogVisible = ref(false)
const dialogType = ref('add')
const studentList = ref([])
const departmentList = ref([])
const total = ref(0)
const studentFormRef = ref(null)

// 获取当前年份，生成近5年的年级选项
const currentYear = new Date().getFullYear()
const gradeOptions = Array.from({ length: 5 }, (_, i) => ({
  value: currentYear - i,
  label: `${currentYear - i}级`
}))

const uploadUrl = import.meta.env.VITE_APP_BASE_API + '/system/student/import'
const uploadHeaders = {
  Authorization: 'Bearer ' + getToken()
}

const queryParams = reactive({
  current: 1,
  size: 10,
  keyword: '',
  departmentId: undefined,
  grade: undefined
})

const studentForm = reactive({
  studentNo: '',
  name: '',
  gender: 1,
  departmentId: '',
  grade: '',
  className: '',
  email: '',
  phone: ''
})

const studentRules = {
  studentNo: [
    { required: true, message: '请输入学号', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  departmentId: [
    { required: true, message: '请选择院系', trigger: 'change' }
  ],
  grade: [
    { required: true, message: '请选择年级', trigger: 'change' }
  ],
  className: [
    { required: true, message: '请输入班级', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

// 重置表单
const resetForm = () => {
  studentForm.studentNo = ''
  studentForm.name = ''
  studentForm.gender = 1
  studentForm.departmentId = ''
  studentForm.grade = ''
  studentForm.className = ''
  studentForm.email = ''
  studentForm.phone = ''
}

// 获取学生列表
const getList = async () => {
  loading.value = true
  try {
    const { data } = await getStudentList(queryParams)
    studentList.value = data.list || data.records || []
    total.value = data.total || 0
    console.log('Student list:', studentList.value) // 添加调试日志
  } catch (error) {
    console.error('获取学生列表失败:', error)
    ElMessage.error('获取学生列表失败')
  } finally {
    loading.value = false
  }
}

// 获取院系列表
const getDepartments = async () => {
  try {
    const response = await getDepartmentList()
    departmentList.value = response.data.records || []
  } catch (error) {
    console.error('Failed to get department list:', error)
    ElMessage.error('获取院系列表失败')
  }
}

const handleQuery = () => {
  // queryParams.current = 1
  getList()
}

const handleAdd = () => {
  dialogType.value = 'add'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.assign(studentForm, row)
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确认删除该学生信息吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteStudent(row.studentNo)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('Failed to delete student:', error)
      ElMessage.error('删除失败')
    }
  })
}

const handleSubmit = async () => {
  try {
    await studentFormRef.value.validate()
    if (dialogType.value === 'add') {
      await createStudent(studentForm)
      ElMessage.success('添加成功')
    } else {
      await updateStudent(studentForm)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error('Failed to submit student form:', error)
    ElMessage.error(error.message || '操作失败')
  }
}

const handleImport = () => {
  importDialogVisible.value = true
}

const handleExport = async () => {
  try {
    await exportStudents(queryParams)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('Failed to export students:', error)
    ElMessage.error('导出失败')
  }
}

const handleUploadSuccess = (response) => {
  ElMessage.success('导入成功')
  importDialogVisible.value = false
  getList()
}

const handleUploadError = () => {
  ElMessage.error('导入失败')
}

const beforeUpload = (file) => {
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isExcel) {
    ElMessage.error('只能上传xlsx文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过10MB!')
    return false
  }
  return true
}

const downloadTemplate = () => {
  window.location.href = import.meta.env.VITE_APP_BASE_API + '/system/student/template'
}

const handleSizeChange = (val) => {
  queryParams.size = val
  getList()
}

const handleCurrentChange = (val) => {
  queryParams.current = val
  getList()
}

onMounted(() => {
  getList()
  getDepartments()
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}

.content-container {
  padding: 20px;
  background-color: #fff;
  border: 1px solid #ddd;
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

.filter-container {
  padding-bottom: 10px;
  .filter-item {
    margin-right: 10px;
  }
}

.table-container {
  margin-top: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

.upload-demo {
  text-align: center;
  .el-upload__tip {
    margin-top: 10px;
    color: #666;
  }
}
</style>
