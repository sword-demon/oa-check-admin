<template>
  <div>
    <el-card>
      <template #header>
        <span>我的待办</span>
      </template>
      <el-table :data="[]" empty-text="暂无待办">
        <el-table-column prop="title" label="申请标题" />
        <el-table-column prop="applicantName" label="申请人" width="120" />
        <el-table-column prop="templateName" label="审批模板" width="140" />
        <el-table-column prop="createdAt" label="申请时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default>
            <el-button link type="primary">查看详情</el-button>
            <el-button link type="success">通过</el-button>
            <el-button link type="danger">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
