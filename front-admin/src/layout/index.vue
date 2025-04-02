<template>
  <div class="app-wrapper" :class="{ 'sidebar-collapsed': !sidebarOpened }">
    <sidebar class="sidebar-container" />
    <div class="main-container">
      <div class="header">
        <navbar />
      </div>
      <app-main />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Sidebar, Navbar, AppMain } from './components'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
const sidebarOpened = computed(() => appStore.sidebar.opened)
</script>

<style lang="scss" scoped>
.app-wrapper {
  height: 100%;
  width: 100%;
  display: flex;
  
  .sidebar-container {
    width: 210px;
    height: 100%;
    background-color: #304156;
    transition: width 0.3s;
    overflow: hidden;
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 1001;
  }

  .main-container {
    min-height: 100%;
    margin-left: 210px;
    position: relative;
    flex: 1;
    display: flex;
    flex-direction: column;
    transition: margin-left 0.3s;
    padding-top: 50px;
    
    .header {
      height: 50px;
      background: #fff;
      box-shadow: 0 1px 4px rgba(0,21,41,.08);
      position: fixed;
      top: 0;
      right: 0;
      left: 210px;
      z-index: 1000;
      transition: left 0.3s;
    }
  }

  &.sidebar-collapsed {
    .sidebar-container {
      width: 64px;
    }

    .main-container {
      margin-left: 64px;
    }

    .header {
      left: 64px;
    }
  }
}
</style>
