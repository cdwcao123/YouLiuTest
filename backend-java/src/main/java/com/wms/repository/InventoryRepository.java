package com.wms.repository;

import com.wms.entity.Inventory;
import com.wms.dto.InventoryResponse;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 库存 Repository
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductIdAndLocationCode(Long productId, String locationCode);

    /**
     * 悲观写锁：出库扣减前锁定库存行，防止并发超卖。
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId AND i.locationCode = :locationCode")
    Optional<Inventory> findByProductIdAndLocationCodeForUpdate(
            @Param("productId") Long productId,
            @Param("locationCode") String locationCode);

    /**
     * 入库原子累加：INSERT ... ON DUPLICATE KEY UPDATE，
     * 避免“先查后改”在并发入库时的丢失更新，也免去先查询是否存在的开销。
     */
    @Modifying
    @Query(value = """
            INSERT INTO inventory (product_id, location_code, quantity, updated_at)
            VALUES (:productId, :locationCode, :quantity, CURRENT_TIMESTAMP)
            ON DUPLICATE KEY UPDATE quantity = quantity + :quantity, updated_at = CURRENT_TIMESTAMP
            """, nativeQuery = true)
    int increaseQuantity(@Param("productId") Long productId,
                         @Param("locationCode") String locationCode,
                         @Param("quantity") Integer quantity);

    boolean existsByProductId(Long productId);

    /**
     * 库存分页查询：一次 JOIN 出商品与仓库信息，避免 N+1 查询。
     */
    @Query(value = """
            SELECT new com.wms.dto.InventoryResponse(
                i.productId, p.name, p.sku, i.locationCode, w.name, i.quantity, i.updatedAt)
            FROM Inventory i
            JOIN Product p ON p.id = i.productId
            JOIN Location l ON l.code = i.locationCode
            JOIN Warehouse w ON w.id = l.warehouseId
            WHERE (:keyword IS NULL OR p.name LIKE %:keyword% OR p.sku LIKE %:keyword%)
              AND (:warehouseId IS NULL OR l.warehouseId = :warehouseId)
              AND (:locationCode IS NULL OR i.locationCode = :locationCode)
            """,
            countQuery = """
            SELECT COUNT(i)
            FROM Inventory i
            JOIN Product p ON p.id = i.productId
            JOIN Location l ON l.code = i.locationCode
            JOIN Warehouse w ON w.id = l.warehouseId
            WHERE (:keyword IS NULL OR p.name LIKE %:keyword% OR p.sku LIKE %:keyword%)
              AND (:warehouseId IS NULL OR l.warehouseId = :warehouseId)
              AND (:locationCode IS NULL OR i.locationCode = :locationCode)
            """)
    Page<InventoryResponse> searchInventory(@Param("keyword") String keyword,
                                            @Param("warehouseId") Long warehouseId,
                                            @Param("locationCode") String locationCode,
                                            Pageable pageable);
}
