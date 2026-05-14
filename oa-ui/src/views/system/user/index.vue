<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>用户管理</span>
          <el-button type="primary">新增</el-button>
        </div>
      </template>
      <el-table :data="[]" empty-text="暂无数据">
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="deptName" label="部门" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default>
            <el-button link type="primary">编辑</el-button>
            <el-button link type="danger">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
