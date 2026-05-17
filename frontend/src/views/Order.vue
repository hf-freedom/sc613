<template>
  <div class="order-page">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>选择桌台</span>
          </template>
          <el-select v-model="selectedTableId" placeholder="请选择桌台" style="width: 100%" @change="onTableChange">
            <el-option
              v-for="table in occupiedTables"
              :key="table.tableId"
              :label="table.tableNumber"
              :value="table.tableId"
            />
          </el-select>
        </el-card>
      </el-col>
    </el-row>

    <el-divider />

    <el-row :gutter="20" v-if="selectedTableId">
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span>菜品列表</span>
              <el-tag type="info">按制作时间排序</el-tag>
            </div>
          </template>
          <el-row :gutter="15">
            <el-col :span="8" v-for="dish in sortedDishes" :key="dish.dishId">
              <div class="dish-item" :class="{ 'out-of-stock': dish.stock === 0 }">
                <div class="dish-header">
                  <div class="dish-name">{{ dish.name }}</div>
                  <el-tag 
                    :type="dish.cookingTimeMinutes <= 10 ? 'success' : dish.cookingTimeMinutes <= 20 ? 'warning' : 'danger'"
                    size="small"
                  >
                    {{ dish.cookingTimeMinutes }}分钟
                  </el-tag>
                </div>
                <div class="dish-price">
                  <span class="price">¥{{ dish.price }}</span>
                  <span class="stock" :class="{ 'low-stock': dish.stock <= 5 }">
                    库存: {{ dish.stock }}
                  </span>
                </div>
                <div class="dish-time-bar">
                  <div 
                    class="time-progress" 
                    :style="{ width: Math.min(dish.cookingTimeMinutes / 30 * 100, 100) + '%' }"
                  ></div>
                </div>
                <div class="dish-action">
                  <el-button-group>
                    <el-button 
                      size="small" 
                      :disabled="dish.stock === 0 || dishQuantities[dish.dishId] <= 0"
                      @click="decreaseQuantity(dish.dishId)"
                    >
                      -
                    </el-button>
                    <el-button size="small" disabled style="min-width: 40px">
                      {{ dishQuantities[dish.dishId] || 0 }}
                    </el-button>
                    <el-button 
                      size="small" 
                      type="primary"
                      :disabled="dish.stock === 0 || dishQuantities[dish.dishId] >= dish.stock"
                      @click="increaseQuantity(dish.dishId)"
                    >
                      +
                    </el-button>
                  </el-button-group>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header>
            <span>已点菜品</span>
          </template>
          <div v-if="selectedDishes.length === 0" style="text-align: center; color: #909399; padding: 40px">
            <el-icon size="48" style="margin-bottom: 10px"><ShoppingCart /></el-icon>
            <div>暂无已点菜品</div>
            <div style="font-size: 12px; margin-top: 5px">请从左侧选择菜品</div>
          </div>
          <div v-else>
            <div v-for="item in selectedDishes" :key="item.dishId" class="selected-dish">
              <div class="dish-detail">
                <span class="dish-name">{{ item.name }}</span>
                <div class="dish-meta">
                  <el-tag size="mini" type="info">{{ getDishCookingTime(item.dishId) }}分钟</el-tag>
                  <span class="quantity">x{{ item.quantity }}</span>
                </div>
              </div>
              <span class="item-price">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
            </div>
            <el-divider />
            
            <div class="order-summary">
              <div class="summary-item">
                <span>菜品数量:</span>
                <span>{{ totalQuantity }} 份</span>
              </div>
              <div class="summary-item">
                <span>预计制作时间:</span>
                <el-tag type="warning">{{ estimatedMaxTime }} 分钟</el-tag>
              </div>
              <div class="summary-item total">
                <span>总计:</span>
                <span>¥{{ totalPrice.toFixed(2) }}</span>
              </div>
            </div>
            
            <el-button
              type="primary"
              size="large"
              style="width: 100%; margin-top: 15px"
              @click="submitOrder"
              :disabled="selectedDishes.length === 0 || submitting"
              :loading="submitting"
            >
              <el-icon style="margin-right: 5px"><Check /></el-icon>
              提交订单并生成备餐队列
            </el-button>
          </div>

          <div v-if="currentOrder" style="margin-top: 20px">
            <el-divider />
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px">
              <h4 style="margin: 0">备餐队列</h4>
              <el-button 
                type="primary" 
                size="small" 
                @click="goToKitchen"
              >
                <el-icon style="margin-right: 5px"><Setting /></el-icon>
                后厨管理
              </el-button>
            </div>
            <el-tag v-if="currentOrder.allDishesCompleted" type="success" style="margin-bottom: 10px">
              全部菜品已完成
            </el-tag>
            <el-tag v-else type="warning" style="margin-bottom: 10px">
              制作中...
            </el-tag>
            <div v-for="item in sortedOrderItems" :key="item.orderItemId" class="order-item">
              <div class="item-left">
                <span class="item-name">{{ item.dishName }}</span>
                <span class="item-quantity">x{{ item.quantity }}</span>
                <el-tag size="mini" type="info">{{ item.cookingTimeMinutes }}分钟</el-tag>
              </div>
              <el-tag :type="getDishStatusType(item.status)" size="small">
                {{ getDishStatusText(item.status) }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart, Check, Setting } from '@element-plus/icons-vue'
import api from '../api'

const route = useRoute()
const router = useRouter()

const selectedTableId = ref('')
const dishes = ref([])
const dishQuantities = ref({})
const currentOrder = ref(null)
const occupiedTables = ref([])
const submitting = ref(false)

const sortedDishes = computed(() => {
  return [...dishes.value].sort((a, b) => a.cookingTimeMinutes - b.cookingTimeMinutes)
})

const selectedDishes = computed(() => {
  return dishes.value
    .filter(dish => dishQuantities.value[dish.dishId] > 0)
    .map(dish => ({
      dishId: dish.dishId,
      name: dish.name,
      price: dish.price,
      quantity: dishQuantities.value[dish.dishId],
      cookingTimeMinutes: dish.cookingTimeMinutes
    }))
})

const totalPrice = computed(() => {
  return selectedDishes.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

const totalQuantity = computed(() => {
  return selectedDishes.value.reduce((sum, item) => sum + item.quantity, 0)
})

const estimatedMaxTime = computed(() => {
  if (selectedDishes.value.length === 0) return 0
  return Math.max(...selectedDishes.value.map(item => item.cookingTimeMinutes))
})

const sortedOrderItems = computed(() => {
  if (!currentOrder.value) return []
  return [...currentOrder.value.items].sort((a, b) => {
    const statusOrder = { PENDING: 0, COOKING: 1, COMPLETED: 2, CANCELLED: 3 }
    return statusOrder[a.status] - statusOrder[b.status] || a.cookingTimeMinutes - b.cookingTimeMinutes
  })
})

const getDishStatusText = (status) => {
  const texts = {
    PENDING: '待制作',
    COOKING: '制作中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return texts[status] || status
}

const getDishStatusType = (status) => {
  const types = {
    PENDING: 'info',
    COOKING: 'warning',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return types[status] || 'info'
}

const getDishCookingTime = (dishId) => {
  const dish = dishes.value.find(d => d.dishId === dishId)
  return dish ? dish.cookingTimeMinutes : 0
}

const increaseQuantity = (dishId) => {
  const dish = dishes.value.find(d => d.dishId === dishId)
  if (dish && dishQuantities.value[dishId] < dish.stock) {
    dishQuantities.value[dishId] = (dishQuantities.value[dishId] || 0) + 1
  }
}

const decreaseQuantity = (dishId) => {
  if (dishQuantities.value[dishId] > 0) {
    dishQuantities.value[dishId]--
  }
}

const goToKitchen = () => {
  router.push('/kitchen')
}

const onTableChange = async () => {
  if (selectedTableId.value) {
    try {
      const res = await api.get(`/order/byTable/${selectedTableId.value}`)
      if (res.data.code === 200 && res.data.data) {
        currentOrder.value = res.data.data
      } else {
        currentOrder.value = null
      }
    } catch (e) {
      currentOrder.value = null
    }
  }
}

const submitOrder = async () => {
  if (!selectedTableId.value) {
    ElMessage.warning('请先选择桌台')
    return
  }
  if (selectedDishes.value.length === 0) {
    ElMessage.warning('请选择菜品')
    return
  }

  submitting.value = true
  const orderItems = selectedDishes.value.map(item => ({
    dishId: item.dishId,
    quantity: item.quantity
  }))

  try {
    const res = await api.post('/order/create', orderItems, {
      params: { tableId: selectedTableId.value }
    })
    if (res.data.code === 200) {
      ElMessage.success({
        message: '订单提交成功，备餐队列已生成',
        duration: 3000
      })
      currentOrder.value = res.data.data
      for (const dishId in dishQuantities.value) {
        dishQuantities.value[dishId] = 0
      }
      loadDishes()
    } else {
      ElMessage.error('订单提交失败')
    }
  } catch (e) {
    ElMessage.error('订单提交失败')
  } finally {
    submitting.value = false
  }
}

const loadDishes = async () => {
  try {
    const res = await api.get('/dish/list')
    if (res.data.code === 200) {
      dishes.value = res.data.data
      res.data.data.forEach(dish => {
        if (!(dish.dishId in dishQuantities.value)) {
          dishQuantities.value[dish.dishId] = 0
        }
      })
    }
  } catch (e) {
    console.error('加载菜品失败', e)
  }
}

const loadOccupiedTables = async () => {
  try {
    const res = await api.get('/table/list')
    if (res.data.code === 200) {
      occupiedTables.value = res.data.data.filter(t => t.status === 'OCCUPIED')
    }
  } catch (e) {
    console.error('加载桌台失败', e)
  }
}

onMounted(() => {
  if (route.query.tableId) {
    selectedTableId.value = route.query.tableId
  }
  loadDishes()
  loadOccupiedTables()
  setInterval(() => {
    loadDishes()
    loadOccupiedTables()
    if (selectedTableId.value) {
      onTableChange()
    }
  }, 10000)
})
</script>

<style scoped>
.order-page {
  padding: 0;
}
.dish-item {
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 15px;
  transition: all 0.3s;
}
.dish-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border-color: #409eff;
}
.dish-item.out-of-stock {
  opacity: 0.5;
  background: #f5f7fa;
}
.dish-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.dish-name {
  font-weight: bold;
  font-size: 16px;
}
.dish-price {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.price {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}
.stock {
  color: #909399;
  font-size: 12px;
}
.stock.low-stock {
  color: #e6a23c;
  font-weight: bold;
}
.dish-time-bar {
  height: 6px;
  background: #ebeef5;
  border-radius: 3px;
  margin-bottom: 12px;
  overflow: hidden;
}
.time-progress {
  height: 100%;
  background: linear-gradient(90deg, #67c23a, #e6a23c, #f56c6c);
  border-radius: 3px;
  transition: width 0.3s;
}
.dish-action {
  display: flex;
  justify-content: flex-end;
}
.selected-dish {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.selected-dish:last-child {
  border-bottom: none;
}
.dish-detail {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.dish-detail .dish-name {
  font-size: 14px;
  margin: 0;
}
.dish-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}
.quantity {
  font-size: 14px;
  color: #909399;
}
.item-price {
  font-weight: bold;
  color: #409eff;
  font-size: 16px;
}
.order-summary {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 6px;
}
.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  font-size: 14px;
}
.summary-item.total {
  font-size: 18px;
  font-weight: bold;
  padding-top: 10px;
  border-top: 1px solid #e4e7ed;
  margin-top: 6px;
}
.summary-item.total span:last-child {
  color: #f56c6c;
}
.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}
.order-item:last-child {
  border-bottom: none;
}
.item-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.item-name {
  font-weight: 500;
}
.item-quantity {
  color: #909399;
  font-size: 14px;
}
</style>
