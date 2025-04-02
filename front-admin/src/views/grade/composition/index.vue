<template>
  <div class="grade-composition-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>成绩组成设置</span>
          <el-button type="primary" @click="handleAdd">添加评分项</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="课程">
          <el-select
            v-model="searchForm.courseId"
            placeholder="选择课程"
            clearable
            filterable
            remote
            :remote-method="handleCourseSearch"
            :loading="courseLoading"
          >
            <el-option
              v-for="item in courseOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
        v-loading="tableLoading"
        :data="tableData"
        style="width: 100%"
        border
      >
        <el-table-column prop="name" label="评分项名称" />
        <el-table-column prop="weight" label="权重" width="120">
          <template #default="scope">
            {{ scope.row.weight }}%
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="120">
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.type)">
              {{ getTypeText(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              link
              size="small"
              @click="handleEdit(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              link
              size="small"
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 评分项表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加评分项' : '编辑评分项'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="composition-form"
      >
        <el-form-item label="课程" prop="courseId">
          <el-select
            v-model="form.courseId"
            placeholder="选择课程"
            style="width: 100%"
            :disabled="dialogType === 'edit'"
          >
            <el-option
              v-for="item in courseOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="评分项名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入评分项名称" />
        </el-form-item>
        <el-form-item label="权重" prop="weight">
          <el-input-number
            v-model="form.weight"
            :min="0"
            :max="100"
            :step="5"
            style="width: 180px"
          />
          <span class="weight-unit">%</span>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="选择类型" style="width: 100%">
            <el-option label="平时作业" value="homework" />
            <el-option label="实验报告" value="lab" />
            <el-option label="期中考试" value="midterm" />
            <el-option label="期末考试" value="final" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="说明" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入说明"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 搜索表单
const searchForm = reactive({
  courseId: ''
})

// 课程选项
const courseOptions = ref([])
const courseLoading = ref(false)

// 表格数据
const tableData = ref([])
const tableLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 对话框
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref(null)
const form = reactive({
  courseId: '',
  name: '',
  weight: 0,
  type: '',
  description: '',
  status: 1
})

// 表单校验规则
const rules = {
  courseId: [
    { required: true, message: '请选择课程', trigger: 'change' }
  ],
  name: [
    { required: true, message: '请输入评分项名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  weight: [
    { required: true, message: '请输入权重', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择类型', trigger: 'change' }
  ]
}

// 搜索课程
const handleCourseSearch = (query) => {
  if (query) {
    courseLoading.value = true
    // 模拟远程搜索
    setTimeout(() => {
      courseOptions.value = [
        { value: 1, label: 'Java程序设计' },
        { value: 2, label: '数据结构' },
        { value: 3, label: '操作系统' }
      ].filter(item => item.label.includes(query))
      courseLoading.value = false
    }, 200)
  } else {
    courseOptions.value = []
  }
}

// 获取类型标签样式
const getTypeTag = (type) => {
  const map = {
    homework: '',
    lab: 'success',
    midterm: 'warning',
    final: 'danger',
    other: 'info'
  }
  return map[type]
}

// 获取类型文本
const getTypeText = (type) => {
  const map = {
    homework: '平时作业',
    lab: '实验报告',
    midterm: '期中考试',
    final: '期末考试',
    other: '其他'
  }
  return map[type]
}

// 搜索
const handleSearch = () => {
  fetchTableData()
}

// 重置搜索
const resetSearch = () => {
  searchForm.courseId = ''
  fetchTableData()
}

// 获取表格数据
const fetchTableData = () => {
  tableLoading.value = true
  // 模拟API调用
  setTimeout(() => {
    tableData.value = [
      {
        id: 1,
        courseId: 1,
        name: '平时作业1',
        weight: 20,
        type: 'homework',
        description: '第一次平时作业',
        status: 1
      },
      {
        id: 2,
        courseId: 1,
        name: '期中考试',
        weight: 30,
        type: 'midterm',
        description: '期中考试成绩',
        status: 1
      }
    ]
    total.value = 2
    tableLoading.value = false
  }, 500)
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchTableData()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchTableData()
}

// 添加
const handleAdd = () => {
  dialogType.value = 'add'
  form.courseId = ''
  form.name = ''
  form.weight = 0
  form.type = ''
  form.description = ''
  form.status = 1
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确定要删除该评分项吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 这里应该调用API删除数据
    ElMessage.success('删除成功')
    fetchTableData()
  })
}

// 状态切换
const handleStatusChange = (row) => {
  // 这里应该调用API更新状态
  ElMessage.success(`状态更新成功：${row.status ? '启用' : '禁用'}`)
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (valid) {
      // 这里应该调用API保存数据
      ElMessage.success(dialogType.value === 'add' ? '添加成功' : '更新成功')
      dialogVisible.value = false
      fetchTableData()
    }
  })
}

// 初始化
fetchTableData()
</script>

<style scoped>
.grade-composition-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.composition-form {
  max-width: 460px;
}

.weight-unit {
  margin-left: 10px;
  color: #909399;
}
</style>
