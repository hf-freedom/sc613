<template>
  <el-container style="height: 100vh">
    <el-aside width="200px" style="background-color: #2f4050">
      <div style="color: white; text-align: center; padding: 20px 0; font-size: 18px; font-weight: bold">
        餐厅管理系统
      </div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#2f4050"
        text-color="#a7b1c2"
        active-text-color="#fff"
      >
        <el-menu-item index="/queue">
          <el-icon><Tickets /></el-icon>
          <span>排队取号</span>
        </el-menu-item>
        <el-menu-item index="/tables">
          <el-icon><Grid /></el-icon>
          <span>桌台管理</span>
        </el-menu-item>
        <el-menu-item index="/order">
          <el-icon><Document /></el-icon>
          <span>点餐管理</span>
        </el-menu-item>
        <el-menu-item index="/kitchen">
          <el-icon><Operation /></el-icon>
          <span>后厨管理</span>
        </el-menu-item>
        <el-menu-item index="/statistics">
          <el-icon><DataAnalysis /></el-icon>
          <span>统计报表</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="background-color: #f3f3f4; border-bottom: 1px solid #e7eaec">
        <h2 style="margin: 0; line-height: 60px">{{ $route.name }}</h2>
      </el-header>
      <el-main style="background-color: #f3f3f4">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { Tickets, Grid, Document, Operation, DataAnalysis } from '@element-plus/icons-vue'
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
</style>
