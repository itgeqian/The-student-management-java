<template>
  <div class="navbar">
    <div class="left-menu">
      <el-icon class="hamburger" @click="toggleSidebar">
        <Expand v-if="!isCollapse" />
        <Fold v-else />
      </el-icon>
      <breadcrumb class="breadcrumb-container" />
    </div>

    <div class="right-menu">
      <el-dropdown class="avatar-container" trigger="click">
        <div class="avatar-wrapper">
          <el-avatar :size="32">
            {{ userInitial }}
          </el-avatar>
          <el-icon><CaretBottom /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <!-- <el-dropdown-item>个人信息</el-dropdown-item> -->
            <el-dropdown-item divided @click="handleLogout">
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Fold, Expand, CaretBottom } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import Breadcrumb from './Breadcrumb.vue'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

const isCollapse = computed(() => appStore.sidebar.opened)
const userInitial = computed(() => {
  const name = userStore.userInfo?.name
  return name ? name.charAt(0) : 'U'
})

const toggleSidebar = () => {
  appStore.toggleSidebar()
}

const handleLogout = () => {
  ElMessageBox.confirm('确认退出系统吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    router.push('/login')
    ElMessage.success('退出成功')
  })
}
</script>

<style lang="scss" scoped>
.navbar {
  height: 50px;
  overflow: hidden;
  position: relative;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;

  .left-menu {
    display: flex;
    align-items: center;

    .hamburger {
      cursor: pointer;
      font-size: 20px;
      margin-right: 16px;
      
      &:hover {
        color: var(--primary-color);
      }
    }

    .breadcrumb-container {
      margin-left: 8px;
    }
  }

  .right-menu {
    display: flex;
    align-items: center;

    .avatar-container {
      cursor: pointer;
      
      .avatar-wrapper {
        display: flex;
        align-items: center;
        gap: 8px;
        
        .el-avatar {
          background: var(--el-color-primary);
          font-size: 14px;
        }

        .el-icon {
          font-size: 12px;
          color: var(--el-text-color-secondary);
        }
      }
    }
  }
}
</style>
