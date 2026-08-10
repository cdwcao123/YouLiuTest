package com.wms.repository;

import com.wms.entity.OutboundOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 出库单明细 Repository。
 */
@Repository
public interface OutboundOrderItemRepository extends JpaRepository<OutboundOrderItem, Long> {

    /** 查询某出库单的所有明细 */
    List<OutboundOrderItem> findByOrderId(Long orderId);

    /** 判断商品是否出现在任何出库单中（删除商品前校验） */
    boolean existsByProductId(Long productId);
}
