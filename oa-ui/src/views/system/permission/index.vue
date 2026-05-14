<template>
  <div>
    <el-card>
      <div class="toolbar">
        <div></div>
        <el-button type="primary" @click="openDialog()">新增权限</el-button>
      </div>
      <el-table
        :data="treeData"
        row-key="id"
        :tree-props="{ children: 'children' }"
        v-loading="loading"
        default-expand-all
      >
        <el-table-column prop="permissionName" label="权限名称" />
        <el-table-column prop="permissionType" label="类型">
          <template #default="{ row }">
            <el-tag v-if="row.permissionType === 1">菜单</el-tag>
            <el-tag v-else-if="row.permissionType === 2" type="warning">
              按钮
            </el-tag>
            <el-tag v-else-if="row.permissionType === 3" type="info">
              API
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路径/标识" />
        <el-table-column prop="icon" label="图标" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 1 ? 'success' : 'danger'"
              size="small"
            >
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button
              v-if="row.permissionType === 1"
              link
              type="success"
              @click="openDialog(undefined, row.id)"
            >
              新增子项
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
      :title="editingPerm ? '编辑权限' : '新增权限'"
      width="500px"
      destroy-on-close
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="上级">
          <el-input v-model="form.parentId" disabled />
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model="form.permissionName" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.permissionType">
            <el-option label="菜单" :value="1" />
            <el-option label="按钮" :value="2" />
            <el-option label="API" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="路径">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item label="组件">
          <el-input v-model="form.component" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
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
import {
  getPermissionTree,
  createPermission,
  updatePermission,
  deletePermission,
} from '@/api/system'

const loading = ref(false)
const treeData = ref<any[]>([])
const dialogVisible = ref(false)
const editingPerm = ref<any>(null)
const form = reactive({
  parentId: 0,
  permissionName: '',
  permissionType: 1,
  path: '',
  component: '',
  icon: '',
  sort: 0,
  status: 1,
})

async function loadData() {
  loading.value = true
  try {
    const data: any = await getPermissionTree()
    treeData.value = data || []
  } finally {
    loading.value = false
  }
}

function openDialog(perm?: any, parentId?: number) {
  editingPerm.value = perm || null
  if (perm) {
    Object.assign(form, {
      parentId: perm.parentId,
      permissionName: perm.permissionName,
      permissionType: perm.permissionType,
      path: perm.path || '',
      component: perm.component || '',
      icon: perm.icon || '',
      sort: perm.sort,
      status: perm.status,
    })
  } else {
    Object.assign(form, {
      parentId: parentId || 0,
      permissionName: '',
      permissionType: 1,
      path: '',
      component: '',
      icon: '',
      sort: 0,
      status: 1,
    })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    if (editingPerm.value) {
      await updatePermission(editingPerm.value.id, { ...form })
      ElMessage.success('更新成功')
    } else {
      await createPermission({ ...form })
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
    await deletePermission(id)
    ElMessage.success('删除成功')
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
