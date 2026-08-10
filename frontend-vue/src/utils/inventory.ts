/**
 * 库存列表筛选相关纯函数，便于单元测试
 */

export interface InventoryQuery {
  keyword?: string
  warehouseId?: number
  locationCode?: string
  page?: number
  pageSize?: number
}

export const LOW_STOCK_THRESHOLD = 10

/** 库存数量低于阈值判定为低库存（用于行高亮） */
export const isLowStock = (quantity: number, threshold = LOW_STOCK_THRESHOLD): boolean =>
  quantity < threshold

/** 根据搜索条件构造查询参数：空值不传，keyword 去除首尾空格 */
export const buildInventoryQuery = (
  keyword: string,
  warehouseId?: number,
  locationCode?: string,
  page = 1,
  pageSize = 20,
): InventoryQuery => {
  const query: InventoryQuery = { page, pageSize }
  const kw = keyword.trim()
  if (kw) query.keyword = kw
  if (warehouseId) query.warehouseId = warehouseId
  const loc = locationCode?.trim()
  if (loc) query.locationCode = loc
  return query
}

/** 简单防抖：连续触发时只执行最后一次 */
export const debounce = <T extends (...args: any[]) => void>(
  fn: T,
  wait = 300,
): ((...args: Parameters<T>) => void) => {
  let timer: ReturnType<typeof setTimeout> | undefined
  return (...args: Parameters<T>) => {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => fn(...args), wait)
  }
}
