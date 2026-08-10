package com.wms.repository;

import com.wms.entity.OutboundOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 出库单 Repository。
 */
@Repository
public interface OutboundOrderRepository extends JpaRepository<OutboundOrder, Long> {

    /**
     * 用于生成出库单号：查询当天（前缀相同）的最大单号
     */
    Optional<OutboundOrder> findTopByOrderNoStartingWithOrderByOrderNoDesc(String prefix);
}
