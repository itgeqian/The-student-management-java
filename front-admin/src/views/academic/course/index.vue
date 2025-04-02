<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input
        v-model="queryParams.keyword"
        placeholder="课程编号/名称"
        style="width: 200px"
        class="filter-item"
        @keyup.enter="handleQuery"
      />
      <el-select v-model="queryParams.department" placeholder="开课院系" clearable class="filter-item" style="width: 200px">
        <el-option
          v-for="item in departmentList"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        />
      </el-select>
      <el-button type="primary" class="filter-item" @click="handleQuery">查询</el-button>
      <el-button type="primary" class="filter-item" @click="handleAdd">新增</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="courseList"
      style="width: 100%"
      border
    >
      <el-table-column prop="courseId" label="课程编号" width="120" />
      <el-table-column prop="name" label="课程名称" width="200" />
      <el-table-column prop="department" label="开课院系" />
      <el-table-column prop="credit" label="学分" width="80" />
      <el-table-column prop="hours" label="学时" width="80" />
      <el-table-column prop="type" label="课程类型" width="120">
        <template #default="{ row }">
          {{ courseTypeMap[row.type] }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="200">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button type="success" link @click="handleManageClass(row)">班级管理</el-button>
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

    <!-- 课程信息表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增课程' : '编辑课程'"
      width="600px"
    >
      <el-form
        ref="courseFormRef"
        :model="courseForm"
        :rules="courseRules"
        label-width="100px"
      >
        <el-form-item label="课程编号" prop="courseId">
          <el-input v-model="courseForm.courseId" :disabled="dialogType === 'edit'" />
        </el-form-item>
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="courseForm.name" />
        </el-form-item>
        <el-form-item label="开课院系" prop="department">
          <el-select v-model="courseForm.department" placeholder="请选择院系">
            <el-option
              v-for="item in departmentList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学分" prop="credit">
          <el-input-number v-model="courseForm.credit" :min="0.5" :max="10" :step="0.5" />
        </el-form-item>
        <el-form-item label="学时" prop="hours">
          <el-input-number v-model="courseForm.hours" :min="16" :max="120" :step="2" />
        </el-form-item>
        <el-form-item label="课程类型" prop="type">
          <el-select v-model="courseForm.type" placeholder="请选择课程类型">
            <el-option
              v-for="(label, value) in courseTypeMap"
              :key="value"
              :label="label"
              :value="value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input v-model="courseForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="courseForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCourseList, addCourse, updateCourse, deleteCourse } from '@/api/course'
import { getDepartmentList } from '@/api/system'
import router from '@/router'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('add')
const courseList = ref([])
const departmentList = ref([])
const total = ref(0)
const courseFormRef = ref(null)

const courseTypeMap = {
  required: '必修课',
  elective: '选修课',
  optional: '任选课',
  practice: '实践课'
}

const queryParams = reactive({
  page: 1,
  limit: 10,
  keyword: '',
  department: undefined
})

const courseForm = reactive({
  courseId: '',
  name: '',
  department: '',
  credit: 2,
  hours: 32,
  type: 'required',
  description: '',
  status: 1
})

const courseRules = {
  courseId: [
    { required: true, message: '请输入课程编号', trigger: 'blur' },
    { pattern: /^[A-Z0-9]{6,8}$/, message: '课程编号必须为6-8位大写字母和数字', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入课程名称', trigger: 'blur' }
  ],
  department: [
    { required: true, message: '请选择开课院系', trigger: 'change' }
  ],
  credit: [
    { required: true, message: '请输入学分', trigger: 'blur' }
  ],
  hours: [
    { required: true, message: '请输入学时', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择课程类型', trigger: 'change' }
  ]
}

// 获取课程列表
const getList = async () => {
  loading.value = true
  try {
    const { data } = await getCourseList(queryParams)
    courseList.value = data.list
    total.value = data.total
  } catch (error) {
    console.error('Failed to get course list:', error)
    ElMessage.error('获取课程列表失败')
  } finally {
    loading.value = false
  }
}

// 获取院系列表
const getDepartments = async () => {
  try {
    const { data } = await getDepartmentList()
    departmentList.value = data.records;
  } catch (error) {
    console.error('Failed to get department list:', error)
    ElMessage.error('获取院系列表失败')
  }
}

const handleQuery = () => {
  queryParams.page = 1
  getList()
}

const handleAdd = () => {
  dialogType.value = 'add'
  Object.assign(courseForm, {
    courseId: '',
    name: '',
    department: '',
    credit: 2,
    hours: 32,
    type: 'required',
    description: '',
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.assign(courseForm, row)
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确认删除该课程吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteCourse(row.courseId)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('Failed to delete course:', error)
      ElMessage.error('删除失败')
    }
  })
}

const handleManageClass = (row) => {
  router.push({
    name: 'CourseClass',
    params: { courseId: row.courseId }
  })
}

const handleSubmit = async () => {
  try {
    await courseFormRef.value.validate()
    if (dialogType.value === 'add') {
      await addCourse(courseForm)
      ElMessage.success('添加成功')
    } else {
      await updateCourse(courseForm)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error('Failed to submit course form:', error)
    ElMessage.error(error.message || '操作失败')
  }
}

const handleSizeChange = (val) => {
  queryParams.limit = val
  getList()
}

const handleCurrentChange = (val) => {
  queryParams.page = val
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

.filter-container {
  padding-bottom: 10px;
  .filter-item {
    margin-right: 10px;
  }
}
</style>
