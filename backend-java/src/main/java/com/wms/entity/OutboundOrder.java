package com.wms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 出库单主表 — 选做任务 A。
 * 记录一次客户出库的整体信息，明细行存放在 outbound_order_items。
 */
@Entity
@Table(name = "outbound_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboundOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 主键 */
    private Long id;

    @Column(name = "order_no", nullable = false, unique = true, length = 50)
    /** 出库单号，格式 OUT-YYYYMMDD-XXX，全局唯一 */
    private String orderNo;

    @Column(name = "customer_name", length = 200)
    /** 客户名称 */
    private String customerName;

    @Column(length = 20)
    @Builder.Default
    /** 单据状态：DRAFT=草稿 / COMPLETED=已完成 */
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
