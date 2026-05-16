<template>
  <div class="container">
    <div class="page-header">
      <h1 class="page-title">用户管理</h1>
    </div>
    <el-card>
      <el-table :data="users" border style="width: 100%">
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="creditScore" label="信用分" width="120" />
        <el-table-column prop="creditLevel" label="信用等级" width="120">
          <template #default="{ row }">
            <el-tag :type="getCreditLevelType(row.creditLevel)" size="small">
              {{ getCreditLevelText(row.creditLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="押金说明">
          <template #default="{ row }">
            {{ getDepositDesc(row.creditLevel) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '../api'

const users = ref([])

const fetchUsers = async () => {
  try {
    const res = await userApi.list()
    users.value = res.data.data
  } catch (e) {
    console.error(e)
  }
}

const getCreditLevelType = (level) => {
  const map = {
    HIGH: 'success',
    MEDIUM: 'warning',
    LOW: 'danger'
  }
  return map[level] || 'info'
}

const getCreditLevelText = (level) => {
  const map = {
    HIGH: '高信用',
    MEDIUM: '中信用',
    LOW: '低信用'
  }
  return map[level] || level
}

const getDepositDesc = (level) => {
  const map = {
    HIGH: '免押金',
    MEDIUM: '1倍基础押金',
    LOW: '2倍基础押金'
  }
  return map[level] || '-'
}

onMounted(() => {
  fetchUsers()
})
</script>