<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleAdd">新增用户</el-button>
        </div>
      </template>
      
      <div class="search-container">
        <el-form :inline="true" :model="queryParams" class="search-form">
          <el-form-item label="用户名">
            <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable style="width: 180px;" />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="queryParams.realName" placeholder="请输入姓名" clearable style="width: 180px;" />
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="queryParams.role" placeholder="请选择角色" clearable style="width: 180px;">
              <el-option label="管理员" value="ADMIN" />
              <el-option label="教师" value="TEACHER" />
              <el-option label="学生" value="STUDENT" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="table-container">
        <el-table
          v-loading="loading"
          :data="userList"
          border
          style="width: 100%"
        >
          <el-table-column type="index" label="序号" />
          <el-table-column prop="realName" label="姓名" show-overflow-tooltip />
          <el-table-column prop="role" label="角色" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.role?.toUpperCase() === 'ADMIN'" type="danger">管理员</el-tag>
              <el-tag v-else-if="row.role?.toUpperCase() === 'TEACHER'" type="success">教师</el-tag>
              <el-tag v-else-if="row.role?.toUpperCase() === 'STUDENT'" type="warning">学生</el-tag>
              <span v-else>{{ row.role }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="email" label="邮箱" show-overflow-tooltip />
          <el-table-column prop="phone" label="手机号" show-overflow-tooltip />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
              <el-button type="primary" link @click="handleResetPwd(row)">重置密码</el-button>
              <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-container">
          <el-pagination
            v-model:current-page="queryParams.current"
            v-model:page-size="queryParams.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            :small="true"
            background
            layout="total, sizes, prev, pager, next"
            :pager-count="5"
            @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          >
            <template #total>共 {{ total }} 条</template>
            <template #sizes="{ size }">
              {{ size }}条/页
            </template>
          </el-pagination>
        </div>
      </div>
    </el-card>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog
      :title="dialogType === 'add' ? '添加用户' : '编辑用户'"
      v-model="dialogVisible"
      width="500px"
      append-to-body
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="userForm.realName" placeholder="请输入姓名" style="width: 180px;" />
        </el-form-item>
        <el-form-item label="账号" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入账号" style="width: 180px;" />
        </el-form-item>
        <el-form-item v-if="dialogType === 'add'" label="密码" prop="password">
          <el-input v-model="userForm.password" placeholder="请输入密码" type="password" show-password style="width: 180px;" />
        </el-form-item>
        <el-form-item v-if="dialogType === 'add'" label="确认密码" prop="confirmPassword">
          <el-input v-model="userForm.confirmPassword" placeholder="请再次输入密码" type="password" show-password style="width: 180px;" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="userForm.role" placeholder="请选择角色" disabled style="width: 180px;">
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" style="width: 180px;" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" style="width: 180px;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitForm">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, createUser, updateUser, deleteUser, resetUserPassword } from '@/api/system'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('add')
const userList = ref([])
const total = ref(0)
const userFormRef = ref(null)

const queryParams = reactive({
  current: 1,
  size: 10,
  username: '',
  realName: '',
  role: ''
})

const userForm = reactive({
  id: '',
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  role: 'ADMIN', // 默认设置为管理员
  email: '',
  phone: ''
})

const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (userForm.confirmPassword !== '') {
      userFormRef.value?.validateField('confirmPassword')
    }
    callback()
  }
}

const validateConfirmPass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== userForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePass, trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPass, trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

// 获取用户列表
const getList = async () => {
  loading.value = true
  try {
    console.log('查询参数:', queryParams)
    const params = { ...queryParams }
    if (!params.role) {
      delete params.role
    }
    const { data } = await getUserList(params)
    console.log('返回数据:', data)
    const list = Array.isArray(data) ? data : (data.records || data.list || [])
    userList.value = list
    total.value = data.total || list.length
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
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
  queryParams.username = ''
  queryParams.realName = ''
  queryParams.role = ''
  handleQuery()
}

// 新增按钮点击
const handleAdd = () => {
  dialogType.value = 'add'
  userForm.id = ''
  userForm.username = ''
  userForm.password = ''
  userForm.confirmPassword = ''
  userForm.realName = ''
  userForm.role = 'ADMIN' // 默认设置为管理员
  userForm.email = ''
  userForm.phone = ''
  dialogVisible.value = true
}

// 编辑按钮点击
const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.assign(userForm, row)
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  if (!userFormRef.value) return
  
  await userFormRef.value.validate()
  
  try {
    const submitData = { ...userForm }
    delete submitData.confirmPassword // 删除确认密码字段，不需要提交给后端
    
    if (dialogType.value === 'add') {
      await createUser(submitData)
      ElMessage.success('添加成功')
    } else {
      delete submitData.password // 编辑时不提交密码字段
      await updateUser(submitData)
      ElMessage.success('修改成功')
    }
    
    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error(dialogType.value === 'add' ? '添加失败:' : '修改失败:', error)
    ElMessage.error(dialogType.value === 'add' ? '添加失败' : '修改失败')
  }
}

// 删除按钮点击
const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该用户吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  })
}

// 重置密码
const handleResetPwd = (row) => {
  ElMessageBox.confirm('确认重置该用户的密码吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await resetUserPassword(row.id)
      ElMessage.success('密码重置成功')
    } catch (error) {
      console.error('密码重置失败:', error)
      ElMessage.error('密码重置失败')
    }
  })
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
getList()
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-container {
  margin-bottom: 20px;
  
  .search-form {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    padding: 20px;
    background-color: #fff;
    border-radius: 4px;
    margin-bottom: 20px;

    :deep(.el-form-item) {
      margin-bottom: 0;
      margin-right: 20px;
    }

    :deep(.el-select) {
      width: 180px;
    }

    :deep(.el-input) {
      width: 180px;
    }
  }
}

.table-container {
  margin-top: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;

  :deep(.el-pagination) {
    .el-pagination__total {
      font-size: 14px;
    }
    .el-pagination__sizes {
      margin-left: 15px;
    }
    button {
      &:disabled {
        background-color: #f4f4f5;
      }
    }
  }
}

.dialog-footer {
  text-align: right;
}
</style>