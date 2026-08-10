package com.wms.repository;

import com.wms.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 库位 Repository。
 */
@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    /** 查询某仓库下的全部库位（供级联选择） */
    List<Location> findByWarehouseId(Long warehouseId);

    /** 判断库位编码是否存在（入库/出库前校验） */
    boolean existsByCode(String code);
}
