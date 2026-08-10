package com.wms.repository;

import com.wms.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品 Repository。
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /** 判断 SKU 是否已存在（新增商品时校验唯一性） */
    boolean existsBySku(String sku);

    /** 查询尚未分配供应商的商品（启动时随机回填） */
    List<Product> findBySupplierNameIsNull();

    /**
     * 模糊搜索商品（按名称或SKU）
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR p.name LIKE %:keyword% OR p.sku LIKE %:keyword%)")
    List<Product> search(@Param("keyword") String keyword);

    /** 商品分页搜索（keyword 为空时返回全部） */
    @Query(value = "SELECT p FROM Product p WHERE " +
            "(:keyword IS NULL OR p.name LIKE %:keyword% OR p.sku LIKE %:keyword%)",
            countQuery = "SELECT COUNT(p) FROM Product p WHERE " +
            "(:keyword IS NULL OR p.name LIKE %:keyword% OR p.sku LIKE %:keyword%)")
    Page<Product> searchPage(@Param("keyword") String keyword, Pageable pageable);
}
