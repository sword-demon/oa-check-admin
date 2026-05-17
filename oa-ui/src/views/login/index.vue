<template>
  <div class="login-container">
    <section class="login-panel">
      <div class="login-panel__hero">
        <p class="login-panel__eyebrow">Workflow Control</p>
        <h1>OA 审批管理平台</h1>
        <p class="login-panel__summary">
          统一处理审批模板、实例流转与请假申请，保持后台工作台的清晰密度和稳定节奏。
        </p>
        <div class="login-panel__chips">
          <span>审批模板</span>
          <span>流程追踪</span>
          <span>请假管理</span>
        </div>
      </div>

      <div class="login-card">
        <div class="login-card__header">
          <h2>账号登录</h2>
          <p>输入账号信息进入工作台</p>
        </div>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" size="large" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" size="large" show-password @keyup.enter="handleLogin" />
          </el-form-item>
          <el-form-item>
            <el-button class="login-card__submit" type="primary" size="large" :loading="loading" @click="handleLogin">登录</el-button>
          </el-form-item>
        </el-form>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  await formRef.value?.validate()
  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
  background:
    radial-gradient(circle at top left, rgba(183, 130, 76, 0.22), transparent 28%),
    radial-gradient(circle at bottom right, rgba(47, 107, 98, 0.18), transparent 30%),
    linear-gradient(135deg, #efe7db 0%, #f5f7f8 44%, #eef4f3 100%);
}

.login-panel {
  display: grid;
  grid-template-columns: minmax(320px, 1.15fr) minmax(320px, 420px);
  width: min(1040px, 100%);
  border: 1px solid rgba(63, 48, 28, 0.1);
  border-radius: 30px;
  overflow: hidden;
  background: rgba(255, 252, 247, 0.8);
  box-shadow: 0 30px 60px rgba(37, 29, 18, 0.14);
  backdrop-filter: blur(16px);
}

.login-panel__hero {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 18px;
  min-height: 540px;
  padding: 56px 52px;
  background:
    linear-gradient(160deg, rgba(24, 35, 45, 0.98), rgba(28, 49, 62, 0.94)),
    var(--app-sidebar-bg);
  color: #f5f4ef;
}

.login-panel__hero h1 {
  margin: 0;
  font-size: clamp(34px, 4vw, 48px);
  line-height: 1.08;
  font-weight: 700;
}

.login-panel__eyebrow {
  margin: 0;
  color: rgba(231, 208, 176, 0.9);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.login-panel__summary {
  max-width: 34ch;
  margin: 0;
  color: rgba(223, 232, 240, 0.74);
  line-height: 1.75;
}

.login-panel__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 6px;
}

.login-panel__chips span {
  display: inline-flex;
  align-items: center;
  min-height: 36px;
  padding: 0 14px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.06);
  color: #e5ecf2;
  font-size: 13px;
}

.login-card {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 52px 44px;
}

.login-card__header {
  margin-bottom: 28px;
}

.login-card__header h2 {
  margin: 0 0 10px;
  color: var(--app-text-primary);
  font-size: 28px;
  line-height: 1.15;
}

.login-card__header p {
  margin: 0;
  color: var(--app-text-secondary);
  font-size: 14px;
}

.login-card__submit {
  width: 100%;
  margin-top: 8px;
}

@media (max-width: 900px) {
  .login-panel {
    grid-template-columns: 1fr;
  }

  .login-panel__hero {
    min-height: 0;
    padding: 40px 32px 30px;
  }

  .login-card {
    padding: 32px;
  }
}

@media (max-width: 640px) {
  .login-container {
    padding: 16px;
  }

  .login-panel {
    border-radius: 24px;
  }

  .login-panel__hero,
  .login-card {
    padding-inline: 22px;
  }
}
</style>
