<template>
  <el-container style="height: 100vh">
    <el-aside :width="isCollapsed ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <span v-show="!isCollapsed">OA Admin</span>
        <span v-show="isCollapsed">OA</span>
      </div>
      <el-menu
        :default-active="route.path"
        :collapse="isCollapsed"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>仪表盘</template>
        </el-menu-item>
        <el-sub-menu index="system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/user">用户管理</el-menu-item>
          <el-menu-item index="/system/role">角色管理</el-menu-item>
          <el-menu-item index="/system/permission">权限管理</el-menu-item>
          <el-menu-item index="/system/dept">部门管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="approval">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>审批管理</span>
          </template>
          <el-menu-item index="/approval/template">审批模板</el-menu-item>
          <el-menu-item index="/approval/my-apply">我的申请</el-menu-item>
          <el-menu-item index="/approval/my-todo">我的待办</el-menu-item>
          <el-menu-item index="/approval/my-done">我的已办</el-menu-item>
          <el-menu-item index="/approval/cc">抄送给我的</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/leave">
          <el-icon><Calendar /></el-icon>
          <template #title>请假管理</template>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="top-nav">
        <el-icon class="collapse-btn" @click="isCollapsed = !isCollapsed">
          <Fold v-if="!isCollapsed" />
          <Expand v-else />
        </el-icon>
        <div class="right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              {{ userStore.userInfo?.nickname || '用户' }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  Odometer, Setting, Document, Calendar, Fold, Expand, ArrowDown,
} from '@element-plus/icons-vue'

const route = useRoute()
const userStore = useUserStore()
const isCollapsed = ref(false)

function handleCommand(cmd: string) {
  if (cmd === 'logout') {
    userStore.logout()
  }
}
</script>

<style scoped>
.sidebar {
  background-color: #304156;
  transition: width 0.3s;
  overflow: hidden;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
}
.top-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e6e6e6;
  background: #fff;
}
.collapse-btn {
  cursor: pointer;
  font-size: 20px;
}
.right {
  display: flex;
  align-items: center;
}
.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
