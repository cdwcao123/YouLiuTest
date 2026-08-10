import { describe, expect, it, vi } from 'vitest'
import {
  buildInventoryQuery,
  debounce,
  getInventoryRowStyle,
  isLowStock,
  LOW_STOCK_THRESHOLD,
} from './inventory'

describe('buildInventoryQuery 库存筛选参数构造', () => {
  it('keyword 去除首尾空格，空条件不传参', () => {
    expect(buildInventoryQuery('  蓝牙耳机  ')).toEqual({
      page: 1,
      pageSize: 20,
      keyword: '蓝牙耳机',
    })
    expect(buildInventoryQuery('   ')).toEqual({ page: 1, pageSize: 20 })
  })

  it('包含仓库与库位筛选条件', () => {
    expect(buildInventoryQuery('SKU-001', 1, ' WH-A-01-01 ', 2, 50)).toEqual({
      page: 2,
      pageSize: 50,
      keyword: 'SKU-001',
      warehouseId: 1,
      locationCode: 'WH-A-01-01',
    })
  })
})

describe('isLowStock 低库存判断', () => {
  it('数量低于阈值时判定为低库存', () => {
    expect(isLowStock(0)).toBe(true)
    expect(isLowStock(LOW_STOCK_THRESHOLD - 1)).toBe(true)
  })

  it('数量等于或高于阈值时不高亮', () => {
    expect(isLowStock(LOW_STOCK_THRESHOLD)).toBe(false)
    expect(isLowStock(100)).toBe(false)
  })
})

describe('getInventoryRowStyle 低库存行高亮', () => {
  it('数量低于阈值时返回红色加粗样式', () => {
    expect(getInventoryRowStyle({ row: { quantity: LOW_STOCK_THRESHOLD - 1 } })).toEqual({
      color: '#f56c6c',
      fontWeight: 'bold',
    })
  })

  it('数量等于或高于阈值时返回空样式', () => {
    expect(getInventoryRowStyle({ row: { quantity: LOW_STOCK_THRESHOLD } })).toEqual({})
  })
})

describe('debounce 防抖', () => {
  it('连续调用只执行最后一次', () => {
    vi.useFakeTimers()
    const fn = vi.fn()
    const debounced = debounce(fn, 300)

    debounced('a')
    debounced('b')
    expect(fn).not.toHaveBeenCalled()

    vi.advanceTimersByTime(299)
    expect(fn).not.toHaveBeenCalled()

    vi.advanceTimersByTime(1)
    expect(fn).toHaveBeenCalledTimes(1)
    expect(fn).toHaveBeenCalledWith('b')
    vi.useRealTimers()
  })
})
