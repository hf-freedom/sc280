<template>
  <div class="container">
    <div class="page-header">
      <h1 class="page-title">商品管理</h1>
    </div>
    <el-card>
      <el-table :data="products" border style="width: 100%">
        <el-table-column prop="name" label="商品名称" width="150" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="category" label="分类" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dailyPrice" label="日租金" width="120">
          <template #default="{ row }">
            ¥{{ row.dailyPrice }}/天
          </template>
        </el-table-column>
        <el-table-column prop="baseDeposit" label="基础押金" width="120">
          <template #default="{ row }">
            ¥{{ row.baseDeposit }}
          </template>
        </el-table-column>
        <el-table-column prop="totalQuantity" label="总库存" width="100" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { productApi } from '../api'

const products = ref([])

const fetchProducts = async () => {
  try {
    const res = await productApi.list()
    products.value = res.data.data
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  fetchProducts()
})
</script>