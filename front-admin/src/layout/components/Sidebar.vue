<template>
  <div class="sidebar-container">
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu
        :default-active="activeMenu"
        :collapse="!isCollapse"
        :background-color="variables.menuBg"
        :text-color="variables.menuText"
        :active-text-color="variables.menuActiveText"
        :unique-opened="true"
        :collapse-transition="false"
        :router="true"
        mode="vertical"
      >
        <sidebar-item
          v-for="menu in menus"
          :key="menu.path"
          :item="menu"
          :base-path="menu.path"
        />
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { getMenusByRole } from '@/router/menu'
import SidebarItem from './SidebarItem.vue'

// 变量定义
const variables = {
  menuText: '#bfcbd9',
  menuActiveText: '#409eff',
  menuBg: '#304156'
}

const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()

const isCollapse = computed(() => appStore.sidebar.opened)

// 根据用户角色获取菜单
const menus = computed(() => {
  const role = userStore.roles?.[0]
  return getMenusByRole(role)
})

// 当前激活的菜单
const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta?.activeMenu) {
    return meta.activeMenu
  }
  return path
})
</script>

<style lang="scss" scoped>
.sidebar-container {
  transition: width 0.28s;
  width: 210px;
  height: 100%;
  position: fixed;
  top: 50px;
  bottom: 0;
  left: 0;
  z-index: 1001;
  overflow: hidden;
  background-color: v-bind('variables.menuBg');

  .scrollbar-wrapper {
    overflow-x: hidden !important;
  }

  .el-scrollbar__bar.is-vertical {
    right: 0px;
  }

  .el-scrollbar {
    height: 100%;
  }

  &.has-logo {
    .el-scrollbar {
      height: calc(100% - 50px);
    }
  }

  .is-horizontal {
    display: none;
  }

  a {
    display: inline-block;
    width: 100%;
    overflow: hidden;
  }

  .el-menu {
    border: none;
    height: 100%;
    width: 100% !important;
  }

  :deep(.el-menu--collapse) {
    .el-sub-menu__title {
      span {
        display: none;
      }
      .el-sub-menu__icon-arrow {
        display: none;
      }
    }

    .el-menu-item {
      span {
        display: none;
      }
    }
  }
}
</style>
