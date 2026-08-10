<script setup lang="ts">
/**
 * 入库管理页 — 任务 1
 * - 供应商名称 + 多行明细
 * - 商品下拉远程搜索
 * - 仓库 → 库位级联选择
 * - 数量输入、行删除、提交
 */
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getProducts,
  getWarehouses,
  getLocations,
  createInboundOrder,
  type Product,
  type Warehouse,
  type Location,
} from '@/api'

interface InboundRow {
  /** 选中的商品 ID */
  productId?: number
  /** 入库数量 */
  quantity: number
  /** 选中的仓库 ID */
  warehouseId?: number
  /** 选中的库位编码 */
  locationCode?: string
  /** 当前行商品下拉的候选项 */
  productOptions: Product[]
  /** 当前仓库下的库位列表（选择仓库后加载） */
  locations: Location[]
  /** 商品远程搜索 loading */
  productLoading: boolean
}

/** 供应商名称 */
const supplierName = ref('')
/** 明细行列表，每行是一笔“商品 + 库位 + 数量” */
const items = ref<InboundRow[]>([])
/** 提交按钮 loading */
const submitting = ref(false)
/** 仓库列表（页面加载时一次性获取） */
const warehouses = ref<Warehouse[]>([])

/** 创建一行空白明细 */
const newRow = (): InboundRow => ({
  quantity: 1,
  productOptions: [],
  locations: [],
  productLoading: false,
})

/** 添加一行明细 */
const addItem = () => {
  items.value.push(newRow())
}

/** 删除指定行明细 */
const removeItem = (index: number) => {
  items.value.splice(index, 1)
}

// 商品远程搜索
const searchProducts = async (row: InboundRow, keyword: string) => {
  row.productLoading = true
  try {
    const res = await getProducts({ keyword: keyword || undefined, page: 1, pageSize: 50 })
    row.productOptions = res.data.list
  } catch (e: any) {
    ElMessage.error('商品搜索失败: ' + (e.response?.data?.message || e.message))
  } finally {
    row.productLoading = false
  }
}

// 选择仓库后加载该仓库的库位（级联）
const onWarehouseChange = async (row: InboundRow) => {
  row.locationCode = undefined
  row.locations = []
  if (!row.warehouseId) return
  try {
    row.locations = (await getLocations(row.warehouseId)).data
  } catch (e: any) {
    ElMessage.error('库位加载失败: ' + (e.response?.data?.message || e.message))
  }
}

/** 提交前前端校验：返回错误提示，null 表示通过 */
const validate = (): string | null => {
  if (!supplierName.value.trim()) return '请输入供应商名称'
  if (items.value.length === 0) return '请添加入库明细'
  for (let i = 0; i < items.value.length; i++) {
    const it = items.value[i]
    if (!it.productId) return `第 ${i + 1} 行请选择商品`
    if (!it.warehouseId) return `第 ${i + 1} 行请选择仓库`
    if (!it.locationCode) return `第 ${i + 1} 行请选择库位`
    if (!it.quantity || it.quantity <= 0) return `第 ${i + 1} 行数量必须大于 0`
  }
  return null
}

/** 提交入库单：校验 -> 调用 API -> 成功后清空表单 */
const handleSubmit = async () => {
  const err = validate()
  if (err) {
    ElMessage.warning(err)
    return
  }
  submitting.value = true
  try {
    const res = await createInboundOrder({
      supplierName: supplierName.value.trim(),
      items: items.value.map((it) => ({
        productId: it.productId!,
        quantity: it.quantity,
        locationCode: it.locationCode!,
      })),
    })
    ElMessage.success(`入库单创建成功：${res.data?.orderNo || ''}`)
    supplierName.value = ''
    items.value = []
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '创建失败')
  } finally {
    submitting.value = false
  }
}

// 页面初始化：加载仓库列表并预置一行明细
onMounted(async () => {
  try {
    warehouses.value = (await getWarehouses()).data
  } catch (e: any) {
    ElMessage.error('仓库加载失败: ' + (e.response?.data?.message || e.message))
  }
  addItem()
})
</script>

<template>
  <div>
    <h3>入库管理</h3>

    <el-form label-width="100px" style="max-width: 1000px">
      <el-form-item label="供应商名称" required>
        <el-input v-model="supplierName" placeholder="请输入供应商名称" style="max-width: 400px" />
      </el-form-item>

      <el-form-item label="入库明细">
        <el-button type="primary" @click="addItem">+ 添加明细</el-button>
      </el-form-item>

      <!-- 明细行 -->
      <el-form-item
        v-for="(item, index) in items"
        :key="index"
        :label="`明细 ${index + 1}`"
        style="display: block"
      >
        <div style="display: flex; gap: 12px; align-items: center; flex-wrap: wrap">
          <el-select
            v-model="item.productId"
            filterable
            remote
            clearable
            :remote-method="(q: string) => searchProducts(item, q)"
            :loading="item.productLoading"
            placeholder="搜索并选择商品"
            style="width: 240px"
          >
            <el-option
              v-for="p in item.productOptions"
              :key="p.id"
              :label="`${p.name}（${p.sku}）`"
              :value="p.id"
            />
          </el-select>

          <el-select
            v-model="item.warehouseId"
            placeholder="选择仓库"
            clearable
            style="width: 160px"
            @change="onWarehouseChange(item)"
          >
            <el-option
              v-for="w in warehouses"
              :key="w.id"
              :label="w.name"
              :value="w.id"
            />
          </el-select>

          <el-select
            v-model="item.locationCode"
            placeholder="选择库位"
            clearable
            :disabled="!item.warehouseId"
            style="width: 180px"
          >
            <el-option
              v-for="l in item.locations"
              :key="l.code"
              :label="l.code"
              :value="l.code"
            />
          </el-select>

          <el-input-number v-model="item.quantity" :min="1" :precision="0" placeholder="数量" />

          <el-button type="danger" size="small" @click="removeItem(index)">删除</el-button>
        </div>
      </el-form-item>
    </el-form>

    <!-- 提交按钮 -->
    <el-button
      type="success"
      :loading="submitting"
      @click="handleSubmit"
      :disabled="items.length === 0"
    >
      提交入库单
    </el-button>

    <el-empty v-if="items.length === 0" description="请点击「添加明细」按钮添加入库商品" />
  </div>
</template>
