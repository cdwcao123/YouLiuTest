<script setup lang="ts">
/**
 * 库存查询页 — 任务 2
 * - 商品名称/SKU 模糊搜索（300ms 防抖）+ 仓库筛选 + 库位筛选
 * - 后端分页
 * - 库存数量 < 10 的行红色高亮
 */
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getInventory, getWarehouses, type Warehouse, type InventoryItem } from '@/api'
import { buildInventoryQuery, debounce, getInventoryRowStyle } from '@/utils/inventory'

const keyword = ref('')
const warehouseId = ref<number>()
const locationCode = ref('')
const warehouses = ref<Warehouse[]>([])
const loading = ref(false)
const inventoryList = ref<InventoryItem[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)

const loadInventory = async () => {
  loading.value = true
  try {
    const params = buildInventoryQuery(
      keyword.value,
      warehouseId.value,
      locationCode.value || undefined,
      page.value,
      pageSize.value,
    )
    const res = await getInventory(params)
    inventoryList.value = res.data.list
    total.value = res.data.total
  } catch (e: any) {
    ElMessage.error('加载失败: ' + (e.response?.data?.message || e.message))
  } finally {
    loading.value = false
  }
}

// 关键词输入防抖：停止输入 300ms 后自动查询
const onKeywordInput = debounce(() => {
  page.value = 1
  loadInventory()
}, 300)

const handleSearch = () => {
  page.value = 1
  loadInventory()
}

const handleReset = () => {
  keyword.value = ''
  warehouseId.value = undefined
  locationCode.value = ''
  page.value = 1
  loadInventory()
}

const handlePageChange = (p: number) => {
  page.value = p
  loadInventory()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  page.value = 1
  loadInventory()
}

onMounted(async () => {
  try {
    warehouses.value = (await getWarehouses()).data
  } catch (e: any) {
    ElMessage.error('仓库加载失败: ' + (e.response?.data?.message || e.message))
  }
  await loadInventory()
})
</script>

<template>
  <div>
    <h3>库存查询</h3>

    <!-- 搜索栏 -->
    <div style="display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap">
      <el-input
        v-model="keyword"
        placeholder="搜索商品名称/SKU..."
        style="width: 260px"
        clearable
        @input="onKeywordInput"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-select
        v-model="warehouseId"
        placeholder="选择仓库"
        clearable
        style="width: 180px"
        @change="handleSearch"
      >
        <el-option
          v-for="w in warehouses"
          :key="w.id"
          :label="w.name"
          :value="w.id"
        />
      </el-select>
      <el-input
        v-model="locationCode"
        placeholder="库位编码（可选）"
        style="width: 200px"
        clearable
        @input="onKeywordInput"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="inventoryList" v-loading="loading" border stripe :row-style="getInventoryRowStyle">
      <el-table-column prop="productName" label="商品名称" min-width="140" />
      <el-table-column prop="supplierName" label="供应商" width="120" />
      <el-table-column prop="sku" label="SKU" width="140" />
      <el-table-column prop="locationCode" label="库位编码" width="140" />
      <el-table-column prop="warehouseName" label="仓库" width="120" />
      <el-table-column prop="quantity" label="库存数量" width="110" sortable />
      <el-table-column prop="updatedAt" label="更新时间" width="180" />
    </el-table>

    <!-- 分页 -->
    <div style="margin-top: 16px; text-align: right">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <el-empty v-if="!loading && inventoryList.length === 0" description="暂无库存数据，请先完成入库操作" />
  </div>
</template>
