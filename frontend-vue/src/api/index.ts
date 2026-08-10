import api from './client'

// ============ 通用类型（与后端 ApiResponse / PageResult 对应） ============

export interface ApiResponse<T> {
  /** 业务状态码：200 成功 / 201 创建成功 / 400 业务错误等 */
  code: number
  /** 提示信息 */
  message: string
  /** 业务数据 */
  data: T
}

export interface PageData<T> {
  /** 当前页数据 */
  list: T[]
  /** 总记录数 */
  total: number
  /** 当前页码（从 1 开始） */
  page: number
  /** 每页条数 */
  pageSize: number
}

// ============ 商品 ============

export interface Product {
  id: number
  name: string
  sku: string
  unit: string
  createdAt: string
  updatedAt: string
}

/** 商品分页列表：支持名称/SKU 模糊搜索 */
export const getProducts = (params?: { keyword?: string; page?: number; pageSize?: number }) =>
  api.get<any, ApiResponse<PageData<Product>>>('/products', { params })

/** 商品详情 */
export const getProduct = (id: number) =>
  api.get<any, ApiResponse<Product>>(`/products/${id}`)

/** 新增商品 */
export const createProduct = (data: { name: string; sku: string; unit?: string }) =>
  api.post<any, ApiResponse<Product>>('/products', data)

/** 更新商品（名称/单位） */
export const updateProduct = (id: number, data: { name: string; unit?: string }) =>
  api.put<any, ApiResponse<Product>>(`/products/${id}`, data)

/** 删除商品（后端会校验关联库存/单据） */
export const deleteProduct = (id: number) =>
  api.delete<any, ApiResponse<null>>(`/products/${id}`)

// ============ 仓库 & 库位 ============

export interface Warehouse {
  id: number
  code: string
  name: string
}

export interface Location {
  id: number
  warehouseId: number
  code: string
  status: string
}

/** 仓库列表 */
export const getWarehouses = () =>
  api.get<any, ApiResponse<Warehouse[]>>('/warehouses')

/** 某仓库下的库位列表（用于“仓库 → 库位”级联选择） */
export const getLocations = (warehouseId: number) =>
  api.get<any, ApiResponse<Location[]>>(`/warehouses/${warehouseId}/locations`)

// ============ 库存查询 ============

export interface InventoryItem {
  productId: number
  productName: string
  sku: string
  locationCode: string
  warehouseName: string
  quantity: number
  updatedAt: string
}

/** 库存分页查询：keyword 模糊搜索 + 仓库/库位筛选 */
export const getInventory = (params: {
  keyword?: string
  warehouseId?: number
  locationCode?: string
  page?: number
  pageSize?: number
}) =>
  api.get<any, ApiResponse<PageData<InventoryItem>>>('/inventory', { params })

// ============ 入库单 ============

export interface InboundItemRequest {
  productId: number
  quantity: number
  locationCode: string
}

export interface InboundOrder {
  id: number
  orderNo: string
  supplierName: string
  status: string
  items: Array<{
    productId: number
    productName: string
    quantity: number
    locationCode: string
  }>
  createdAt: string
}

/** 创建入库单：后端在同一事务内保存单据并累加库存 */
export const createInboundOrder = (data: {
  supplierName: string
  items: InboundItemRequest[]
}) =>
  api.post<any, ApiResponse<InboundOrder>>('/inbound-orders', data)

// ============ 出库单（选做 A） ============

export interface OutboundItemRequest {
  productId: number
  quantity: number
  locationCode: string
}

export interface OutboundOrder {
  id: number
  orderNo: string
  customerName: string
  status: string
  items: Array<{
    productId: number
    productName: string
    quantity: number
    locationCode: string
  }>
  createdAt: string
}

/** 创建出库单：后端加锁扣减库存，库存不足会报错 */
export const createOutboundOrder = (data: {
  customerName: string
  items: OutboundItemRequest[]
}) =>
  api.post<any, ApiResponse<OutboundOrder>>('/outbound-orders', data)
