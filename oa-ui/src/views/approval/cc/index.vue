<template>
  <div>
    <el-card>
      <template #header>
        <span>抄送给我的</span>
      </template>
      <el-table :data="[]" empty-text="暂无抄送记录">
        <el-table-column prop="title" label="申请标题" />
        <el-table-column prop="applicantName" label="申请人" width="120" />
        <el-table-column prop="templateName" label="审批模板" width="140" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning">审批中</el-tag>
            <el-tag v-else-if="row.status === 1" type="success">已通过</el-tag>
            <el-tag v-else-if="row.status === 2" type="danger">已拒绝</el-tag>
            <el-tag v-else type="info">已撤回</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="抄送时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default>
            <el-button link type="primary">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
