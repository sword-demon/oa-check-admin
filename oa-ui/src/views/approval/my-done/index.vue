<template>
  <div>
    <el-card>
      <template #header>
        <span>我的已办</span>
      </template>
      <el-table :data="[]" empty-text="暂无已办记录">
        <el-table-column prop="title" label="申请标题" />
        <el-table-column prop="applicantName" label="申请人" width="120" />
        <el-table-column prop="templateName" label="审批模板" width="140" />
        <el-table-column prop="result" label="审批结果" width="120">
          <template #default="{ row }">
            <el-tag :type="row.result === 1 ? 'success' : 'danger'">
              {{ row.result === 1 ? '通过' : '拒绝' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="审批意见" />
        <el-table-column prop="approvedAt" label="审批时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default>
            <el-button link type="primary">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
