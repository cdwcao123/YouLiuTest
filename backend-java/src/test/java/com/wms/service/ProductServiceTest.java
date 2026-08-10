package com.wms.service;

import com.wms.common.BusinessException;
import com.wms.entity.Product;
import com.wms.repository.InboundOrderItemRepository;
import com.wms.repository.InventoryRepository;
import com.wms.repository.OutboundOrderItemRepository;
import com.wms.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * 商品删除 Bug 修复测试（任务 3）
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private InboundOrderItemRepository inboundOrderItemRepository;
    @Mock
    private OutboundOrderItemRepository outboundOrderItemRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder().id(1L).name("蓝牙耳机 Pro").sku("SKU-001").build();
    }

    @Test
    @DisplayName("有关联库存的商品不能删除")
    void delete_withInventory_throws() {
        when(productRepository.existsById(1L)).thenReturn(true);
        when(inventoryRepository.existsByProductId(1L)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> productService.delete(1L));
        assertTrue(ex.getMessage().contains("关联库存"));
        verify(productRepository, never()).deleteById(1L);
    }

    @Test
    @DisplayName("有关联出入库单的商品不能删除")
    void delete_withOrderItems_throws() {
        when(productRepository.existsById(1L)).thenReturn(true);
        when(inventoryRepository.existsByProductId(1L)).thenReturn(false);
        when(inboundOrderItemRepository.existsByProductId(1L)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> productService.delete(1L));
        assertTrue(ex.getMessage().contains("关联出入库单"));
        verify(productRepository, never()).deleteById(1L);
    }

    @Test
    @DisplayName("无任何关联数据的商品可以正常删除")
    void delete_withoutAssociations_succeeds() {
        when(productRepository.existsById(1L)).thenReturn(true);
        when(inventoryRepository.existsByProductId(1L)).thenReturn(false);
        when(inboundOrderItemRepository.existsByProductId(1L)).thenReturn(false);
        when(outboundOrderItemRepository.existsByProductId(1L)).thenReturn(false);

        productService.delete(1L);

        verify(productRepository).deleteById(1L);
    }
}
