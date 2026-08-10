package com.wms.repository;

import com.wms.entity.InboundOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * 入库单明细 Repository。
 * Spring Data JPA 会根据方法名自动生成查询实现。
 */
@Repository
public interface InboundOrderItemRepository extends JpaRepository<InboundOrderItem, Long> {

    /** 查询某入库单的所有明细 */
    List<InboundOrderItem> findByOrderId(Long orderId);

    /** 批量查询多张单据的明细（列表页避免 N+1） */
    List<InboundOrderItem> findByOrderIdIn(Collection<Long> orderIds);

    /** 判断商品是否出现在任何入库单中（删除商品前校验） */
    boolean existsByProductId(Long productId);
}
