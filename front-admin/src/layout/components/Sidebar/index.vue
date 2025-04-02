<template>
  <div class="sidebar">
    <el-menu
      :default-active="activeMenu"
      :collapse="isCollapse"
      :unique-opened="true"
      :collapse-transition="false"
      mode="vertical"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409EFF"
    >
      <sidebar-item
        v-for="route in routes"
        :key="route.path"
        :item="route"
        :base-path="route.path"
      />
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { usePermissionStore } from '@/stores/permission'
import { useAppStore } from '@/stores/app' 
import SidebarItem from './SidebarItem.vue'

const route = useRoute()
const permissionStore = usePermissionStore()
const appStore = useAppStore() 

const routes = computed(() => {
  return permissionStore.routes
})

const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})

// true 表示折叠，false 表示展开
const isCollapse = computed(() => !appStore.sidebar.opened)

</script>

<style lang="scss" scoped>
.sidebar {
  height: 100%;
  .el-menu {
    height: 100%;
    border: none;
    width: 100%;
  }
}

:deep(.el-menu--collapse) {
  .el-sub-menu {
    .el-sub-menu__title {
      span {
        display: none;
      }
    }
  }

  .el-menu-item {
    span {
      display: none;
    }
  }
}

:deep(.el-menu-item) {
  &.is-active {
    background-color: #263445 !important;
  }
}
</style>
