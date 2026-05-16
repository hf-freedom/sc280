<template>
  <el-container style="height: 100vh">
    <el-aside width="200px" style="background-color: #2c3e50">
      <div style="padding: 20px; color: white; font-size: 18px; font-weight: bold">
        租赁库存系统
      </div>
      <el-menu
        default-active="0"
        class="el-menu-vertical"
        background-color="#2c3e50"
        text-color="#fff"
        active-text-color="#409eff"
        router
      >
        <el-menu-item index="/">
          <el-icon><DataLine /></el-icon>
          <span>数据概览</span>
        </el-menu-item>
        <el-menu-item index="/products">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/inventory">
          <el-icon><Box /></el-icon>
          <span>库存管理</span>
        </el-menu-item>
        <el-menu-item index="/orders">
          <el-icon><Document /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/alerts">
          <el-icon><Bell /></el-icon>
          <span>告警中心</span>
          <el-badge :value="alertCount" :hidden="alertCount === 0" class="item" />
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-main style="background-color: #f5f7fa">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  DataLine,
  Goods,
  Box,
  Document,
  User,
  Bell
} from '@element-plus/icons-vue'
import { alertApi } from './api'

const alertCount = ref(0)

const fetchAlerts = async () => {
  try {
    const [expiring, overdue, maintenance] = await Promise.all([
      alertApi.getExpiring(),
      alertApi.getOverdue(),
      alertApi.getMaintenanceTimeout()
    ])
    alertCount.value =
      (expiring.data.data?.length || 0) +
      (overdue.data.data?.length || 0) +
      (maintenance.data.data?.length || 0)
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  fetchAlerts()
  setInterval(fetchAlerts, 60000)
})
</script>

<style scoped>
.el-menu-vertical {
  border-right: none;
}
</style>