<template>
  <div class="tables-page">
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6">
        <el-statistic title="空闲桌台" :value="availableCount" value-style="color: #67c23a" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="用餐中" :value="occupiedCount" value-style="color: #f56c6c" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="待清理" :value="cleaningCount" value-style="color: #e6a23c" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="总桌台" :value="tables.length" value-style="color: #409eff" />
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="6" v-for="table in tables" :key="table.tableId">
        <el-card shadow="hover" :class="['table-card', getStatusClass(table.status)]">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span class="table-number">{{ table.tableNumber }}</span>
              <el-tag :type="getStatusTagType(table.status)" size="small">
                {{ getStatusText(table.status) }}
              </el-tag>
            </div>
          </template>
          
          <div class="table-info">
            <div class="info-row">
              <el-icon><OfficeBuilding /></el-icon>
              <span class="label">桌台类型:</span>
              <span class="value">{{ getTableTypeName(table.tableType) }}</span>
            </div>
            
            <div v-if="table.status === 'OCCUPIED' && table.occupyTime" class="info-row">
              <el-icon><Clock /></el-icon>
              <span class="label">入座时间:</span>
              <span class="value">{{ formatTime(table.occupyTime) }}</span>
            </div>
            
            <div v-if="table.status === 'CLEANING' && table.checkoutTime" class="info-row">
              <el-icon><Timer /></el-icon>
              <span class="label">结账时间:</span>
              <span class="value">{{ formatTime(table.checkoutTime) }}</span>
            </div>
            
            <div v-if="table.currentQueueId" class="info-row">
              <el-icon><User /></el-icon>
              <span class="label">取号ID:</span>
              <span class="value short">{{ table.currentQueueId.slice(0, 8) }}...</span>
            </div>
          </div>

          <div v-if="table.status === 'OCCUPIED'" class="action-buttons">
            <el-button 
              type="primary" 
              size="small" 
              @click="goToOrder(table)" 
              style="width: 100%; margin-bottom: 8px"
            >
              <el-icon style="margin-right: 5px"><Edit /></el-icon>
              点餐管理
            </el-button>
            <el-button 
              type="success" 
              size="small" 
              @click="checkout(table)" 
              style="width: 100%"
            >
              <el-icon style="margin-right: 5px"><Check /></el-icon>
              结账
            </el-button>
          </div>

          <div v-if="table.status === 'CLEANING'" class="action-buttons">
            <div class="cleaning-alert">
              <el-alert
                title="待清理"
                type="warning"
                :closable="false"
                show-icon
                style="margin-bottom: 12px"
              >
                <template #default>
                  顾客已结账，请及时清理桌台
                </template>
              </el-alert>
            </div>
            <el-button 
              type="warning" 
              size="large" 
              @click="cleanTable(table)" 
              style="width: 100%"
            >
              <el-icon style="margin-right: 5px"><CircleCheck /></el-icon>
              清理完成
            </el-button>
          </div>

          <div v-if="table.status === 'AVAILABLE'" class="action-buttons">
            <div class="available-badge">
              <el-icon size="48" color="#67c23a"><SuccessFilled /></el-icon>
              <div style="color: #67c23a; margin-top: 8px">空闲可用</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  OfficeBuilding, 
  Clock, 
  Timer, 
  User, 
  Edit, 
  Check, 
  CircleCheck,
  SuccessFilled
} from '@element-plus/icons-vue'
import api from '../api'

const router = useRouter()
const tables = ref([])

const availableCount = computed(() => {
  return tables.value.filter(t => t.status === 'AVAILABLE').length
})

const occupiedCount = computed(() => {
  return tables.value.filter(t => t.status === 'OCCUPIED').length
})

const cleaningCount = computed(() => {
  return tables.value.filter(t => t.status === 'CLEANING').length
})

const getStatusText = (status) => {
  const texts = {
    AVAILABLE: '空闲',
    OCCUPIED: '用餐中',
    CLEANING: '待清理'
  }
  return texts[status] || status
}

const getStatusTagType = (status) => {
  const types = {
    AVAILABLE: 'success',
    OCCUPIED: 'danger',
    CLEANING: 'warning'
  }
  return types[status] || 'info'
}

const getStatusClass = (status) => {
  return 'status-' + status.toLowerCase()
}

const getTableTypeName = (type) => {
  const names = {
    SMALL: '小桌',
    MEDIUM: '中桌',
    LARGE: '大桌',
    EXTRA_LARGE: '超大桌'
  }
  return names[type] || type
}

const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  return date.toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

const goToOrder = (table) => {
  router.push({
    path: '/order',
    query: { tableId: table.tableId }
  })
}

const checkout = async (table) => {
  try {
    await ElMessageBox.confirm(`确认对桌台 ${table.tableNumber} 进行结账?`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await api.get(`/order/byTable/${table.tableId}`)
    if (res.data.code === 200 && res.data.data) {
      const payRes = await api.post('/order/pay', null, {
        params: { orderId: res.data.data.orderId }
      })
      if (payRes.data.code === 200 && payRes.data.data) {
        ElMessage.success('结账成功')
        loadTables()
      } else {
        ElMessage.error('结账失败')
      }
    } else {
      ElMessage.warning('该桌台暂无订单')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const cleanTable = async (table) => {
  try {
    await ElMessageBox.confirm(
      `确认桌台 ${table.tableNumber} 已清理完成?\n清理后桌台将变为空闲状态，可重新安排顾客入座。`, 
      '清理确认', 
      {
        confirmButtonText: '确认清理完成',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--warning'
      }
    )
    
    const res = await api.post('/table/clean', null, {
      params: { tableId: table.tableId }
    })
    if (res.data.code === 200 && res.data.data) {
      ElMessage.success({
        message: `桌台 ${table.tableNumber} 已清理完成，已释放重新使用`,
        duration: 3000
      })
      loadTables()
    } else {
      ElMessage.error('操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const loadTables = async () => {
  try {
    const res = await api.get('/table/list')
    if (res.data.code === 200) {
      tables.value = res.data.data
    }
  } catch (e) {
    console.error('加载桌台数据失败', e)
  }
}

onMounted(() => {
  loadTables()
  setInterval(loadTables, 10000)
})
</script>

<style scoped>
.tables-page {
  padding: 0;
}
.table-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}
.table-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}
.table-number {
  font-weight: bold;
  font-size: 16px;
}
.status-occupied {
  border-left: 5px solid #f56c6c;
}
.status-available {
  border-left: 5px solid #67c23a;
}
.status-cleaning {
  border-left: 5px solid #e6a23c;
  animation: pulse-warning 2s infinite;
}
@keyframes pulse-warning {
  0%, 100% {
    box-shadow: 0 2px 12px rgba(230, 162, 60, 0.2);
  }
  50% {
    box-shadow: 0 2px 20px rgba(230, 162, 60, 0.4);
  }
}
.table-info {
  margin-bottom: 15px;
}
.info-row {
  display: flex;
  align-items: center;
  padding: 6px 0;
  font-size: 13px;
}
.info-row .label {
  color: #909399;
  margin-left: 6px;
  margin-right: 6px;
}
.info-row .value {
  color: #303133;
  font-weight: 500;
}
.info-row .value.short {
  font-family: monospace;
}
.action-buttons {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}
.cleaning-alert {
  margin-bottom: 10px;
}
.cleaning-alert :deep(.el-alert__description) {
  font-size: 12px;
}
.available-badge {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 15px 0;
}
</style>
