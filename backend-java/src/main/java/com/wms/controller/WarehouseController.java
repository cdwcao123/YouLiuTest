package com.wms.controller;

import com.wms.common.ApiResponse;
import com.wms.entity.Location;
import com.wms.entity.Warehouse;
import com.wms.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 仓库 & 库位查询 Controller。
 * 供入库/出库表单的“仓库 → 库位”级联选择使用。
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    /** 仓库列表 */
    @GetMapping("/warehouses")
    public ApiResponse<List<Warehouse>> listWarehouses() {
        return ApiResponse.success(warehouseService.listAll());
    }

    /** 某仓库下的库位列表 */
    @GetMapping("/warehouses/{id}/locations")
    public ApiResponse<List<Location>> getLocations(@PathVariable Long id) {
        return ApiResponse.success(warehouseService.getLocationsByWarehouse(id));
    }
}
