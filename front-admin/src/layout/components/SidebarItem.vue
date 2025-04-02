<template>
  <div v-if="!item.hidden">
    <!-- 一级菜单（有子菜单） -->
    <template v-if="item.children && item.children.length > 0">
      <el-sub-menu :index="resolvePath(item.path)" popper-append-to-body>
        <template #title>
          <el-icon v-if="item.meta?.icon">
            <component :is="item.meta.icon" />
          </el-icon>
          <span>{{ item.meta?.title }}</span>
        </template>
        <!-- 渲染子菜单 -->
        <template v-for="child in item.children" :key="child.path">
          <!-- 递归渲染多级菜单 -->
          <sidebar-item
            v-if="child.children && child.children.length > 0"
            :item="child"
            :base-path="resolvePath(item.path)"
          />
          <!-- 渲染最终的菜单项 -->
          <el-menu-item 
            v-else 
            :index="resolvePath(child.path, item.path)"
            @click="handleMenuClick(resolvePath(child.path, item.path))"
          >
            <el-icon v-if="child.meta?.icon">
              <component :is="child.meta.icon" />
            </el-icon>
            <template #title>{{ child.meta?.title }}</template>
          </el-menu-item>
        </template>
      </el-sub-menu>
    </template>

    <!-- 没有子菜单的菜单项 -->
    <template v-else>
      <el-menu-item 
        :index="resolvePath(item.path)"
        @click="handleMenuClick(resolvePath(item.path))"
      >
        <el-icon v-if="item.meta?.icon">
          <component :is="item.meta.icon" />
        </el-icon>
        <template #title>{{ item.meta?.title }}</template>
      </el-menu-item>
    </template>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  basePath: {
    type: String,
    default: ''
  }
})

// 处理菜单点击事件
const handleMenuClick = (path) => {
  console.log('Navigating to:', path)
  console.log('Route matched:', router.resolve(path).matched)
  router.push(path)
}

const resolvePath = (routePath, parentPath = '') => {
  if (!routePath) {
    return props.basePath
  }

  // 如果是完整路径（以/开头），直接返回
  if (routePath.startsWith('/')) {
    return routePath
  }

  // 处理二级菜单路径
  if (parentPath) {
    return `/${parentPath}/${routePath}`.replace(/\/+/g, '/')
  }

  // 处理一级菜单路径
  return `/${routePath}`.replace(/\/+/g, '/')
}
</script>

<style lang="scss" scoped>
.svg-icon {
  margin-right: 16px;
}

.el-menu {
  border: none;
  height: 100%;
  width: 100% !important;
}

.submenu-title-noDropdown,
.el-submenu__title {
  &:hover {
    background-color: var(--el-menu-hover-bg-color) !important;
  }
}

:deep(.el-sub-menu__title) {
  span {
    margin-left: 8px;
  }
}

:deep(.el-menu-item) {
  span {
    margin-left: 8px;
  }
}
</style>
