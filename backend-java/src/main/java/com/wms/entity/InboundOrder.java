package com.wms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 入库单主表：记录一次采购入库的整体信息（供应商、单号、状态、创建时间），
 * 明细行存放在 inbound_order_items（InboundOrderItem）。
 * status 在建单并完成库存累加后置为 COMPLETED。
 */
@Entity
@Table(name = "inbound_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InboundOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 主键 */
    private Long id;

    @Column(name = "order_no", nullable = false, unique = true, length = 50)
    /** 入库单号，格式 IN-YYYYMMDD-XXX，全局唯一 */
    private String orderNo;

    @Column(name = "supplier_name", length = 200)
    /** 供应商名称 */
    private String supplierName;

    @Column(length = 20)
    @Builder.Default
    /** 单据状态：DRAFT=草稿 / COMPLETED=已完成（当前创建即完成） */
    private String status = "DRAFT";

    @Column(name = "created_at", updatable = false)
    /** 创建时间 */
    private LocalDateTime createdAt;

    @PrePersist
    /** 插入前自动填充创建时间 */
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
