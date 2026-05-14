<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>审批模板</span>
          <el-button type="primary" @click="dialogVisible = true">新建模板</el-button>
        </div>
      </template>
      <el-table :data="[]" empty-text="暂无数据">
        <el-table-column prop="name" label="模板名称" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default>
            <el-button link type="primary">编辑</el-button>
            <el-button link type="danger">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog v-model="dialogVisible" title="新建审批模板" width="600px">
      <el-form label-width="100px">
        <el-form-item label="模板名称">
          <el-input placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="模板描述">
          <el-input type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="审批流程">
          <el-button type="primary" plain>配置流程节点</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const dialogVisible = ref(false)
</script>
