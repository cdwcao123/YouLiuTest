package com.wms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 库存表 — 核心业务实体。
 * 以“商品 + 库位”为维度记录实时库存，(product_id, location_code) 唯一，
 * 入库累加、出库扣减都直接操作这张表。
 */
@Entity
@Table(name = "inventory", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"product_id", "location_code"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 主键 */
    private Long id;

    @Column(name = "product_id", nullable = false)
    /** 商品 ID */
    private Long productId;

    @Column(name = "location_code", nullable = false, length = 50)
    /** 库位编码（与 locations.code 对应） */
    private String locationCode;

    @Column(nullable = false)
    @Builder.Default
    /** 当前库存数量，禁止为负（出库前会校验） */
    private Integer quantity = 0;

    @Column(name = "updated_at")
    /** 最后变动时间（入库/出库/初始化时自动刷新） */
    private LocalDateTime updatedAt;

    @PrePersist
    /** 插入时写入初始时间 */
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    /** 更新时刷新时间（JPA 实体更新路径生效；原生 SQL 路径在 SQL 内用 CURRENT_TIMESTAMP） */
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
