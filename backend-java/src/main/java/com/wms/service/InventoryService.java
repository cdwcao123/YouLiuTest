package com.wms.service;

import com.wms.common.PageResult;
import com.wms.dto.InventoryResponse;
import com.wms.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * 库存查询 Service — 任务 2
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    /**
     * 库存分页查询：一次 JOIN 查出商品名称/仓库名称，避免 N+1。
     * 查询条件：商品名称/SKU 模糊、仓库、库位编码。
     */
    public PageResult<InventoryResponse> queryInventory(String keyword, Long warehouseId,
                                                        String locationCode, int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 100);

        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        String loc = (locationCode == null || locationCode.isBlank()) ? null : locationCode.trim();

        Page<InventoryResponse> result = inventoryRepository.searchInventory(
                kw, warehouseId, loc, PageRequest.of(safePage - 1, safeSize));
        return new PageResult<>(result.getContent(), result.getTotalElements(), safePage, safeSize);
    }
}
