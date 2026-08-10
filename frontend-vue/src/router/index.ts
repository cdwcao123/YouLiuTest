import { createRouter, createWebHashHistory } from 'vue-router'

/**
 * 路由配置（hash 模式），访问地址形如：http://localhost:5173/#/products
 * 页面组件使用动态 import 实现路由懒加载，只有访问时才下载对应代码。
 */
const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      redirect: '/products', // 默认进入商品管理页
    },
    {
      path: '/products', // 商品管理（参考实现）
      name: 'Products',
      component: () => import('@/views/ProductsView.vue'),
    },
    {
      path: '/inventory', // 库存查询（任务 2）
      name: 'Inventory',
      component: () => import('@/views/InventoryView.vue'),
    },
    {
      path: '/inbound', // 入库管理（任务 1）
      name: 'Inbound',
      component: () => import('@/views/InboundView.vue'),
    },
    {
      path: '/outbound', // 出库管理（选做 A）
      name: 'Outbound',
      component: () => import('@/views/OutboundView.vue'),
    },
  ],
})

export default router
