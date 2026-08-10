<script setup lang="ts">
// 根组件：只负责整体布局，各业务页面由路由渲染到 <router-view />
</script>

<template>
  <el-container style="min-height: 100vh">
    <!-- 顶部导航栏：系统标题 + 横向菜单，菜单 index 与路由路径一一对应 -->
    <el-header style="background: #409eff; color: white; display: flex; align-items: center; padding: 0 20px">
      <h2 style="margin: 0">WMS 仓储管理系统</h2>
      <el-menu
        mode="horizontal"
        :default-active="$route.path"
        router
        style="margin-left: 40px; border-bottom: none; flex: 1"
        background-color="#409eff"
        text-color="#fff"
        active-text-color="#ffd04b"
      >
        <el-menu-item index="/products">商品管理</el-menu-item>
        <el-menu-item index="/inventory">库存查询</el-menu-item>
        <el-menu-item index="/inbound">入库管理</el-menu-item>
        <el-menu-item index="/outbound">出库管理</el-menu-item>
      </el-menu>
    </el-header>

    <!-- 主内容区：显示当前路由对应的页面组件 -->
    <el-main style="padding: 20px">
      <router-view />
    </el-main>
  </el-container>
</template>

<style>
body {
  margin: 0;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', Arial, sans-serif;
}
</style>
