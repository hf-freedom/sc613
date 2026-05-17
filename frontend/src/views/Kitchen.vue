<template>
  <div class="kitchen-page">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>待制作菜品</span>
          </template>
          <div v-if="pendingDishes.length === 0" style="text-align: center; color: #909399; padding: 40px">
            暂无待制作菜品
          </div>
          <div v-else>
            <div v-for="item in pendingDishes" :key="item.orderItemId" class="kitchen-item">
              <div class="item-info">
                <div class="item-name">{{ item.dishName }} x{{ item.quantity }}</div>
                <div class="item-time">预计制作时间: {{ item.cookingTimeMinutes }}分钟</div>
              </div>
              <el-button type="primary" size="small" @click="startCooking(item)">
                开始制作
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>制作中菜品</span>
          </template>
          <div v-if="cookingDishes.length === 0" style="text-align: center; color: #909399; padding: 40px">
            暂无制作中菜品
          </div>
          <div v-else>
            <div v-for="item in cookingDishes" :key="item.orderItemId" class="kitchen-item cooking">
              <div class="item-info">
                <div class="item-name">{{ item.dishName }} x{{ item.quantity }}</div>
                <div class="item-time">
                  <el-icon style="color: #e6a23c; margin-right: 5px"><Loading /></el-icon>
                  正在制作中...
                </div>
              </div>
              <el-button type="success" size="small" @click="completeDish(item)">
                制作完成
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-divider />

    <el-card shadow="hover">
      <template #header>
        <span>已完成菜品</span>
      </template>
      <div v-if="completedDishes.length === 0" style="text-align: center; color: #909399; padding: 40px">
        暂无已完成菜品
      </div>
      <div v-else>
        <el-table :data="completedDishes" stripe>
          <el-table-column prop="dishName" label="菜品名称" />
          <el-table-column prop="quantity" label="数量" />
          <el-table-column label="状态">
            <template #default>
              <el-tag type="success" size="small">已完成</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import api from '../api'

const kitchenQueue = ref([])

const pendingDishes = computed(() => {
  return kitchenQueue.value.filter(item => item.status === 'PENDING')
    .sort((a, b) => a.cookingTimeMinutes - b.cookingTimeMinutes)
})

const cookingDishes = computed(() => {
  return kitchenQueue.value.filter(item => item.status === 'COOKING')
})

const completedDishes = computed(() => {
  return kitchenQueue.value.filter(item => item.status === 'COMPLETED')
})

const startCooking = async (item) => {
  try {
    const res = await api.post('/order/startCooking', null, {
      params: {
        orderId: item.orderId,
        orderItemId: item.orderItemId
      }
    })
    if (res.data.code === 200 && res.data.data) {
      ElMessage.success(`开始制作: ${item.dishName}`)
      loadKitchenQueue()
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const completeDish = async (item) => {
  try {
    const res = await api.post('/order/completeDish', null, {
      params: {
        orderId: item.orderId,
        orderItemId: item.orderItemId
      }
    })
    if (res.data.code === 200 && res.data.data) {
      ElMessage.success(`制作完成: ${item.dishName}`)
      loadKitchenQueue()
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const loadKitchenQueue = async () => {
  try {
    const ordersRes = await api.get('/order/list')
    if (ordersRes.data.code === 200) {
      const allItems = []
      ordersRes.data.data.forEach(order => {
        order.items.forEach(item => {
          item.orderId = order.orderId
          allItems.push(item)
        })
      })
      kitchenQueue.value = allItems
    }
  } catch (e) {
    console.error('加载后厨队列失败', e)
  }
}

onMounted(() => {
  loadKitchenQueue()
  setInterval(loadKitchenQueue, 5000)
})
</script>

<style scoped>
.kitchen-page {
  padding: 0;
}
.kitchen-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 10px;
}
.kitchen-item.cooking {
  border-left: 4px solid #e6a23c;
  background: #fdf6ec;
}
.item-name {
  font-weight: bold;
  font-size: 16px;
  margin-bottom: 5px;
}
.item-time {
  color: #909399;
  font-size: 14px;
  display: flex;
  align-items: center;
}
</style>
