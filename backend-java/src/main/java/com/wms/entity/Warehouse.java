package com.wms.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 仓库主数据：物理仓（如广州主仓、深圳保税仓），
 * 每个仓库下挂多个库位（见 Location）。
 */
@Entity
@Table(name = "warehouses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 主键 */
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    /** 仓库编码（唯一），如 WH-A */
    private String code;

    @Column(nullable = false, length = 200)
    /** 仓库名称，如 广州主仓 */
    private String name;
}
