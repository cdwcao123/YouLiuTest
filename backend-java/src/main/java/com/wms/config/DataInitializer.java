package com.wms.config;

import com.wms.entity.*;
import com.wms.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器：应用启动时执行。
 * 若商品表为空，则插入示例商品/仓库/库位/库存，方便本地开发与演示；
 * 若已有数据则跳过，保证不会重复初始化。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final LocationRepository locationRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            log.info("示例数据已存在，跳过初始化");
            return;
        }

        log.info("初始化示例数据...");

        // 商品（5 个示例 SKU）
        Product p1 = productRepository.save(Product.builder().name("蓝牙耳机 Pro").sku("SKU-001").unit("个").build());
        Product p2 = productRepository.save(Product.builder().name("Type-C 数据线").sku("SKU-002").unit("条").build());
        Product p3 = productRepository.save(Product.builder().name("无线充电板").sku("SKU-003").unit("个").build());
        Product p4 = productRepository.save(Product.builder().name("手机壳 透明款").sku("SKU-004").unit("个").build());
        Product p5 = productRepository.save(Product.builder().name("屏幕保护膜").sku("SKU-005").unit("张").build());

        // 仓库（广州主仓 / 深圳保税仓）
        Warehouse wh1 = warehouseRepository.save(Warehouse.builder().code("WH-A").name("广州主仓").build());
        Warehouse wh2 = warehouseRepository.save(Warehouse.builder().code("WH-B").name("深圳保税仓").build());

        // 库位（挂在对应仓库下，状态 FREE=空闲 / OCCUPIED=占用）
        Location loc1 = locationRepository.save(Location.builder().warehouseId(wh1.getId()).code("WH-A-01-01").status("OCCUPIED").build());
        Location loc2 = locationRepository.save(Location.builder().warehouseId(wh1.getId()).code("WH-A-01-02").status("OCCUPIED").build());
        Location loc3 = locationRepository.save(Location.builder().warehouseId(wh1.getId()).code("WH-A-02-01").status("FREE").build());
        Location loc4 = locationRepository.save(Location.builder().warehouseId(wh2.getId()).code("WH-B-01-01").status("FREE").build());

        // 初始库存（product + location 维度，允许同一商品分布在多个库位）
        inventoryRepository.save(Inventory.builder().productId(p1.getId()).locationCode(loc1.getCode()).quantity(150).build());
        inventoryRepository.save(Inventory.builder().productId(p1.getId()).locationCode(loc2.getCode()).quantity(80).build());
        inventoryRepository.save(Inventory.builder().productId(p2.getId()).locationCode(loc1.getCode()).quantity(300).build());
        inventoryRepository.save(Inventory.builder().productId(p3.getId()).locationCode(loc2.getCode()).quantity(5).build());
        inventoryRepository.save(Inventory.builder().productId(p4.getId()).locationCode(loc1.getCode()).quantity(8).build());

        log.info("示例数据初始化完成: {} 商品, {} 仓库, {} 库位, {} 库存记录",
                productRepository.count(), warehouseRepository.count(),
                locationRepository.count(), inventoryRepository.count());
    }
}
