<template>
  <div class="statistics-page">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div class="stat-value" style="color: #409eff">{{ statistics.totalQueuesToday }}</div>
            <div class="stat-label">今日取号数量</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div class="stat-value" style="color: #67c23a">{{ statistics.completedTablesToday }}</div>
            <div class="stat-label">今日完成桌数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div class="stat-value" style="color: #e6a23c">{{ statistics.averageWaitTimeMinutes.toFixed(2) }}</div>
            <div class="stat-label">平均等待时间(分钟)</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div class="stat-value" style="color: #f56c6c">{{ (statistics.dishTimeoutRate * 100).toFixed(1) }}%</div>
            <div class="stat-label">菜品超时率</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-divider />

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>翻台率分析</span>
          </template>
          <div style="padding: 20px; text-align: center">
            <div style="font-size: 48px; font-weight: bold; color: #67c23a">
              {{ (statistics.tableTurnoverRate * 100).toFixed(1) }}%
            </div>
            <div style="color: #909399; margin-top: 10px">
              完成桌数 / 总取号数
            </div>
            <el-progress
              :percentage="statistics.tableTurnoverRate * 100"
              :stroke-width="20"
              style="margin-top: 20px"
            />
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>今日菜品统计</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="总出菜品数">
              <span style="color: #409eff; font-weight: bold">{{ statistics.totalDishesToday }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="超时菜品数">
              <span style="color: #f56c6c; font-weight: bold">{{ statistics.timeoutDishesToday }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="按时完成菜品数">
              <span style="color: #67c23a; font-weight: bold">
                {{ statistics.totalDishesToday - statistics.timeoutDishesToday }}
              </span>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-divider />

    <el-card shadow="hover">
      <template #header>
        <span>操作</span>
      </template>
      <el-button type="danger" @click="resetStatistics">重置今日统计数据</el-button>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const statistics = ref({
  averageWaitTimeMinutes: 0,
  tableTurnoverRate: 0,
  dishTimeoutRate: 0,
  totalQueuesToday: 0,
  completedTablesToday: 0,
  totalDishesToday: 0,
  timeoutDishesToday: 0,
  totalWaitTimeSeconds: 0
})

const resetStatistics = async () => {
  try {
    await ElMessageBox.confirm('确定要重置今日所有统计数据吗?', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await api.post('/statistics/reset')
    if (res.data.code === 200) {
      ElMessage.success('统计数据已重置')
      loadStatistics()
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const loadStatistics = async () => {
  try {
    const res = await api.get('/statistics')
    if (res.data.code === 200) {
      statistics.value = res.data.data
    }
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
}

onMounted(() => {
  loadStatistics()
  setInterval(loadStatistics, 10000)
})
</script>

<style scoped>
.statistics-page {
  padding: 0;
}
.stat-value {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 10px;
}
.stat-label {
  color: #909399;
  font-size: 14px;
}
</style>
