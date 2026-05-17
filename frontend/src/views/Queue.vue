<template>
  <div class="queue-page">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <span>顾客取号</span>
          </template>
          <el-form :model="queueForm" label-width="80px">
            <el-form-item label="用餐人数">
              <el-input-number v-model="queueForm.peopleCount" :min="1" :max="20" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="takeQueue" style="width: 100%">取号</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <span>前台叫号</span>
          </template>
          <el-form label-width="80px">
            <el-form-item label="桌台类型">
              <el-select v-model="callTableType" placeholder="请选择">
                <el-option label="小桌 (1-2人)" value="SMALL" />
                <el-option label="中桌 (3-4人)" value="MEDIUM" />
                <el-option label="大桌 (5-6人)" value="LARGE" />
                <el-option label="超大桌 (7-10人)" value="EXTRA_LARGE" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="danger" @click="callQueue" style="width: 100%">叫下一位</el-button>
            </el-form-item>
          </el-form>
          <div v-if="currentCalled" class="called-card">
            <el-alert
              :title="getCountdownTitle()"
              type="warning"
              :closable="false"
              show-icon
              style="margin-bottom: 15px"
            >
              <template #default>
                <div class="countdown-display">
                  <span class="countdown-number">{{ countdownSeconds }}</span>
                  <span class="countdown-unit">秒</span>
                </div>
              </template>
            </el-alert>
            
            <div class="queue-number-display">
              <div class="label">当前叫号</div>
              <div class="number">{{ currentCalled.queueNumber }}</div>
            </div>
            
            <div class="queue-info-display">
              <div class="info-item">
                <span class="info-label">桌台类型:</span>
                <span class="info-value">{{ getTypeName(currentCalled.tableType) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">用餐人数:</span>
                <span class="info-value">{{ currentCalled.peopleCount }} 人</span>
              </div>
            </div>
            
            <el-button 
              type="success" 
              size="large" 
              @click="confirmQueue" 
              style="width: 100%; margin-top: 15px"
            >
              <el-icon style="margin-right: 5px"><Check /></el-icon>
              顾客确认入座
            </el-button>
            
            <el-button 
              type="info" 
              @click="skipCurrent" 
              style="width: 100%; margin-top: 8px"
            >
              顾客不在，过号
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-divider />

    <el-row :gutter="20">
      <el-col :span="18">
        <h3 style="margin-bottom: 15px">排队列表</h3>
        <el-row :gutter="20">
          <el-col :span="6" v-for="(queues, type) in queueByType" :key="type">
            <el-card shadow="hover">
              <template #header>
                <span>{{ getTypeName(type) }}</span>
                <span style="float: right; color: #909399">等待: {{ queues.length }} 桌</span>
              </template>
              <div v-if="queues.length === 0" style="text-align: center; color: #909399; padding: 20px">
                暂无排队
              </div>
              <div v-else>
                <div 
                  v-for="item in queues" 
                  :key="item.queueId" 
                  class="queue-item"
                  :class="{ 'calling-item': currentCalled && currentCalled.queueId === item.queueId }"
                >
                  <div>
                    <span class="queue-number">{{ item.queueNumber }}</span>
                    <el-tag 
                      v-if="currentCalled && currentCalled.queueId === item.queueId" 
                      type="warning" 
                      size="small"
                      style="margin-left: 8px"
                    >
                      正在叫号
                    </el-tag>
                  </div>
                  <div class="queue-info">{{ item.peopleCount }} 人</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-col>
      <el-col :span="6">
        <h3 style="margin-bottom: 15px">过号记录</h3>
        <el-card shadow="hover">
          <div v-if="skipHistory.length === 0" style="text-align: center; color: #909399; padding: 40px 0">
            暂无过号记录
          </div>
          <div v-else>
            <div 
              v-for="(item, index) in skipHistory" 
              :key="index"
              class="skip-history-item"
            >
              <div class="skip-number">{{ item.queueNumber }}</div>
              <div class="skip-info">
                <el-tag size="mini" :type="item.reason === '自动超时' ? 'danger' : 'info'">
                  {{ item.reason }}
                </el-tag>
                <span class="skip-time">{{ item.skipTime }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import api from '../api'

const queueForm = ref({
  peopleCount: 2
})

const callTableType = ref('')
const currentCalled = ref(null)
const queueByType = ref({})
const countdownSeconds = ref(180)
const skipHistory = ref([])
let countdownTimer = null

const getTypeName = (type) => {
  const names = {
    SMALL: '小桌 (1-2人)',
    MEDIUM: '中桌 (3-4人)',
    LARGE: '大桌 (5-6人)',
    EXTRA_LARGE: '超大桌 (7-10人)'
  }
  return names[type] || type
}

const takeQueue = async () => {
  try {
    const res = await api.post('/queue/take', null, {
      params: { peopleCount: queueForm.value.peopleCount }
    })
    if (res.data.code === 200) {
      ElMessage.success(`取号成功! 您的号码是: ${res.data.data.queueNumber}`)
      loadQueueData()
    }
  } catch (e) {
    ElMessage.error('取号失败')
  }
}

const getCountdownTitle = () => {
  if (countdownSeconds.value > 120) {
    return '请顾客尽快确认入座'
  } else if (countdownSeconds.value > 60) {
    return '⚠️ 剩余时间不足2分钟，请尽快确认'
  } else {
    return '🚨 即将超时，请立即确认！'
  }
}

const startCountdown = () => {
  stopCountdown()
  countdownSeconds.value = 180
  countdownTimer = setInterval(() => {
    countdownSeconds.value--
    if (countdownSeconds.value <= 0) {
      handleTimeout()
    }
  }, 1000)
}

const stopCountdown = () => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

const handleTimeout = async () => {
  stopCountdown()
  ElMessage.warning(`顾客 ${currentCalled.value.queueNumber} 已超时，自动过号`)
  currentCalled.value = null
  loadQueueData()
}

const skipCurrent = async () => {
  try {
    await ElMessageBox.confirm('确定跳过当前顾客，叫下一位吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const skippedItem = {
      queueNumber: currentCalled.value.queueNumber,
      skipTime: new Date().toLocaleTimeString(),
      reason: '手动过号'
    }
    skipHistory.value.unshift(skippedItem)
    if (skipHistory.value.length > 10) {
      skipHistory.value.pop()
    }
    stopCountdown()
    currentCalled.value = null
    loadQueueData()
    ElMessage.info('已过号')
  } catch (e) {
    // 用户取消
  }
}

const callQueue = async () => {
  if (!callTableType.value) {
    ElMessage.warning('请先选择桌台类型')
    return
  }
  try {
    const res = await api.post('/queue/call', null, {
      params: { tableType: callTableType.value }
    })
    if (res.data.code === 200) {
      currentCalled.value = res.data.data
      startCountdown()
      ElMessage.success(`已叫号: ${res.data.data.queueNumber}`)
      loadQueueData()
    } else {
      ElMessage.warning(res.data.message)
    }
  } catch (e) {
    ElMessage.error('叫号失败')
  }
}

const confirmQueue = async () => {
  if (!currentCalled.value) return
  try {
    const res = await api.post('/queue/confirm', null, {
      params: { queueId: currentCalled.value.queueId }
    })
    if (res.data.code === 200 && res.data.data) {
      stopCountdown()
      ElMessage.success('确认入座成功，已分配桌台')
      currentCalled.value = null
      loadQueueData()
    } else {
      stopCountdown()
      ElMessage.error('确认失败，可能已超时')
      currentCalled.value = null
    }
  } catch (e) {
    ElMessage.error('确认失败')
  }
}

const loadQueueData = async () => {
  try {
    const res = await api.get('/queue/byType')
    if (res.data.code === 200) {
      queueByType.value = res.data.data
      
      // 检测当前叫号是否已被后端标记为过号
      if (currentCalled.value) {
        let stillCalling = false
        for (const type in res.data.data) {
          const found = res.data.data[type].find(
            item => item.queueId === currentCalled.value.queueId
          )
          if (found) {
            stillCalling = true
            break
          }
        }
        
        // 如果不在排队列表中，说明已被后端自动过号
        if (!stillCalling) {
          handleBackendTimeout()
        }
      }
    }
  } catch (e) {
    console.error('加载排队数据失败', e)
  }
}

const handleBackendTimeout = () => {
  if (currentCalled.value) {
    const skippedItem = {
      queueNumber: currentCalled.value.queueNumber,
      skipTime: new Date().toLocaleTimeString(),
      reason: '自动超时'
    }
    skipHistory.value.unshift(skippedItem)
    if (skipHistory.value.length > 10) {
      skipHistory.value.pop()
    }
    stopCountdown()
    ElMessage.warning(`顾客 ${currentCalled.value.queueNumber} 已超时，系统自动过号`)
    currentCalled.value = null
  }
}

let refreshTimer = null

onMounted(() => {
  loadQueueData()
  refreshTimer = setInterval(loadQueueData, 10000)
})

onUnmounted(() => {
  stopCountdown()
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.queue-page {
  padding: 0;
}
.queue-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #f0f0f0;
}
.queue-item:last-child {
  border-bottom: none;
}
.queue-number {
  font-weight: bold;
  font-size: 16px;
  color: #409eff;
}
.queue-info {
  color: #909399;
}
.called-card {
  margin-top: 15px;
  padding: 15px;
  background: linear-gradient(135deg, #fff7e6 0%, #fff2e8 100%);
  border-radius: 8px;
  border: 1px solid #ffd591;
}
.countdown-display {
  display: flex;
  align-items: baseline;
  justify-content: center;
}
.countdown-number {
  font-size: 48px;
  font-weight: bold;
  color: #fa8c16;
  line-height: 1;
}
.countdown-unit {
  font-size: 16px;
  color: #fa8c16;
  margin-left: 5px;
}
.queue-number-display {
  text-align: center;
  padding: 20px 0;
  background: white;
  border-radius: 6px;
  margin-bottom: 15px;
}
.queue-number-display .label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}
.queue-number-display .number {
  font-size: 36px;
  font-weight: bold;
  color: #e6a23c;
}
.queue-info-display {
  background: white;
  border-radius: 6px;
  padding: 10px 15px;
}
.info-item {
  display: flex;
  justify-content: space-between;
  padding: 5px 0;
}
.info-item:not(:last-child) {
  border-bottom: 1px solid #f0f0f0;
}
.info-label {
  color: #909399;
}
.info-value {
  font-weight: 500;
  color: #303133;
}
.calling-item {
  background: #fff7e6;
  border-radius: 4px;
  margin: 5px 0;
}
.skip-history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}
.skip-history-item:last-child {
  border-bottom: none;
}
.skip-number {
  font-weight: bold;
  color: #f56c6c;
  font-size: 16px;
}
.skip-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}
.skip-time {
  font-size: 12px;
  color: #909399;
}
</style>
