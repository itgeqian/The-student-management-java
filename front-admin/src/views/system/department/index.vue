<template>
  <div class="department-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <el-button type="primary" @click="handleAdd">新增部门</el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="departmentList"
        style="width: 100%"
        border
      >
        
        <el-table-column prop="name" label="部门名称" min-width="200" />
        <el-table-column prop="description" label="描述" min-width="300" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.limit"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          :small="true"
          background
          layout="total, sizes, prev, pager, next"
          :pager-count="5"
          prev-text="上一页"
          next-text="下一页"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        >
          <template #total>共 {{ total }} 条</template>
          <template #sizes="{ size }">
            {{ size }}条/页
          </template>
        </el-pagination>
      </div>
    </el-card>

    <!-- 新增/编辑部门对话框 -->
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
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入部门描述"
          />
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
import { 
  getDepartmentList,
  createDepartment,
  updateDepartment,
  deleteDepartment
} from '@/api/system'

// 部门列表数据
const departmentList = ref([])
const loading = ref(false)
const total = ref(0)

// 查询参数
const queryParams = ref({
  page: 1,
  limit: 10
})

// 获取部门列表
const getList = async () => {
  loading.value = true
  try {
    const { data } = await getDepartmentList(queryParams.value)
    departmentList.value = data.list || data.records || []
    total.value = data.total || departmentList.value.length
  } catch (error) {
    console.error('获取部门列表失败:', error)
    ElMessage.error('获取部门列表失败')
  } finally {
    loading.value = false
  }
}

// 表单数据
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const form = ref({
  id: undefined,
  name: '',
  description: ''
})

// 表单校验规则
const rules = {
  name: [
    { required: true, message: '请输入部门名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '描述不能超过200个字符', trigger: 'blur' }
  ]
}

// 新增部门
const handleAdd = () => {
  dialogTitle.value = '新增部门'
  dialogVisible.value = true
}

// 编辑部门
const handleEdit = (row) => {
  dialogTitle.value = '编辑部门'
  Object.assign(form.value, row)
  dialogVisible.value = true
}

// 删除部门
const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确认要删除该部门吗？删除后无法恢复！',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteDepartment(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除部门失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (form.value.id) {
          await updateDepartment(form.value)
          ElMessage.success('更新成功')
        } else {
          await createDepartment(form.value)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        getList()
      } catch (error) {
        console.error('保存部门失败:', error)
        ElMessage.error('保存失败')
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.value = {
    id: undefined,
    name: '',
    description: ''
  }
}

// 页码改变
const handleCurrentChange = (page) => {
  queryParams.value.page = page
  getList()
}

// 每页条数改变
const handleSizeChange = (size) => {
  queryParams.value.limit = size
  queryParams.value.page = 1
  getList()
}

// 初始化
getList()
</script>

<style lang="scss" scoped>
.department-container {
  padding: 20px;

  .box-card {
    margin-bottom: 20px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  :deep(.el-input),
  :deep(.el-textarea) {
    width: 100%;
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .dialog-footer {
    text-align: right;
    margin-top: 20px;
  }
}
</style>
