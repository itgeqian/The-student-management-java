<template>
  <div class="app-container">
    <div class="content-container">
      <div class="filter-container">
        <el-form :inline="true" :model="queryParams" class="demo-form-inline">
          <el-input
            v-model="queryParams.name"
            placeholder="教师工号/姓名"
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
          <el-button type="primary" class="filter-item" @click="handleQuery">查询</el-button>
          <el-button type="primary" @click="handleAdd">新增</el-button>
        </el-form>
      </div>

      <div class="table-container">

        <el-table
          v-loading="loading"
          :data="teacherList"
          style="width: 100%"
          border
        >
          <el-table-column prop="teacherNo" label="教师工号" width="120" />
          <el-table-column prop="name" label="姓名" width="120" />
          <!-- <el-table-column prop="gender" label="性别" width="80">
            <template #default="{ row }">
              {{ row.gender === 1 ? '男' : '女' }}
            </template>
          </el-table-column> -->
          <el-table-column label="所属院系">
            <template #default="{ row }">
              {{ departmentList.find(d => d.id === row.departmentId)?.name || '-' }}
            </template>
          </el-table-column>
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
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 教师信息表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增教师' : '编辑教师'"
      width="600px"
    >
      <el-form
        ref="teacherFormRef"
        :model="teacherForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="教师工号" prop="teacherNo">
          <el-input v-model="teacherForm.teacherNo" :disabled="dialogType === 'edit'" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="teacherForm.name" />
        </el-form-item>
        <!-- <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="teacherForm.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item> -->
        <el-form-item label="所属院系" prop="departmentId">
          <el-select v-model="teacherForm.departmentId" placeholder="请选择院系">
            <el-option
              v-for="item in departmentList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="teacherForm.email" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="teacherForm.phone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 导入教师对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="导入教师"
      width="600px"
    >
      <el-upload
        ref="uploadRef"
        :action="importTeachers"
        :on-success="handleImportSuccess"
        :on-error="handleImportError"
        :limit="1"
        :auto-upload="false"
      >
        <el-button type="primary">点击上传</el-button>
        <template #tip>
          <div class="el-upload__tip">只能上传 excel 文件</div>
        </template>
      </el-upload>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="importDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleImport">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getTeacherList, 
  createTeacher, 
  updateTeacher, 
  deleteTeacher,
  importTeachers,
  exportTeachers
} from '@/api/teacher'
import { getDepartmentList } from '@/api/system'

const loading = ref(false)
const dialogVisible = ref(false)
const importDialogVisible = ref(false)
const dialogType = ref('add')
const teacherList = ref([])
const departmentList = ref([])
const total = ref(0)
const teacherFormRef = ref(null)

// 查询参数
const queryParams = reactive({
  current: 1,
  size: 10,
  name: '',
  departmentId: undefined
})

// 教师表单
const teacherForm = reactive({
  teacherNo: '',
  name: '',
  gender: 1,
  departmentId: '',
  title: '',
  email: '',
  phone: ''
})

// 表单校验规则
const rules = {
  teacherNo: [
    { required: true, message: '请输入教师工号', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入教师姓名', trigger: 'blur' }
  ],
  departmentId: [
    { required: true, message: '请选择所属院系', trigger: 'change' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

// 职称格式化
const formatTitle = (title) => {
  const titleMap = {
    'professor': '教授',
    'associate_professor': '副教授',
    'lecturer': '讲师',
    'assistant': '助教'
  }
  return titleMap[title] || title
}

// 获取教师列表
const getList = async () => {
  loading.value = true
  try {
    const { data } = await getTeacherList(queryParams)
    teacherList.value = data.list || data.records || []
    total.value = data.total || 0
    console.log('Teacher list:', teacherList.value) // 添加调试日志
  } catch (error) {
    console.error('获取教师列表失败:', error)
    ElMessage.error('获取教师列表失败')
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
  queryParams.departmentId = undefined
  handleQuery()
}

// 新增按钮点击
const handleAdd = () => {
  dialogType.value = 'add'
  teacherForm.id = ''
  teacherForm.name = ''
  teacherForm.gender = 1
  teacherForm.departmentId = ''
  teacherForm.title = ''
  teacherForm.email = ''
  teacherForm.phone = ''
  dialogVisible.value = true
}

// 编辑按钮点击
const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.assign(teacherForm, row)
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  if (!teacherFormRef.value) return
  
  await teacherFormRef.value.validate()
  
  try {
    if (dialogType.value === 'add') {
      await createTeacher(teacherForm)
      ElMessage.success('添加成功')
    } else {
      await updateTeacher(teacherForm)
      ElMessage.success('修改成功')
    }
    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(error.message || '提交失败')
  }
}

// 删除按钮点击
const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该教师吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteTeacher(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  })
}

// 导入教师
const handleImport = async () => {
  try {
    const formData = new FormData()
    formData.append('file', uploadRef.value.uploadFiles[0].raw)
    await importTeachers(formData)
    ElMessage.success('导入成功')
    importDialogVisible.value = false
    getList()
  } catch (error) {
    console.error('导入失败:', error)
    ElMessage.error('导入失败')
  }
}

// 导出教师
const handleExport = async () => {
  try {
    const response = await exportTeachers(queryParams)
    const blob = new Blob([response.data], { type: 'application/vnd.ms-excel' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', '教师信息.xlsx')
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
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
  getDepartmentList().then((response) => {
    departmentList.value = response.data?.list || response.data?.records || response.data || []
  })
})
</script>

<style scoped>
/* 移除所有局部样式，使用全局统一样式 */
</style>
