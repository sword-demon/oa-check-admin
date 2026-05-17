<template>
  <div>
    <el-card>
      <div class="toolbar">
        <div class="search">
<#if ctx.entity.searchableFields?size gt 0>
  <#list ctx.entity.searchableFields as field>
          <el-${fieldSearchControl(field)}
            v-model="search.${field.name}"
            placeholder="${field.comment}"
            clearable
            style="width: ${fieldSearchWidth(field)}"
            <#if field.enumRef??><@searchSelectOptions field/></#if>
            @<#if field.enumRef??>change<#else>clear</#if>="loadData"
          />
  </#list>
          <el-button type="primary" style="margin-left: 10px" @click="loadData">搜索</el-button>
</#if>
        </div>
        <el-button type="primary" @click="openFormDialog()">新增${ctx.entity.comment}</el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading">
  <#list ctx.entity.fields as field>
        <el-table-column prop="${field.name}" label="${field.comment}" <#if field.type == "String">min-width="150"<#else/>width="<#if field.type == "LocalDateTime">170<#else>120</#if>"</#if> />
  </#list>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openFormDialog(row)">编辑</el-button>
            <el-popconfirm title="确认删除?" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        style="margin-top: 15px; justify-content: flex-end"
        @current-change="loadData"
      />
    </el-card>

    <${ctx.entity.name}FormDialog
      v-model:visible="formVisible"
      :${ctx.entity.beanName}-data="editingRow"
      @saved="loadData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  get${ctx.entity.name}List,
  delete${ctx.entity.name},
} from '@/api/${ctx.config.module}'
import ${ctx.entity.name}FormDialog from './components/${ctx.entity.name}FormDialog.vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const search = reactive({
<#list ctx.entity.searchableFields as field>
  ${field.name}: undefined as <#if field.type == "String">string<#elseif field.type == "Integer">number<#else/>string</#if> | undefined,
</#list>
})
const formVisible = ref(false)
const editingRow = ref<any>(null)

async function loadData() {
  loading.value = true
  try {
    const data: any = await get${ctx.entity.name}List({
  <#list ctx.entity.searchableFields as field>
      ${field.name}: search.${field.name} || undefined,
  </#list>
      page: page.value,
      pageSize: pageSize.value,
    })
    tableData.value = data.list || data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function openFormDialog(row?: any) {
  editingRow.value = row || null
  formVisible.value = true
}

async function handleDelete(id: number) {
  try {
    await delete${ctx.entity.name}(id)
    ElMessage.success('删除成功')
    loadData()
  } catch {
    // interceptor handles error
  }
}

onMounted(loadData)
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; margin-bottom: 15px; }
.search { display: flex; align-items: center; gap: 10px; }
</style>
<#macro searchSelectOptions field>
  <#if ctx.enums[field.enumRef]??>
            >
    <#list ctx.enums[field.enumRef].values as enumVal>
            <el-option label="${enumVal.label}" :value="${enumVal.code}" />
    </#list>
          </el-${fieldSearchControl(field)}
  </#if>
</#macro>
<#function fieldSearchControl field>
  <#if field.enumRef??>
    <#return "select">
  <#elseif field.type == "String">
    <#return "input">
  <#else>
    <#return "input">
  </#if>
</#function>
<#function fieldSearchWidth field>
  <#if field.enumRef??>
    <#return "130px">
  <#elseif field.type == "String">
    <#return "200px">
  <#else>
    <#return "200px">
  </#if>
</#function>
