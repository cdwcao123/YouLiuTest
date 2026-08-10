package com.wms.repository;

import com.wms.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 仓库 Repository。
 */
@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
}
