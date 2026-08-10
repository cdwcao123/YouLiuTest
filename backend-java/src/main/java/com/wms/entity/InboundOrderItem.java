package com.wms.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 入库单明细行：记录该单据下每个商品入到哪个库位、数量多少，
 * 通过 orderId 关联入库单主表。
 */
@Entity
@Table(name = "inbound_order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InboundOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 主键 */
    private Long id;

    @Column(name = "order_id", nullable = false)
    /** 所属入库单 ID */
    private Long orderId;

    @Column(name = "product_id", nullable = false)
    /** 商品 ID */
    private Long productId;

    @Column(nullable = false)
    /** 入库数量（>0，DTO 层已校验） */
    private Integer quantity;

    @Column(name = "location_code", nullable = false, length = 50)
    /** 目标库位编码 */
    private String locationCode;
}
