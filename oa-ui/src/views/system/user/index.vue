<template>
  <div>
    <el-card>
      <div class="toolbar">
        <div class="search">
          <el-input
            v-model="search.username"
            placeholder="用户名"
            clearable
            style="width: 200px"
            @clear="loadData"
          />
          <el-select
            v-model="search.status"
            placeholder="状态"
            clearable
            style="width: 120px; margin-left: 10px"
            @change="loadData"
          >
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
          <el-button type="primary" style="margin-left: 10px" @click="loadData">
            搜索
          </el-button>
        </div>
        <el-button type="primary" @click="openDialog()">新增用户</el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确认删除?" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        style="margin-top: 15px; justify-content: flex-end"
        @current-change="loadData"
      />
    </el-card>
    <el-dialog
      v-model="dialogVisible"
      :title="editingUser ? '编辑用户' : '新增用户'"
      width="500px"
      destroy-on-close
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" :disabled="!!editingUser" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="密码" :required="!editingUser">
          <el-input
            v-model="form.password"
            type="password"
            :placeholder="editingUser ? '留空不修改' : '请输入密码'"
          />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserList, createUser, updateUser, deleteUser } from '@/api/user'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const search = reactive({
  username: '',
  status: undefined as number | undefined,
})
const dialogVisible = ref(false)
const editingUser = ref<any>(null)
const form = reactive({
  username: '',
  nickname: '',
  password: '',
  email: '',
  phone: '',
  status: 1,
})

async function loadData() {
  loading.value = true
  try {
    const data: any = await getUserList({
      username: search.username || undefined,
      status: search.status,
      page: page.value,
      pageSize: pageSize.value,
    })
    tableData.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function openDialog(user?: any) {
  editingUser.value = user || null
  if (user) {
    Object.assign(form, {
      username: user.username,
      nickname: user.nickname,
      password: '',
      email: user.email || '',
      phone: user.phone || '',
      status: user.status,
    })
  } else {
    Object.assign(form, {
      username: '',
      nickname: '',
      password: '',
      email: '',
      phone: '',
      status: 1,
    })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    const payload: any = { ...form }
    if (!payload.password) delete payload.password
    if (editingUser.value) {
      await updateUser(editingUser.value.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createUser(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {
    // interceptor handles error
  }
}

async function handleDelete(id: number) {
  try {
    await deleteUser(id)
    ElMessage.success('删除成功')
    loadData()
  } catch {
    // interceptor handles error
  }
}

onMounted(loadData)
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; margin-bottom: 15px; }
.search { display: flex; align-items: center; }
</style>
