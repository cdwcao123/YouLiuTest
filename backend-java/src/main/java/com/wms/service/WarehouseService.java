package com.wms.service;

import com.wms.entity.Location;
import com.wms.entity.Warehouse;
import com.wms.repository.LocationRepository;
import com.wms.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 仓库 & 库位查询 Service。
 */
@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final LocationRepository locationRepository;

    /** 全部仓库列表 */
    public List<Warehouse> listAll() {
        return warehouseRepository.findAll();
    }

    /** 某仓库下的库位列表 */
    public List<Location> getLocationsByWarehouse(Long warehouseId) {
        return locationRepository.findByWarehouseId(warehouseId);
    }
}
