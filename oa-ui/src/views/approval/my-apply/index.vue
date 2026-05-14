<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>我的申请</span>
          <el-button type="primary">发起申请</el-button>
        </div>
      </template>
      <el-table :data="[]" empty-text="暂无数据">
        <el-table-column prop="title" label="申请标题" />
        <el-table-column prop="templateName" label="审批模板" width="140" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning">审批中</el-tag>
            <el-tag v-else-if="row.status === 1" type="success">已通过</el-tag>
            <el-tag v-else-if="row.status === 2" type="danger">已拒绝</el-tag>
            <el-tag v-else type="info">已撤回</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary">查看</el-button>
            <el-button link type="warning" v-if="row.status === 0">撤回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
