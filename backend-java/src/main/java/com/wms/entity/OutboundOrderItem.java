package com.wms.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 出库单明细行：记录出库商品、数量与来源库位。
 */
@Entity
@Table(name = "outbound_order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboundOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 主键 */
    private Long id;

    @Column(name = "order_id", nullable = false)
    /** 所属出库单 ID */
    private Long orderId;

    @Column(name = "product_id", nullable = false)
    /** 商品 ID */
    private Long productId;

    @Column(nullable = false)
    /** 出库数量（>0，DTO 层已校验） */
    private Integer quantity;

    @Column(name = "location_code", nullable = false, length = 50)
    /** 来源库位编码 */
    private String locationCode;
}
