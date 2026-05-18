<template>
  <div class="page-shell page-shell--dense">
    <el-card class="page-panel">
      <div class="toolbar">
        <div class="search">
          <el-input
            v-model="search.roleName"
            placeholder="角色名称"
            clearable
            style="width: 200px"
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
          <el-select
            v-model="search.status"
            placeholder="状态"
            clearable
            style="width: 120px; margin-left: 10px"
            @change="handleSearch"
          >
            <el-option label="正常" :value="CommonStatus.ACTIVE" />
            <el-option label="禁用" :value="CommonStatus.DISABLED" />
          </el-select>
          <el-button type="primary" style="margin-left: 10px" @click="handleSearch">搜索</el-button>
        </div>
        <el-button type="primary" @click="openDialog()">新增角色</el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading" class="page-table">
        <el-table-column prop="roleName" label="角色名称" />
        <el-table-column prop="roleKey" label="角色标识" />
        <el-table-column prop="dataScope" label="数据范围">
          <template #default="{ row }">
            {{
              row.dataScope === 1
                ? '全部'
                : row.dataScope === 2
                  ? '本部门'
                  : '自定义'
            }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === CommonStatus.ACTIVE ? 'success' : 'danger'">
              {{ row.status === CommonStatus.ACTIVE ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="warning" @click="openPermissionDialog(row)">
              分配权限
            </el-button>
            <el-popconfirm title="确认删除?" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="page-pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="editingRole ? '编辑角色' : '新增角色'"
      width="500px"
      destroy-on-close
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="角色名称" required>
          <el-input v-model="form.roleName" />
        </el-form-item>
        <el-form-item label="角色标识" required>
          <el-input v-model="form.roleKey" />
        </el-form-item>
        <el-form-item label="数据范围">
          <el-select v-model="form.dataScope">
            <el-option label="全部数据" :value="1" />
            <el-option label="本部门数据" :value="2" />
            <el-option label="自定义部门" :value="3" />
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

    <el-dialog
      v-model="permDialogVisible"
      title="分配权限"
      width="600px"
      destroy-on-close
    >
      <el-tree
        ref="permTreeRef"
        :data="permTreeData"
        show-checkbox
        node-key="id"
        :default-checked-keys="checkedPermIds"
        :props="{ label: 'permissionName', children: 'children' }"
      />
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignPermissions">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { nextTick, ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getRoleList,
  createRole,
  updateRole,
  deleteRole,
  assignPermissions,
  getPermissionTree,
  getRolePermissionIds,
} from '@/api/system'
import { CommonStatus } from '@/types'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const search = reactive({
  roleName: '',
  status: undefined as number | undefined,
})
const pagination = reactive({
  page: 1,
  pageSize: 20,
})
const dialogVisible = ref(false)
const editingRole = ref<any>(null)
const form = reactive({
  roleName: '',
  roleKey: '',
  dataScope: 1,
  status: CommonStatus.ACTIVE,
})

const permDialogVisible = ref(false)
const permTreeData = ref<any[]>([])
const checkedPermIds = ref<number[]>([])
const permTreeRef = ref<any>()
const currentPermRoleId = ref<number>(0)

async function loadData() {
  loading.value = true
  try {
    const data: any = await getRoleList({
      roleName: search.roleName || undefined,
      status: search.status,
      page: pagination.page,
      pageSize: pagination.pageSize,
    })
    tableData.value = Array.isArray(data) ? data : data?.list || []
    total.value = Array.isArray(data) ? data.length : data?.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  loadData()
}

function openDialog(role?: any) {
  editingRole.value = role || null
  if (role) {
    Object.assign(form, {
      roleName: role.roleName,
      roleKey: role.roleKey,
      dataScope: role.dataScope,
      status: role.status,
    })
  } else {
    Object.assign(form, {
      roleName: '',
      roleKey: '',
      dataScope: 1,
      status: CommonStatus.ACTIVE,
    })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    if (editingRole.value) {
      await updateRole(editingRole.value.id, { ...form })
      ElMessage.success('更新成功')
    } else {
      await createRole({ ...form })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {
    // handled by interceptor
  }
}

async function handleDelete(id: number) {
  try {
    await deleteRole(id)
    ElMessage.success('删除成功')
    loadData()
  } catch {
    // handled
  }
}

async function openPermissionDialog(role: any) {
  currentPermRoleId.value = role.id
  const [tree, permissionIds] = await Promise.all([
    getPermissionTree(),
    getRolePermissionIds(role.id),
  ])
  permTreeData.value = tree || []
  checkedPermIds.value = permissionIds || []
  permDialogVisible.value = true
  await nextTick()
  permTreeRef.value?.setCheckedKeys(checkedPermIds.value, false)
}

async function handleAssignPermissions() {
  const checkedKeys = permTreeRef.value?.getCheckedKeys(false) || []
  try {
    await assignPermissions(currentPermRoleId.value, checkedKeys)
    ElMessage.success('权限分配成功')
    permDialogVisible.value = false
    loadData()
  } catch {
    // handled
  }
}

onMounted(loadData)
</script>
