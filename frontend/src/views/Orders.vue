<template>
  <div class="container">
    <div class="page-header">
      <h1 class="page-title">订单管理</h1>
      <el-button type="primary" @click="openCreateOrder">创建订单</el-button>
    </div>
    <el-card>
      <el-table :data="orders" border style="width: 100%">
        <el-table-column prop="id" label="订单号" width="180" show-overflow-tooltip />
        <el-table-column prop="userName" label="用户" width="100">
          <template #default="{ row }">
            {{ getUserName(row.userId) }}
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="商品" width="120">
          <template #default="{ row }">
            {{ getProductName(row.productId) }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="70" />
        <el-table-column label="租期信息" width="280">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 4px">
              <el-tag type="info" size="small" v-if="row.rentalDays">共 {{ row.rentalDays }} 天</el-tag>
            </div>
            <div style="margin-top: 4px">{{ formatDate(row.startTime) }}</div>
            <div style="color: #909399; font-size: 12px">至 {{ formatDate(row.endTime) }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="总金额" width="90">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="penaltyAmount" label="违约金" width="90">
          <template #default="{ row }">
            <span v-if="row.penaltyAmount" style="color: #f56c6c">¥{{ row.penaltyAmount }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="refundAmount" label="退款金额" width="100">
          <template #default="{ row }">
            <span v-if="row.status === 'CANCELLED' && row.refundAmount" style="color: #67c23a">¥{{ row.refundAmount }}</span>
            <span v-else-if="row.status === 'CANCELLED'" style="color: #909399">¥0.00</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="returnedDamaged" label="归还状态" width="100">
          <template #default="{ row }">
            <span v-if="row.status === 'RETURNED'">
              <el-tag :type="row.returnedDamaged ? 'danger' : 'success'" size="small">
                {{ row.returnedDamaged ? '损坏归还' : '正常归还' }}
              </el-tag>
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="实际归还时间" width="180">
          <template #default="{ row }">
            {{ row.actualReturnTime ? formatDate(row.actualReturnTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button
                v-if="row.status === 'CONFIRMED'"
                type="warning"
                size="small"
                @click="cancelOrder(row)"
              >
                取消订单
              </el-button>
              <el-button
                v-if="row.status === 'RENTING' || row.status === 'OVERDUE'"
                type="primary"
                size="small"
                @click="showRenewDialog(row)"
              >
                续租
              </el-button>
              <el-button
                v-if="row.status === 'RENTING' || row.status === 'OVERDUE'"
                type="success"
                size="small"
                @click="showReturnDialog(row, false)"
              >
                正常归还
              </el-button>
              <el-button
                v-if="row.status === 'RENTING' || row.status === 'OVERDUE'"
                type="danger"
                size="small"
                @click="showReturnDialog(row, true)"
              >
                损坏归还
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>

  <el-dialog v-model="showCreateOrder" title="创建租赁订单" width="600px">
    <el-form :model="orderForm" label-width="100px">
      <el-form-item label="选择用户">
        <el-select v-model="orderForm.userId" placeholder="请选择用户" style="width: 100%">
          <el-option
            v-for="user in users"
            :key="user.id"
            :label="`${user.name} (${user.phone}) - ${user.creditLevel === 'HIGH' ? '免押金' : user.creditLevel === 'MEDIUM' ? '正常押金' : '双倍押金'}`"
            :value="user.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="选择商品">
        <el-select v-model="orderForm.productId" placeholder="请选择商品" style="width: 100%" @change="checkStock">
          <el-option
            v-for="product in products"
            :key="product.id"
            :label="`${product.name} (¥${product.dailyPrice}/天)`"
            :value="product.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="租赁数量">
        <el-input-number v-model="orderForm.quantity" :min="1" :max="100" @change="checkStock" />
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
      <el-form-item v-if="availableCount !== null" label="库存校验">
        <el-tag :type="availableCount >= orderForm.quantity ? 'success' : 'danger'">
          可用库存：{{ availableCount }} 件
        </el-tag>
      </el-form-item>
      <el-form-item v-if="orderDays > 0 && orderForm.productId" label="预估费用">
        <div style="color: #409eff; font-weight: 500; font-size: 16px">
          {{ orderDays }} 天 × ¥{{ getProductDailyPrice(orderForm.productId) }} × {{ orderForm.quantity }} 件 = ¥{{ orderDays * getProductDailyPrice(orderForm.productId) * orderForm.quantity }}
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showCreateOrder = false">取消</el-button>
      <el-button
        type="primary"
        @click="submitOrder"
        :loading="submitting"
        :disabled="availableCount !== null && availableCount < orderForm.quantity"
      >
        确认创建
      </el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="showRenew" title="续租订单" width="600px">
    <el-descriptions :column="2" border style="margin-bottom: 20px" size="small">
      <el-descriptions-item label="商品">{{ getProductName(currentOrder.productId) }}</el-descriptions-item>
      <el-descriptions-item label="数量">{{ currentOrder.quantity }} 件</el-descriptions-item>
      <el-descriptions-item label="原租期">
        {{ formatDate(currentOrder.startTime) }} 至 {{ formatDate(currentOrder.endTime) }}
      </el-descriptions-item>
      <el-descriptions-item label="日租金">¥{{ currentOrder.dailyPrice }}/件/天</el-descriptions-item>
    </el-descriptions>
    <el-form :model="renewForm" label-width="100px">
      <el-form-item label="原结束时间">
        <el-input :value="formatDate(currentOrder.endTime)" disabled style="width: 100%" />
      </el-form-item>
      <el-form-item label="续租至">
        <el-date-picker
          v-model="renewForm.newEndTime"
          type="datetime"
          placeholder="选择新的结束时间"
          style="width: 100%"
          @change="checkRenewStock"
        />
      </el-form-item>
      <el-form-item v-if="renewStockCheck.available" label="库存校验">
        <el-tag :type="renewStockCheck.availableCount >= currentOrder.quantity ? 'success' : 'danger'" size="small">
          后续时段可用库存：{{ renewStockCheck.availableCount }} 件
        </el-tag>
      </el-form-item>
      <el-form-item v-if="renewDays > 0" label="续租费用">
        <div style="color: #409eff; font-weight: 500">
          {{ renewDays }} 天 × ¥{{ currentOrder.dailyPrice }} × {{ currentOrder.quantity }} 件 = ¥{{ renewDays * currentOrder.dailyPrice * currentOrder.quantity }}
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showRenew = false">取消</el-button>
      <el-button
        type="primary"
        @click="submitRenew"
        :loading="renewing"
        :disabled="renewStockCheck.available && renewStockCheck.availableCount < currentOrder.quantity"
      >
        确认续租
      </el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="showCancel" title="取消订单" width="550px">
    <el-descriptions :column="2" border style="margin-bottom: 20px" size="small">
      <el-descriptions-item label="商品">{{ getProductName(cancelOrderInfo.productId) }}</el-descriptions-item>
      <el-descriptions-item label="数量">{{ cancelOrderInfo.quantity }} 件</el-descriptions-item>
      <el-descriptions-item label="订单金额">¥{{ cancelOrderInfo.totalAmount }}</el-descriptions-item>
      <el-descriptions-item label="开始时间">{{ formatDate(cancelOrderInfo.startTime) }}</el-descriptions-item>
    </el-descriptions>
    <div style="background: #f5f7fa; padding: 15px; border-radius: 6px; margin-bottom: 20px">
      <div style="margin-bottom: 10px; font-weight: 500; color: #606266">退款规则说明</div>
      <div style="font-size: 13px; color: #909399; line-height: 1.8">
        <div>• 距离开始时间 ≥ 72 小时：<span style="color: #67c23a">100% 退款</span></div>
        <div>• 距离开始时间 ≥ 48 小时且 < 72 小时：<span style="color: #e6a23c">80% 退款</span></div>
        <div>• 距离开始时间 ≥ 24 小时且 < 48 小时：<span style="color: #f56c6c">50% 退款</span></div>
        <div>• 距离开始时间 < 24 小时：<span style="color: #909399">0% 退款</span></div>
      </div>
    </div>
    <el-alert :type="cancelRefundInfo.hoursUntilStart < 24 ? 'error' : 'warning'" :closable="false" style="margin-bottom: 15px">
      <template #title>
        <div>
          <span>距离开始时间还有：</span>
          <span style="font-weight: bold; font-size: 16px">{{ cancelRefundInfo.hoursUntilStart }}</span>
          <span> 小时</span>
        </div>
        <div style="margin-top: 8px">
          <span>退款比例：</span>
          <span :style="{ fontWeight: 'bold', fontSize: '16px', color: cancelRefundInfo.hoursUntilStart < 24 ? '#f56c6c' : '#409eff' }">
            {{ (cancelRefundInfo.refundRatio * 100).toFixed(0) }}%
          </span>
        </div>
        <div style="margin-top: 8px">
          <span>预估退款金额：</span>
          <span style="font-weight: bold; font-size: 16px; color: #67c23a">
            ¥{{ (cancelOrderInfo.totalAmount * cancelRefundInfo.refundRatio).toFixed(2) }}
          </span>
        </div>
      </template>
    </el-alert>
    <template #footer>
      <el-button @click="showCancel = false">我再想想</el-button>
      <el-button type="danger" @click="confirmCancelOrder" :loading="canceling">确认取消</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="showReturn" :title="returnForm.damaged ? '损坏归还' : '正常归还'" width="500px">
    <el-form :model="returnForm" label-width="100px">
      <el-form-item v-if="returnForm.damaged" label="损坏备注">
        <el-input v-model="returnForm.damageNote" type="textarea" :rows="3" placeholder="请描述损坏情况" />
      </el-form-item>
      <el-form-item v-if="returnForm.damaged" label="预计维修天数">
        <el-input-number v-model="returnForm.maintenanceDays" :min="1" :max="30" />
      </el-form-item>
      <el-form-item v-else label="归还确认">
        <div style="color: #67c23c">确认商品完好无损，正常归还。</div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showReturn = false">取消</el-button>
      <el-button type="primary" @click="submitReturn" :loading="returning">确认归还</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi, productApi, userApi } from '../api'

const orders = ref([])
const products = ref([])
const users = ref([])
const availableCount = ref(null)
const orderDays = ref(0)

const showCreateOrder = ref(false)
const showRenew = ref(false)
const showReturn = ref(false)
const showCancel = ref(false)
const submitting = ref(false)
const renewing = ref(false)
const returning = ref(false)
const canceling = ref(false)

const currentOrderId = ref('')
const currentOrder = ref({
  productId: '',
  quantity: 1,
  startTime: '',
  endTime: '',
  dailyPrice: 0
})

const renewForm = ref({
  newEndTime: ''
})

const renewStockCheck = ref({
  available: false,
  availableCount: 0
})

const renewDays = ref(0)

const cancelOrderInfo = ref({
  id: '',
  productId: '',
  quantity: 0,
  totalAmount: 0,
  startTime: ''
})

const cancelRefundInfo = ref({
  hoursUntilStart: 0,
  refundRatio: 0
})

const returnForm = ref({
  damaged: false,
  damageNote: '',
  maintenanceDays: 3
})

const fetchOrders = async () => {
  try {
    const res = await orderApi.list()
    orders.value = res.data.data
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

const getProductName = (productId) => {
  const product = products.value.find(p => p.id === productId)
  return product ? product.name : productId
}

const getUserName = (userId) => {
  const user = users.value.find(u => u.id === userId)
  return user ? user.name : userId
}

const getProductDailyPrice = (productId) => {
  const product = products.value.find(p => p.id === productId)
  return product ? product.dailyPrice : 0
}

const getStatusType = (status) => {
  const map = {
    PENDING: 'info',
    CONFIRMED: 'primary',
    RENTING: 'warning',
    RETURNED: 'success',
    CANCELLED: 'info',
    OVERDUE: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    PENDING: '待确认',
    CONFIRMED: '已确认',
    RENTING: '租赁中',
    RETURNED: '已归还',
    CANCELLED: '已取消',
    OVERDUE: '已逾期'
  }
  return map[status] || status
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const openCreateOrder = () => {
  orderForm.value = {
    userId: '',
    productId: '',
    quantity: 1,
    startTime: '',
    endTime: ''
  }
  availableCount.value = null
  orderDays.value = 0
  showCreateOrder.value = true
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

      const startDate = new Date(orderForm.value.startTime)
      const endDate = new Date(orderForm.value.endTime)
      orderDays.value = Math.max(0, Math.ceil((endDate - startDate) / (1000 * 60 * 60 * 24)) + 1)
    } catch (e) {
      console.error(e)
    }
  } else {
    availableCount.value = null
    orderDays.value = 0
  }
}

const submitOrder = async () => {
  if (!orderForm.value.userId || !orderForm.value.productId || !orderForm.value.startTime || !orderForm.value.endTime) {
    ElMessage.warning('请填写完整信息')
    return
  }

  if (availableCount.value !== null && availableCount.value < orderForm.value.quantity) {
    ElMessage.error(`库存不足，当前仅可用 ${availableCount.value} 件`)
    return
  }

  submitting.value = true
  try {
    await orderApi.create(orderForm.value)
    ElMessage.success('订单创建成功')
    showCreateOrder.value = false
    fetchOrders()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '创建失败')
  } finally {
    submitting.value = false
  }
}

const calculateRefundRatio = (hoursUntilStart) => {
  if (hoursUntilStart >= 72) {
    return 1.0
  } else if (hoursUntilStart >= 48) {
    return 0.8
  } else if (hoursUntilStart >= 24) {
    return 0.5
  } else {
    return 0.0
  }
}

const cancelOrder = (row) => {
  cancelOrderInfo.value = {
    id: row.id,
    productId: row.productId,
    quantity: row.quantity,
    totalAmount: row.totalAmount,
    startTime: row.startTime
  }

  const now = new Date()
  const startTime = new Date(row.startTime)
  const hoursUntilStart = Math.max(0, Math.ceil((startTime - now) / (1000 * 60 * 60)))

  cancelRefundInfo.value = {
    hoursUntilStart,
    refundRatio: calculateRefundRatio(hoursUntilStart)
  }

  showCancel.value = true
}

const confirmCancelOrder = async () => {
  canceling.value = true
  try {
    await orderApi.cancel(cancelOrderInfo.value.id)
    ElMessage.success('订单已取消')
    showCancel.value = false
    fetchOrders()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '取消失败')
  } finally {
    canceling.value = false
  }
}

const showRenewDialog = (row) => {
  currentOrderId.value = row.id
  currentOrder.value = {
    productId: row.productId,
    quantity: row.quantity,
    startTime: row.startTime,
    endTime: row.endTime,
    dailyPrice: row.dailyPrice
  }
  renewForm.value.newEndTime = ''
  renewStockCheck.value = { available: false, availableCount: 0 }
  renewDays.value = 0
  showRenew.value = true
}

const checkRenewStock = async () => {
  if (!currentOrder.value.productId || !renewForm.value.newEndTime) {
    renewStockCheck.value = { available: false, availableCount: 0 }
    renewDays.value = 0
    return
  }

  try {
    const res = await productApi.checkAvailability(
      currentOrder.value.productId,
      currentOrder.value.endTime,
      renewForm.value.newEndTime
    )
    renewStockCheck.value = {
      available: true,
      availableCount: res.data.data.availableCount
    }

    const endDate = new Date(renewForm.value.newEndTime)
    const startDate = new Date(currentOrder.value.endTime)
    renewDays.value = Math.max(0, Math.ceil((endDate - startDate) / (1000 * 60 * 60 * 24)))
  } catch (e) {
    console.error(e)
    renewStockCheck.value = { available: false, availableCount: 0 }
    renewDays.value = 0
  }
}

const submitRenew = async () => {
  if (!renewForm.value.newEndTime) {
    ElMessage.warning('请选择新的结束时间')
    return
  }

  if (renewStockCheck.value.available && renewStockCheck.value.availableCount < currentOrder.value.quantity) {
    ElMessage.error(`后续时段库存不足，当前仅可用 ${renewStockCheck.value.availableCount} 件`)
    return
  }

  renewing.value = true
  try {
    await orderApi.renew({
      orderId: currentOrderId.value,
      newEndTime: renewForm.value.newEndTime
    })
    ElMessage.success('续租成功')
    showRenew.value = false
    fetchOrders()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '续租失败')
  } finally {
    renewing.value = false
  }
}

const showReturnDialog = (row, isDamaged = false) => {
  currentOrderId.value = row.id
  returnForm.value = {
    damaged: isDamaged,
    damageNote: '',
    maintenanceDays: 3
  }
  showReturn.value = true
}

const submitReturn = async () => {
  returning.value = true
  try {
    await orderApi.return({
      orderId: currentOrderId.value,
      ...returnForm.value
    })
    ElMessage.success('归还成功')
    showReturn.value = false
    fetchOrders()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '归还失败')
  } finally {
    returning.value = false
  }
}

onMounted(() => {
  fetchOrders()
  fetchProducts()
  fetchUsers()
})
</script>