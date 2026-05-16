import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

export const productApi = {
  list: () => api.get('/products'),
  get: (id) => api.get(`/products/${id}`),
  checkAvailability: (id, startTime, endTime) =>
    api.get(`/products/${id}/availability`, { params: { startTime, endTime } })
}

export const inventoryApi = {
  list: (productId) => api.get('/inventory', { params: { productId } }),
  completeMaintenance: (id) => api.post(`/inventory/${id}/complete-maintenance`)
}

export const userApi = {
  list: () => api.get('/users'),
  get: (id) => api.get(`/users/${id}`)
}

export const orderApi = {
  list: () => api.get('/orders'),
  get: (id) => api.get(`/orders/${id}`),
  create: (data) => api.post('/orders', data),
  cancel: (id) => api.post(`/orders/${id}/cancel`),
  renew: (data) => api.post('/orders/renew', data),
  return: (data) => api.post('/orders/return', data)
}

export const alertApi = {
  getExpiring: () => api.get('/alerts/expiring'),
  getOverdue: () => api.get('/alerts/overdue'),
  getMaintenanceTimeout: () => api.get('/alerts/maintenance-timeout')
}

export const statsApi = {
  get: () => api.get('/stats')
}

export default api