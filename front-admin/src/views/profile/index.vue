<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 用户信息卡片 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
            </div>
          </template>
          <div class="text-center">
            <el-avatar :size="100" :src="userInfo.avatar || '/default-avatar.png'" />
            <h3 class="mt-2">{{ userInfo.name }}</h3>
            <div :class="['role-tag', `role-${userInfo.role.toLowerCase()}`]">
              {{ formatRole(userInfo.role) }}
            </div>
          </div>
          <el-descriptions class="mt-4" :column="1" border>
            <el-descriptions-item label="用户名">
              {{ userInfo.username }}
            </el-descriptions-item>
            <el-descriptions-item label="姓名">
              {{ userInfo.name }}
            </el-descriptions-item>
            <el-descriptions-item label="工号/学号">
              {{ userInfo.code }}
            </el-descriptions-item>
            <el-descriptions-item label="手机号">
              {{ userInfo.phone }}
            </el-descriptions-item>
            <el-descriptions-item label="邮箱">
              {{ userInfo.email }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <!-- 编辑表单 -->
      <el-col :span="16">
        <!-- <el-card>
          <template #header>
            <div class="card-header">
              <span>修改信息</span>
            </div>
          </template>
          
          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="100px"
            v-loading="loading"
          >
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" disabled />
            </el-form-item>
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSubmit" :loading="submitting">保存修改</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card> -->

        <!-- 修改密码卡片 -->
        <el-card class="mt-4">
          <template #header>
            <div class="card-header">
              <span>修改密码</span>
            </div>
          </template>
          
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword" :loading="changingPassword">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { updateUserInfo, updatePassword } from '@/api/user'

const userStore = useUserStore()
const formRef = ref(null)
const passwordFormRef = ref(null)
const loading = ref(false)
const submitting = ref(false)
const changingPassword = ref(false)

// 用户信息
const userInfo = reactive({
  username: '',
  name: '',
  email: '',
  phone: '',
  avatar: '',
  role: '',
  code: '',
  department: '',
  major: '',
  className: ''
})

// 编辑表单
const form = reactive({
  username: '',
  name: '',
  email: '',
  phone: '',
  avatar: ''
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 密码表单验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 格式化角色显示
const formatRole = (role) => {
  const roleMap = {
    ADMIN: '管理员',
    TEACHER: '教师',
    STUDENT: '学生'
  }
  return roleMap[role] || role
}

// 初始化用户信息
const initUserInfo = () => {
  const info = userStore.userInfo
  if (info) {
    if(info.role === 'STUDENT') {
      userInfo.code = info.studentInfo.studentNo
      userInfo.name = info.studentInfo.name
      userInfo.phone = info.studentInfo.phone
      userInfo.email = info.studentInfo.email
    }
    if(info.role === 'TEACHER') {
      userInfo.code = info.teacherInfo.teacherNo
      userInfo.name = info.teacherInfo.name
      userInfo.phone = info.teacherInfo.phone
      userInfo.email = info.teacherInfo.email
    }
    Object.assign(userInfo, info)
    Object.assign(form, {
      username: info.username,
      name: info.name,
      email: info.email,
      phone: info.phone,
      avatar: info.avatar
    })
  }
}

// 获取最新的用户信息
const refreshUserInfo = async () => {
  loading.value = true
  try {
    await userStore.getUserInfo()
    initUserInfo()
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

// 头像上传前的验证
const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('头像只能是 JPG 或 PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }
  return true
}

// 头像上传成功的回调
const handleAvatarSuccess = (response) => {
  form.avatar = response.data.url
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    await updateUserInfo({
      name: form.name,
      email: form.email,
      phone: form.phone,
      avatar: form.avatar
    })
    
    await refreshUserInfo()
    ElMessage.success('保存成功')
  } catch (error) {
    if (error === 'cancel') return
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
    initUserInfo()
  }
}

// 修改密码
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()
    changingPassword.value = true

    await updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })

    ElMessage.success('密码修改成功')
    passwordFormRef.value.resetFields()
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error('修改密码失败')
  } finally {
    changingPassword.value = false
  }
}

onMounted(() => {
  refreshUserInfo()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.text-center {
  text-align: center;
}

.role-tag {
  display: inline-block;
  padding: 6px 16px;
  border-radius: 15px;
  font-size: 14px;
  font-weight: 500;
  margin-top: 8px;
  transition: all 0.3s ease;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.role-teacher {
  background: linear-gradient(45deg, #409eff, #36a3ff);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.role-student {
  background: linear-gradient(45deg, #67c23a, #85ce61);
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.3);
}

.role-admin {
  background: linear-gradient(45deg, #f56c6c, #ff7875);
  box-shadow: 0 2px 8px rgba(245, 108, 108, 0.3);
}

.role-tag:hover {
  transform: translateY(-2px);
  filter: brightness(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.role-teacher:hover {
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

.role-student:hover {
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.4);
}

.role-admin:hover {
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.4);
}

.mt-2 {
  margin-top: 8px;
}

.mt-4 {
  margin-top: 16px;
}

.avatar-uploader {
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 178px;
  height: 178px;
  display: inline-block;
}

.avatar-uploader:hover {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
}
</style>
