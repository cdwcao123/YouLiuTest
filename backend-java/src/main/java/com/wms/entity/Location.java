package com.wms.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 库位：库存存放的最小物理单元，属于某个仓库（warehouseId 外键）。
 * code 全局唯一，库存记录通过 locationCode 关联到库位。
 */
@Entity
@Table(name = "locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 主键 */
    private Long id;

    @Column(name = "warehouse_id", nullable = false)
    /** 所属仓库 ID（逻辑外键，指向 warehouses.id） */
    private Long warehouseId;

    @Column(nullable = false, unique = true, length = 50)
    /** 库位编码（全局唯一），如 WH-A-01-01 */
    private String code;

    @Column(length = 20)
    @Builder.Default
    /** 库位状态：FREE=空闲 / OCCUPIED=占用（示例数据展示用） */
    private String status = "FREE";
}
