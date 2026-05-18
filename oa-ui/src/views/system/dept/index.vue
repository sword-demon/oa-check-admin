<template>
  <div class="page-shell page-shell--dense">
    <el-card class="page-panel">
      <div class="toolbar">
        <div class="search">
          <el-input
            v-model="search.deptName"
            placeholder="部门名称"
            clearable
            style="width: 200px"
            @clear="loadData"
            @keyup.enter="loadData"
          />
          <el-select
            v-model="search.status"
            placeholder="状态"
            clearable
            style="width: 120px; margin-left: 10px"
            @change="loadData"
          >
            <el-option label="正常" :value="CommonStatus.ACTIVE" />
            <el-option label="禁用" :value="CommonStatus.DISABLED" />
          </el-select>
          <el-button type="primary" style="margin-left: 10px" @click="loadData">搜索</el-button>
        </div>
        <el-button type="primary" @click="openDialog()">新增部门</el-button>
      </div>
      <el-table
        :data="treeData"
        row-key="id"
        :tree-props="{ children: 'children' }"
        v-loading="loading"
        class="page-table"
        default-expand-all
      >
        <el-table-column prop="deptName" label="部门名称" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="负责人" min-width="140">
          <template #default="{ row }">
            {{ leaderLabel(row.leaderUserId) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag
              :type="row.status === CommonStatus.ACTIVE ? 'success' : 'danger'"
              size="small"
            >
              {{ row.status === CommonStatus.ACTIVE ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button
              link
              type="success"
              @click="openDialog(undefined, row.id)"
            >
              新增子部门
            </el-button>
            <el-popconfirm title="确认删除?" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="editingDept ? '编辑部门' : '新增部门'"
      width="500px"
      destroy-on-close
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="部门名称" required>
          <el-input v-model="form.deptName" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-select
            v-model="form.leaderUserId"
            placeholder="选择负责人"
            clearable
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="userLabel(user)"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="正常" :value="CommonStatus.ACTIVE" />
            <el-option label="禁用" :value="CommonStatus.DISABLED" />
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
import { getDeptTree, createDept, updateDept, deleteDept } from '@/api/system'
import { getUserList } from '@/api/user'
import { CommonStatus, type SysDept, type SysUser } from '@/types'

const loading = ref(false)
const treeData = ref<SysDept[]>([])
const userOptions = ref<SysUser[]>([])
const search = reactive({
  deptName: '',
  status: undefined as number | undefined,
})
const dialogVisible = ref(false)
const editingDept = ref<any>(null)
const form = reactive({
  parentId: 0,
  deptName: '',
  sort: 0,
  leaderUserId: undefined as number | undefined,
  status: CommonStatus.ACTIVE,
})

function userLabel(user: SysUser) {
  return user.nickname ? `${user.nickname}（${user.username}）` : user.username
}

function leaderLabel(leaderUserId?: number | null) {
  if (!leaderUserId) return '-'
  const user = userOptions.value.find((item) => item.id === leaderUserId)
  return user ? userLabel(user) : `用户 #${leaderUserId}`
}

async function loadData() {
  loading.value = true
  try {
    const data: any = await getDeptTree({
      deptName: search.deptName || undefined,
      status: search.status,
    })
    treeData.value = data || []
  } finally {
    loading.value = false
  }
}

async function loadUsers() {
  const data: any = await getUserList({
    status: CommonStatus.ACTIVE,
    page: 1,
    pageSize: 200,
  })
  userOptions.value = data.list || []
}

function openDialog(dept?: any, parentId?: number) {
  editingDept.value = dept || null
  if (dept) {
    Object.assign(form, {
      parentId: dept.parentId,
      deptName: dept.deptName,
      sort: dept.sort,
      leaderUserId: dept.leaderUserId,
      status: dept.status,
    })
  } else {
    Object.assign(form, {
      parentId: parentId || 0,
      deptName: '',
      sort: 0,
      leaderUserId: undefined,
      status: CommonStatus.ACTIVE,
    })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    if (editingDept.value) {
      await updateDept(editingDept.value.id, { ...form })
      ElMessage.success('更新成功')
    } else {
      await createDept({ ...form })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {
    // handled
  }
}

async function handleDelete(id: number) {
  try {
    await deleteDept(id)
    ElMessage.success('删除成功')
    loadData()
  } catch {
    // handled
  }
}

onMounted(() => {
  loadData()
  loadUsers()
})
</script>
