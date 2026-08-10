package com.wms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 商品主数据：WMS 所有出入库/库存操作都以商品为维度展开。
 * SKU 全局唯一，删除前会校验是否被库存/单据引用。
 */
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 主键 */
    private Long id;

    @Column(nullable = false, length = 200)
    /** 商品名称 */
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    /** 商品编码（唯一），业务上用于精确标识商品 */
    private String sku;

    @Column(length = 20)
    @Builder.Default
    /** 计量单位，如 个/条/张 */
    private String unit = "个";

    @Column(name = "created_at", updatable = false)
    /** 创建时间（插入时自动填充，不可更新） */
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    /** 最后更新时间（每次修改自动刷新） */
    private LocalDateTime updatedAt;

    @PrePersist
    /** 插入前自动写入创建/更新时间 */
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    /** 更新前自动刷新更新时间 */
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
