import { createRouter, createWebHistory } from 'vue-router'
import Queue from '../views/Queue.vue'
import Tables from '../views/Tables.vue'
import Order from '../views/Order.vue'
import Kitchen from '../views/Kitchen.vue'
import Statistics from '../views/Statistics.vue'

const routes = [
  { path: '/', redirect: '/queue' },
  { path: '/queue', component: Queue, name: '排队取号' },
  { path: '/tables', component: Tables, name: '桌台管理' },
  { path: '/order', component: Order, name: '点餐管理' },
  { path: '/kitchen', component: Kitchen, name: '后厨管理' },
  { path: '/statistics', component: Statistics, name: '统计报表' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
