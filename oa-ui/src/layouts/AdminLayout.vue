<template>
  <div class="admin-shell" :class="{ 'admin-shell--mobile-menu': mobileMenuOpen }">
    <div v-if="mobileMenuOpen" class="admin-shell__backdrop" @click="closeMobileMenu" />

    <aside
      class="admin-sidebar"
      :class="{
        'admin-sidebar--collapsed': isCollapsed,
        'admin-sidebar--mobile-open': mobileMenuOpen,
      }"
    >
      <div class="admin-sidebar__brand">
        <div class="admin-sidebar__logo">OA</div>
        <div v-show="!isCollapsed" class="admin-sidebar__brand-copy">
          <strong>OA Admin</strong>
          <span>审批与流程中心</span>
        </div>
      </div>

      <div v-show="!isCollapsed" class="admin-sidebar__section-label">工作台</div>
      <el-menu
        :default-active="route.path"
        :collapse="isCollapsed"
        :collapse-transition="false"
        router
        class="admin-menu"
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
    </aside>

    <div class="admin-main">
      <header class="admin-header">
        <div class="admin-header__left">
          <el-button class="admin-header__menu-btn" text @click="toggleMenu">
            <el-icon size="18">
              <Expand v-if="isMobile" />
              <Fold v-else-if="!isCollapsed" />
              <Expand v-else />
            </el-icon>
          </el-button>
          <div class="admin-header__titles">
            <div class="admin-header__eyebrow">OA Workflow Console</div>
            <div class="admin-header__title">{{ currentTitle }}</div>
          </div>
        </div>

        <div class="admin-header__right">
          <div v-if="!isMobile" class="admin-header__status">
            <span class="admin-header__status-dot" />
            审批流程在线
          </div>
          <el-dropdown @command="handleCommand">
            <button class="admin-user" type="button">
              <span class="admin-user__avatar">{{ userInitial }}</span>
              <span class="admin-user__meta">
                <strong>{{ userDisplayName }}</strong>
                <small>{{ route.meta.title || '工作台' }}</small>
              </span>
              <el-icon><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  Odometer, Setting, Document, Calendar, Fold, Expand, ArrowDown,
} from '@element-plus/icons-vue'

const route = useRoute()
const userStore = useUserStore()
const isCollapsed = ref(false)
const isMobile = ref(false)
const mobileMenuOpen = ref(false)

const currentTitle = computed(() => String(route.meta.title || '工作台'))
const userDisplayName = computed(() => {
  const user = userStore.userInfo
  return String(user?.nickname || user?.realName || user?.name || user?.username || '用户')
})

const userInitial = computed(() => {
  return userDisplayName.value.slice(0, 1).toUpperCase()
})

function syncViewport() {
  if (typeof window === 'undefined') return
  const mobile = window.innerWidth < 992
  isMobile.value = mobile
  if (mobile) {
    isCollapsed.value = false
  } else {
    mobileMenuOpen.value = false
  }
}

function handleCommand(cmd: string) {
  if (cmd === 'logout') {
    userStore.logout()
  }
}

function closeMobileMenu() {
  mobileMenuOpen.value = false
}

function toggleMenu() {
  if (isMobile.value) {
    mobileMenuOpen.value = !mobileMenuOpen.value
    return
  }
  isCollapsed.value = !isCollapsed.value
}

onMounted(() => {
  syncViewport()
  window.addEventListener('resize', syncViewport)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', syncViewport)
})
</script>

<style scoped lang="scss">
.admin-shell {
  display: flex;
  min-height: 100vh;
  background: transparent;

  &__backdrop {
    position: fixed;
    inset: 0;
    z-index: 19;
    background: rgba(16, 20, 26, 0.42);
    backdrop-filter: blur(3px);
  }
}

.admin-sidebar {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  flex-direction: column;
  width: 268px;
  height: 100vh;
  padding: 22px 14px 18px;
  border-right: 1px solid var(--app-sidebar-border);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.04) 0%, rgba(255, 255, 255, 0) 120px),
    var(--app-sidebar-bg);
  color: var(--app-sidebar-text);
  transition: width 0.24s ease, transform 0.24s ease;

  &--collapsed {
    width: 88px;
  }

  &__brand {
    display: flex;
    align-items: center;
    gap: 14px;
    min-height: 68px;
    margin-bottom: 12px;
    padding: 10px 12px;
    border: 1px solid var(--app-sidebar-border);
    border-radius: 20px;
    background: var(--app-sidebar-surface);
  }

  &__logo {
    display: grid;
    place-items: center;
    width: 42px;
    height: 42px;
    border-radius: 14px;
    background: linear-gradient(135deg, rgba(183, 130, 76, 0.92), rgba(227, 205, 175, 0.88));
    color: #1f2d38;
    font-size: 15px;
    font-weight: 700;
    flex-shrink: 0;
  }

  &__brand-copy {
    display: flex;
    flex-direction: column;
    gap: 4px;
    min-width: 0;

    strong {
      color: #f7f4ee;
      font-size: 16px;
      line-height: 1.2;
    }

    span {
      color: rgba(223, 232, 240, 0.68);
      font-size: 12px;
      line-height: 1.4;
    }
  }

  &__section-label {
    margin: 10px 10px 10px 14px;
    color: rgba(223, 232, 240, 0.42);
    font-size: 11px;
    font-weight: 700;
    letter-spacing: 0.08em;
    text-transform: uppercase;
  }
}

.admin-menu {
  border-right: none;
  background: transparent;

  :deep(.el-menu) {
    border-right: none;
    background: transparent;
  }

  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    height: 46px;
    margin-bottom: 6px;
    border-radius: 14px;
    color: var(--app-sidebar-text);
  }

  :deep(.el-sub-menu__title:hover),
  :deep(.el-menu-item:hover) {
    background: rgba(255, 255, 255, 0.07);
    color: var(--app-sidebar-text-active);
  }

  :deep(.el-menu-item.is-active) {
    background: linear-gradient(90deg, rgba(47, 107, 98, 0.9), rgba(39, 81, 75, 0.94));
    color: #f5f4ef;
    box-shadow: 0 14px 26px rgba(19, 35, 34, 0.24);
  }

  :deep(.el-sub-menu .el-menu-item) {
    min-width: auto;
    padding-left: 52px !important;
  }

  :deep(.el-menu-item [class*='el-icon']),
  :deep(.el-sub-menu__title [class*='el-icon']) {
    font-size: 18px;
  }
}

.admin-main {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
}

.admin-header {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-height: var(--app-header-height);
  padding: 16px var(--app-shell-padding);
  background: rgba(244, 240, 232, 0.76);
  border-bottom: 1px solid rgba(63, 48, 28, 0.08);
  backdrop-filter: blur(18px);

  &__left,
  &__right {
    display: flex;
    align-items: center;
    gap: 14px;
    min-width: 0;
  }

  &__menu-btn {
    width: 42px;
    height: 42px;
    border: 1px solid rgba(63, 48, 28, 0.1);
    border-radius: 12px;
    background: rgba(255, 253, 249, 0.7);
    color: var(--app-text-primary);
  }

  &__titles {
    display: flex;
    flex-direction: column;
    gap: 4px;
    min-width: 0;
  }

  &__eyebrow {
    color: var(--app-accent);
    font-size: 11px;
    font-weight: 700;
    letter-spacing: 0.1em;
    text-transform: uppercase;
  }

  &__title {
    color: var(--app-text-primary);
    font-size: 20px;
    font-weight: 650;
    line-height: 1.2;
  }

  &__status {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    height: 42px;
    padding: 0 14px;
    border: 1px solid rgba(47, 107, 98, 0.16);
    border-radius: 999px;
    background: rgba(47, 107, 98, 0.08);
    color: var(--app-text-secondary);
    font-size: 13px;
    white-space: nowrap;
  }

  &__status-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: var(--el-color-success);
    box-shadow: 0 0 0 5px rgba(68, 120, 84, 0.15);
  }
}

.admin-user {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  height: 46px;
  padding: 0 14px 0 10px;
  border: 1px solid rgba(63, 48, 28, 0.1);
  border-radius: 16px;
  background: rgba(255, 253, 249, 0.78);
  color: var(--app-text-primary);
  cursor: pointer;

  &__avatar {
    display: grid;
    place-items: center;
    width: 30px;
    height: 30px;
    border-radius: 10px;
    background: linear-gradient(135deg, rgba(47, 107, 98, 0.94), rgba(183, 130, 76, 0.86));
    color: #fff;
    font-size: 13px;
    font-weight: 700;
    flex-shrink: 0;
  }

  &__meta {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    min-width: 0;

    strong,
    small {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    strong {
      font-size: 13px;
      line-height: 1.2;
    }

    small {
      color: var(--app-text-tertiary);
      font-size: 11px;
      line-height: 1.2;
    }
  }
}

.admin-content {
  flex: 1;
  min-width: 0;
  padding: var(--app-shell-padding);
}

@media (max-width: 991px) {
  .admin-sidebar {
    position: fixed;
    left: 0;
    transform: translateX(-100%);
    width: min(300px, calc(100vw - 32px));
    box-shadow: 0 28px 54px rgba(13, 20, 29, 0.24);

    &--mobile-open {
      transform: translateX(0);
    }

    &--collapsed {
      width: min(300px, calc(100vw - 32px));
    }
  }

  .admin-header {
    &__status {
      display: none;
    }
  }
}

@media (max-width: 768px) {
  .admin-header {
    padding-inline: 16px;

    &__title {
      font-size: 18px;
    }
  }

  .admin-user {
    padding-right: 10px;

    &__meta {
      display: none;
    }
  }

  .admin-content {
    padding: 16px;
  }
}
</style>
