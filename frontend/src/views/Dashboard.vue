<template>
  <div class="container">
    <div class="page-header">
      <h1 class="page-title">数据概览</h1>
    </div>

    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.totalProducts }}</div>
          <div class="stat-label">商品总数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.totalInventory }}</div>
          <div class="stat-label">库存总数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value" style="color: #67c23a">{{ stats.activeOrders }}</div>
          <div class="stat-label">进行中订单</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value" style="color: #e6a23c">{{ stats.maintenanceCount }}</div>
          <div class="stat-label">维修中物品</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>快速操作</span>
          </template>
          <div style="display: flex; flex-direction: column; gap: 10px">
            <el-button type="primary" @click="showCreateOrder = true" style="width: 100%">
              创建新租赁订单
            </el-button>
            <el-button @click="$router.push('/alerts')" style="width: 100%">
              查看告警通知
            </el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>系统说明</span>
          </template>
          <div style="line-height: 2">
            <p>• 支持按租期锁定库存，自动计算时间段重叠</p>
            <p>• 逾期未归还自动计算违约金</p>
            <p>• 商品损坏归还后进入维修状态</p>
            <p>• 续租需重新校验后续时间段库存</p>
            <p>• 高信用用户免押金，低信用用户需更高押金</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>

  <el-dialog v-model="showCreateOrder" title="创建租赁订单" width="600px">
    <el-form :model="orderForm" label-width="100px">
      <el-form-item label="选择用户">
        <el-select v-model="orderForm.userId" placeholder="请选择用户" style="width: 100%">
          <el-option
            v-for="user in users"
            :key="user.id"
            :label="`${user.name} (${user.phone})`"
            :value="user.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="选择商品">
        <el-select v-model="orderForm.productId" placeholder="请选择商品" style="width: 100%" @change="checkStock">
          <el-option
            v-for="product in products"
            :key="product.id"
            :label="product.name"
            :value="product.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="租赁数量">
        <el-input-number v-model="orderForm.quantity" :min="1" :max="100" />
      </el-form-item>
      <el-form-item label="开始时间">
        <el-date-picker
          v-model="orderForm.startTime"
          type="datetime"
          placeholder="选择开始时间"
          style="width: 100%"
          @change="checkStock"
        />
      </el-form-item>
      <el-form-item label="结束时间">
        <el-date-picker
          v-model="orderForm.endTime"
          type="datetime"
          placeholder="选择结束时间"
          style="width: 100%"
          @change="checkStock"
        />
      </el-form-item>
      <el-form-item v-if="availableCount !== null" label="可用库存">
        <el-tag :type="availableCount >= orderForm.quantity ? 'success' : 'danger'">
          {{ availableCount }} 件
        </el-tag>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showCreateOrder = false">取消</el-button>
      <el-button type="primary" @click="submitOrder" :loading="submitting">确认创建</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { statsApi, productApi, userApi, orderApi } from '../api'

const stats = ref({
  totalProducts: 0,
  totalInventory: 0,
  activeOrders: 0,
  maintenanceCount: 0
})

const showCreateOrder = ref(false)
const products = ref([])
const users = ref([])
const availableCount = ref(null)
const submitting = ref(false)

const orderForm = ref({
  userId: '',
  productId: '',
  quantity: 1,
  startTime: '',
  endTime: ''
})

const fetchStats = async () => {
  try {
    const res = await statsApi.get()
    stats.value = res.data.data
  } catch (e) {
    console.error(e)
  }
}

const fetchProducts = async () => {
  try {
    const res = await productApi.list()
    products.value = res.data.data
  } catch (e) {
    console.error(e)
  }
}

const fetchUsers = async () => {
  try {
    const res = await userApi.list()
    users.value = res.data.data
  } catch (e) {
    console.error(e)
  }
}

const checkStock = async () => {
  if (orderForm.value.productId && orderForm.value.startTime && orderForm.value.endTime) {
    try {
      const res = await productApi.checkAvailability(
        orderForm.value.productId,
        orderForm.value.startTime,
        orderForm.value.endTime
      )
      availableCount.value = res.data.data.availableCount
    } catch (e) {
      console.error(e)
    }
  }
}

const submitOrder = async () => {
  if (!orderForm.value.userId || !orderForm.value.productId || !orderForm.value.startTime || !orderForm.value.endTime) {
    ElMessage.warning('请填写完整信息')
    return
  }

  submitting.value = true
  try {
    await orderApi.create(orderForm.value)
    ElMessage.success('订单创建成功')
    showCreateOrder.value = false
    fetchStats()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '创建失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchStats()
  fetchProducts()
  fetchUsers()
})
</script>