<template>
  <div class="login-container">
    <div class="login-content">
      <div class="login-left">
        <div class="welcome-text">
          <h1>欢迎使用</h1>
          <h2>教务管理系统</h2>
          <p>高效、便捷的教学管理平台</p>
        </div>
        <div class="decoration-image" />
      </div>
      <div class="login-right">
        <div class="login-box">
          <h3 class="login-title">登录</h3>
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            size="large"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                :prefix-icon="User"
                clearable
                @keyup.enter="handleLogin"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                :type="passwordVisible ? 'text' : 'password'"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                clearable
                @keyup.enter="handleLogin"
              >
                <template #suffix>
                  <el-icon
                    class="cursor-pointer"
                    @click="passwordVisible = !passwordVisible"
                  >
                    <View v-if="passwordVisible" />
                    <Hide v-else />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item>
              <el-button
                :loading="loading"
                type="primary"
                class="login-button"
                @click="handleLogin"
              >
                {{ loading ? '登录中...' : '登录' }}
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock, View, Hide } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)
const passwordVisible = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [{ required: true, trigger: 'blur', message: '请输入用户名' }],
  password: [{ required: true, trigger: 'blur', message: '请输入密码' }]
}

const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    await userStore.userLogin(loginForm)
    
    // 登录成功后直接跳转到首页
    router.push({ path: '/' })
    ElMessage.success('登录成功')
  } catch (error) {
    console.error('Login failed:', error)
    ElMessage.error(error.message || '登录失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7eb 100%);
  padding: 20px;
}

.login-content {
  width: 1000px;
  height: 600px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  display: flex;
  overflow: hidden;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  color: #fff;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    width: 300px;
    height: 300px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 50%;
    top: -100px;
    right: -100px;
  }

  &::after {
    content: '';
    position: absolute;
    width: 200px;
    height: 200px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 50%;
    bottom: -50px;
    left: -50px;
  }
}

.welcome-text {
  position: relative;
  z-index: 1;

  h1 {
    font-size: 36px;
    margin: 0 0 10px;
    font-weight: 600;
  }

  h2 {
    font-size: 28px;
    margin: 0 0 20px;
    font-weight: 500;
  }

  p {
    font-size: 16px;
    opacity: 0.9;
    margin: 0;
  }
}

.decoration-image {
  flex: 1;
  background: url('@/assets/images/login-decoration.svg') no-repeat center;
  background-size: contain;
  margin-top: 40px;
}

.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-box {
  width: 100%;
  max-width: 360px;
}

.login-title {
  font-size: 28px;
  color: #1f2937;
  margin: 0 0 40px;
  text-align: center;
  font-weight: 600;
}

.login-form {
  .el-input {
    --el-input-hover-border-color: #1890ff;
    --el-input-focus-border-color: #1890ff;
    height: 45px;
    
    :deep(.el-input__wrapper) {
      box-shadow: 0 0 0 1px #e5e7eb;
      
      &.is-focus {
        box-shadow: 0 0 0 1px #1890ff;
      }
    }
  }

  .el-button {
    height: 45px;
  }
}

.login-button {
  width: 100%;
  font-size: 16px;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  border: none;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
  }

  &:active {
    transform: translateY(0);
  }
}

.cursor-pointer {
  cursor: pointer;
}

@media (max-width: 768px) {
  .login-content {
    width: 100%;
    height: auto;
    flex-direction: column;
  }

  .login-left {
    padding: 30px;
    text-align: center;

    .welcome-text {
      h1 {
        font-size: 28px;
      }

      h2 {
        font-size: 24px;
      }
    }

    .decoration-image {
      display: none;
    }
  }

  .login-right {
    padding: 30px;
  }

  .login-box {
    max-width: 100%;
  }
}
</style>
