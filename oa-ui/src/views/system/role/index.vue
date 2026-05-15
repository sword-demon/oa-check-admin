<template>
  <div>
    <el-card>
      <div class="toolbar">
        <div></div>
        <el-button type="primary" @click="openDialog()">新增角色</el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading">
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
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getRoleList,
  createRole,
  updateRole,
  deleteRole,
  assignPermissions,
  getPermissionTree,
} from '@/api/system'

const loading = ref(false)
const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const editingRole = ref<any>(null)
const form = reactive({
  roleName: '',
  roleKey: '',
  dataScope: 1,
  status: 1,
})

const permDialogVisible = ref(false)
const permTreeData = ref<any[]>([])
const checkedPermIds = ref<number[]>([])
const permTreeRef = ref<any>()
const currentPermRoleId = ref<number>(0)

async function loadData() {
  loading.value = true
  try {
    const data: any = await getRoleList()
    tableData.value = Array.isArray(data) ? data : data?.list || []
  } finally {
    loading.value = false
  }
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
      status: 1,
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
  const tree: any = await getPermissionTree()
  permTreeData.value = tree || []
  checkedPermIds.value = role.permissions?.map((p: any) => p.id) || []
  permDialogVisible.value = true
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

<style scoped>
.toolbar { display: flex; justify-content: space-between; margin-bottom: 15px; }
</style>
