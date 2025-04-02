<template>
  <div class="class-container">
    <el-card class="filter-container">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="班级名称">
          <el-input
            v-model="queryParams.className"
            placeholder="请输入班级名称"
            clearable
          />
        </el-form-item>
        <el-form-item label="专业">
          <el-select v-model="queryParams.majorId" placeholder="选择专业" clearable>
            <el-option
              v-for="item in majorOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="年级">
          <el-select v-model="queryParams.grade" placeholder="选择年级" clearable>
            <el-option
              v-for="item in gradeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="mt-20">
      <template #header>
        <div class="card-header">
          <el-button type="primary" @click="handleAdd">新增班级</el-button>
        </div>
      </template>

      <el-table :data="classList" style="width: 100%" border>
        <el-table-column prop="className" label="班级名称" />
        <el-table-column prop="majorName" label="专业" />
        <el-table-column prop="grade" label="年级" />
        <el-table-column prop="advisor" label="班主任" />
        <el-table-column prop="studentCount" label="学生人数" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="primary" link @click="handleStudents(row)">
              学生管理
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑班级对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="500px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="班级名称" prop="className">
          <el-input v-model="form.className" placeholder="请输入班级名称" />
        </el-form-item>
        <el-form-item label="专业" prop="majorId">
          <el-select v-model="form.majorId" placeholder="选择专业">
            <el-option
              v-for="item in majorOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-select v-model="form.grade" placeholder="选择年级">
            <el-option
              v-for="item in gradeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="班主任" prop="advisor">
          <el-select
            v-model="form.advisor"
            placeholder="选择班主任"
            filterable
            remote
            :remote-method="handleTeacherSearch"
            :loading="teacherLoading"
          >
            <el-option
              v-for="item in teacherOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="handleSubmit">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()

// 查询参数
const queryParams = ref({
  className: '',
  majorId: undefined,
  grade: undefined,
  pageNum: 1,
  pageSize: 10
})

// 班级列表数据
const classList = ref([
  {
    id: 1,
    className: '计算机科学1班',
    majorName: '计算机科学与技术',
    majorId: 1,
    grade: 2023,
    advisor: '张老师',
    studentCount: 45,
    createTime: '2024-01-01 10:00:00'
  },
  {
    id: 2,
    className: '软件工程2班',
    majorName: '软件工程',
    majorId: 2,
    grade: 2023,
    advisor: '李老师',
    studentCount: 42,
    createTime: '2024-01-01 10:00:00'
  }
])

const total = ref(50)

// 专业选项
const majorOptions = ref([
  { value: 1, label: '计算机科学与技术' },
  { value: 2, label: '软件工程' },
  { value: 3, label: '网络工程' },
  { value: 4, label: '信息安全' }
])

// 年级选项
const gradeOptions = ref([
  { value: 2024, label: '2024级' },
  { value: 2023, label: '2023级' },
  { value: 2022, label: '2022级' },
  { value: 2021, label: '2021级' }
])

// 教师选项
const teacherOptions = ref([])
const teacherLoading = ref(false)

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const form = ref({
  className: '',
  majorId: undefined,
  grade: undefined,
  advisor: undefined
})

// 表单校验规则
const rules = {
  className: [
    { required: true, message: '请输入班级名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  majorId: [
    { required: true, message: '请选择专业', trigger: 'change' }
  ],
  grade: [
    { required: true, message: '请选择年级', trigger: 'change' }
  ],
  advisor: [
    { required: true, message: '请选择班主任', trigger: 'change' }
  ]
}

// 查询
const handleQuery = () => {
  queryParams.value.pageNum = 1
  fetchData()
}

// 重置查询
const resetQuery = () => {
  queryParams.value = {
    className: '',
    majorId: undefined,
    grade: undefined,
    pageNum: 1,
    pageSize: 10
  }
  fetchData()
}

// 获取数据
const fetchData = () => {
  // 这里应该调用后端API获取数据
  console.log('获取班级列表数据')
}

// 新增班级
const handleAdd = () => {
  dialogTitle.value = '新增班级'
  dialogVisible.value = true
}

// 编辑班级
const handleEdit = (row) => {
  dialogTitle.value = '编辑班级'
  dialogVisible.value = true
  Object.assign(form.value, row)
}

// 删除班级
const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确认要删除该班级吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(() => {
      ElMessage({
        type: 'success',
        message: '删除成功'
      })
    })
    .catch(() => {})
}

// 学生管理
const handleStudents = (row) => {
  router.push({
    path: '/academic/class/students',
    query: { classId: row.id }
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (valid) {
      // 这里应该调用API保存数据
      ElMessage({
        type: 'success',
        message: '保存成功'
      })
      dialogVisible.value = false
      resetForm()
      fetchData()
    }
  })
}

// 重置表单
const resetForm = () => {
  if (!formRef.value) return
  formRef.value.resetFields()
  form.value = {
    className: '',
    majorId: undefined,
    grade: undefined,
    advisor: undefined
  }
}

// 搜索教师
const handleTeacherSearch = (query) => {
  if (query) {
    teacherLoading.value = true
    // 模拟远程搜索
    setTimeout(() => {
      teacherOptions.value = [
        { value: 1, label: '张老师' },
        { value: 2, label: '李老师' },
        { value: 3, label: '王老师' }
      ].filter(item => item.label.includes(query))
      teacherLoading.value = false
    }, 200)
  } else {
    teacherOptions.value = []
  }
}

// 分页
const handleSizeChange = (val) => {
  queryParams.value.pageSize = val
  fetchData()
}

const handleCurrentChange = (val) => {
  queryParams.value.pageNum = val
  fetchData()
}
</script>

<style scoped>
.class-container {
  padding: 20px;
}

.mt-20 {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>
