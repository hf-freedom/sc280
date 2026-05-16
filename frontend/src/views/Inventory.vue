<template>
  <div class="container">
    <div class="page-header">
      <h1 class="page-title">库存管理</h1>
      <el-select v-model="selectedProduct" placeholder="按商品筛选" clearable @change="fetchInventory">
        <el-option
          v-for="product in products"
          :key="product.id"
          :label="product.name"
          :value="product.id"
        />
      </el-select>
    </div>
    <el-card>
      <el-table :data="inventory" border style="width: 100%">
        <el-table-column prop="serialNumber" label="序列号" width="150" />
        <el-table-column prop="productName" label="所属商品" width="150">
          <template #default="{ row }">
            {{ getProductName(row.productId) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentOrderId" label="当前订单" width="200" show-overflow-tooltip />
        <el-table-column prop="maintenanceStart" label="维修开始时间" width="180">
          <template #default="{ row }">
            {{ row.maintenanceStart ? formatDate(row.maintenanceStart) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="maintenanceDays" label="预计维修天数" width="120">
          <template #default="{ row }">
            {{ row.maintenanceDays || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="maintenanceNote" label="维修备注" width="200" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.maintenanceNote || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'MAINTENANCE'"
              type="primary"
              size="small"
              @click="completeMaintenance(row.id)"
            >
              完成维修
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { inventoryApi, productApi } from '../api'

const inventory = ref([])
const products = ref([])
const selectedProduct = ref('')

const fetchProducts = async () => {
  try {
    const res = await productApi.list()
    products.value = res.data.data
  } catch (e) {
    console.error(e)
  }
}

const fetchInventory = async () => {
  try {
    const res = await inventoryApi.list(selectedProduct.value || undefined)
    inventory.value = res.data.data
  } catch (e) {
    console.error(e)
  }
}

const getProductName = (productId) => {
  const product = products.value.find(p => p.id === productId)
  return product ? product.name : productId
}

const getStatusType = (status) => {
  const map = {
    AVAILABLE: 'success',
    RENTED: 'warning',
    MAINTENANCE: 'danger',
    DAMAGED: 'info'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    AVAILABLE: '可租赁',
    RENTED: '已出租',
    MAINTENANCE: '维修中',
    DAMAGED: '已损坏'
  }
  return map[status] || status
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const completeMaintenance = async (id) => {
  try {
    await ElMessageBox.confirm('确认完成该物品的维修？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await inventoryApi.completeMaintenance(id)
    ElMessage.success('维修完成')
    fetchInventory()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  fetchProducts()
  fetchInventory()
})
</script>