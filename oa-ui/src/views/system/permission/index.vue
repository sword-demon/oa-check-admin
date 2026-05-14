<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>权限管理</span>
          <el-button type="primary">新增权限</el-button>
        </div>
      </template>
      <el-table :data="[]" empty-text="暂无数据" row-key="id" default-expand-all :tree-props="{ children: 'children' }">
        <el-table-column prop="name" label="权限名称" />
        <el-table-column prop="code" label="权限编码" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.type === 1">目录</el-tag>
            <el-tag v-else-if="row.type === 2" type="success">菜单</el-tag>
            <el-tag v-else type="warning">按钮</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default>
            <el-button link type="primary">新增子项</el-button>
            <el-button link type="primary">编辑</el-button>
            <el-button link type="danger">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
