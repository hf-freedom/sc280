<template>
  <div class="container">
    <div class="page-header">
      <h1 class="page-title">告警中心</h1>
      <el-button type="primary" @click="fetchAll">刷新</el-button>
    </div>

    <el-card style="margin-bottom: 20px">
      <template #header>
        <span style="display: flex; align-items: center">
          <el-icon style="color: #e6a23c; margin-right: 8px"><Warning /></el-icon>
          即将到期订单 ({{ expiringOrders.length }})
        </span>
      </template>
      <el-table v-if="expiringOrders.length > 0" :data="expiringOrders" border size="small">
        <el-table-column prop="id" label="订单号" width="200" show-overflow-tooltip />
        <el-table-column prop="userId" label="用户ID" width="150" show-overflow-tooltip />
        <el-table-column prop="productId" label="商品ID" width="150" show-overflow-tooltip />
        <el-table-column prop="endTime" label="到期时间" width="180">
          <template #default="{ row }">{{ formatDate(row.endTime) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag type="warning" size="small">即将到期</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无即将到期的订单" :image-size="100" />
    </el-card>

    <el-card style="margin-bottom: 20px">
      <template #header>
        <span style="display: flex; align-items: center">
          <el-icon style="color: #f56c6c; margin-right: 8px"><CircleClose /></el-icon>
          逾期订单 ({{ overdueOrders.length }})
        </span>
      </template>
      <el-table v-if="overdueOrders.length > 0" :data="overdueOrders" border size="small">
        <el-table-column prop="id" label="订单号" width="200" show-overflow-tooltip />
        <el-table-column prop="userId" label="用户ID" width="150" show-overflow-tooltip />
        <el-table-column prop="productId" label="商品ID" width="150" show-overflow-tooltip />
        <el-table-column prop="endTime" label="应归还时间" width="180">
          <template #default="{ row }">{{ formatDate(row.endTime) }}</template>
        </el-table-column>
        <el-table-column prop="penaltyAmount" label="违约金" width="100">
          <template #default="{ row }">¥{{ row.penaltyAmount || 0 }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag type="danger" size="small">已逾期</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无逾期订单" :image-size="100" />
    </el-card>

    <el-card>
      <template #header>
        <span style="display: flex; align-items: center">
          <el-icon style="color: #f56c6c; margin-right: 8px"><Tools /></el-icon>
          维修超时物品 ({{ maintenanceTimeoutItems.length }})
        </span>
      </template>
      <el-table v-if="maintenanceTimeoutItems.length > 0" :data="maintenanceTimeoutItems" border size="small">
        <el-table-column prop="serialNumber" label="序列号" width="150" />
        <el-table-column prop="productId" label="商品ID" width="200" show-overflow-tooltip />
        <el-table-column prop="maintenanceStart" label="维修开始时间" width="180">
          <template #default="{ row }">{{ formatDate(row.maintenanceStart) }}</template>
        </el-table-column>
        <el-table-column prop="maintenanceDays" label="预计天数" width="100" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="completeMaintenance(row.id)">
              完成维修
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无维修超时物品" :image-size="100" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Warning, CircleClose, Tools } from '@element-plus/icons-vue'
import { alertApi, inventoryApi } from '../api'

const expiringOrders = ref([])
const overdueOrders = ref([])
const maintenanceTimeoutItems = ref([])

const fetchExpiring = async () => {
  try {
    const res = await alertApi.getExpiring()
    expiringOrders.value = res.data.data
  } catch (e) {
    console.error(e)
  }
}

const fetchOverdue = async () => {
  try {
    const res = await alertApi.getOverdue()
    overdueOrders.value = res.data.data
  } catch (e) {
    console.error(e)
  }
}

const fetchMaintenanceTimeout = async () => {
  try {
    const res = await alertApi.getMaintenanceTimeout()
    maintenanceTimeoutItems.value = res.data.data
  } catch (e) {
    console.error(e)
  }
}

const fetchAll = () => {
  fetchExpiring()
  fetchOverdue()
  fetchMaintenanceTimeout()
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
    fetchMaintenanceTimeout()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  fetchAll()
})
</script>