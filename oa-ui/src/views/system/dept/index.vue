<template>
  <div>
    <el-card>
      <div class="toolbar">
        <div></div>
        <el-button type="primary" @click="openDialog()">新增部门</el-button>
      </div>
      <el-table
        :data="treeData"
        row-key="id"
        :tree-props="{ children: 'children' }"
        v-loading="loading"
        default-expand-all
      >
        <el-table-column prop="deptName" label="部门名称" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="leaderUserId" label="负责人ID" width="100" />
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
        <el-form-item label="负责人ID">
          <el-input-number v-model="form.leaderUserId" />
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
import { getDeptTree, createDept, updateDept, deleteDept } from '@/api/system'

const loading = ref(false)
const treeData = ref<any[]>([])
const dialogVisible = ref(false)
const editingDept = ref<any>(null)
const form = reactive({
  parentId: 0,
  deptName: '',
  sort: 0,
  leaderUserId: undefined as number | undefined,
  status: 1,
})

async function loadData() {
  loading.value = true
  try {
    const data: any = await getDeptTree()
    treeData.value = data || []
  } finally {
    loading.value = false
  }
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
      status: 1,
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

onMounted(loadData)
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; margin-bottom: 15px; }
</style>
