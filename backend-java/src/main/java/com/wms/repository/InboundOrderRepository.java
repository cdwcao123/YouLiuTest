package com.wms.repository;

import com.wms.entity.InboundOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 入库单 Repository
 */
@Repository
public interface InboundOrderRepository extends JpaRepository<InboundOrder, Long> {

    /**
     * 用于生成入库单号：查询当天（前缀相同）的最大单号
     */
    Optional<InboundOrder> findTopByOrderNoStartingWithOrderByOrderNoDesc(String prefix);
}
