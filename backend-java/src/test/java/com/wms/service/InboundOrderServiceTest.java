package com.wms.service;

import com.wms.common.BusinessException;
import com.wms.dto.InboundOrderCreateRequest;
import com.wms.dto.InboundOrderResponse;
import com.wms.entity.InboundOrder;
import com.wms.entity.InboundOrderItem;
import com.wms.entity.Product;
import com.wms.repository.InboundOrderItemRepository;
import com.wms.repository.InboundOrderRepository;
import com.wms.repository.InventoryRepository;
import com.wms.repository.LocationRepository;
import com.wms.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 入库单创建 Service 层单元测试（选做任务 B）
 */
@ExtendWith(MockitoExtension.class)
class InboundOrderServiceTest {

    @Mock
    private InboundOrderRepository inboundOrderRepository;
    @Mock
    private InboundOrderItemRepository inboundOrderItemRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private InboundOrderService inboundOrderService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder().id(1L).name("蓝牙耳机 Pro").sku("SKU-001").build();
    }

    @Test
    @DisplayName("创建入库单成功：生成单号、保存明细并累加库存")
    void createInboundOrder_success_increasesInventory() {
        InboundOrderCreateRequest request = new InboundOrderCreateRequest();
        request.setSupplierName("供应商A");
        InboundOrderCreateRequest.InboundItemRequest item = new InboundOrderCreateRequest.InboundItemRequest();
        item.setProductId(1L);
        item.setQuantity(100);
        item.setLocationCode("WH-A-01-01");
        request.setItems(List.of(item));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(locationRepository.existsByCode("WH-A-01-01")).thenReturn(true);
        when(inboundOrderRepository.findTopByOrderNoStartingWithOrderByOrderNoDesc(anyString()))
                .thenReturn(Optional.empty());
        when(inboundOrderRepository.save(any(InboundOrder.class))).thenAnswer(inv -> {
            InboundOrder order = inv.getArgument(0);
            order.setId(1L);
            return order;
        });
        when(inboundOrderItemRepository.save(any(InboundOrderItem.class))).thenAnswer(inv -> inv.getArgument(0));

        InboundOrderResponse response = inboundOrderService.createInboundOrder(request);

        assertNotNull(response.getId());
        String expectedPrefix = "IN-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        assertTrue(response.getOrderNo().startsWith(expectedPrefix));
        assertEquals("IN-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-001",
                response.getOrderNo());
        assertEquals("COMPLETED", response.getStatus());
        assertEquals(1, response.getItems().size());
        assertEquals("蓝牙耳机 Pro", response.getItems().get(0).getProductName());

        verify(inventoryRepository).increaseQuantity(1L, "WH-A-01-01", 100);
        verify(inboundOrderItemRepository).save(any(InboundOrderItem.class));
    }

    @Test
    @DisplayName("商品不存在时抛业务异常且不保存任何单据")
    void createInboundOrder_productNotFound_throws() {
        InboundOrderCreateRequest request = new InboundOrderCreateRequest();
        request.setSupplierName("供应商A");
        InboundOrderCreateRequest.InboundItemRequest item = new InboundOrderCreateRequest.InboundItemRequest();
        item.setProductId(999L);
        item.setQuantity(10);
        item.setLocationCode("WH-A-01-01");
        request.setItems(List.of(item));

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> inboundOrderService.createInboundOrder(request));
        assertTrue(ex.getMessage().contains("商品不存在"));
        verify(inboundOrderRepository, never()).save(any());
        verify(inventoryRepository, never()).increaseQuantity(any(), any(), any());
    }

    @Test
    @DisplayName("库位不存在时抛业务异常")
    void createInboundOrder_locationNotFound_throws() {
        InboundOrderCreateRequest request = new InboundOrderCreateRequest();
        request.setSupplierName("供应商A");
        InboundOrderCreateRequest.InboundItemRequest item = new InboundOrderCreateRequest.InboundItemRequest();
        item.setProductId(1L);
        item.setQuantity(10);
        item.setLocationCode("NO-SUCH-LOC");
        request.setItems(List.of(item));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(locationRepository.existsByCode("NO-SUCH-LOC")).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> inboundOrderService.createInboundOrder(request));
        assertTrue(ex.getMessage().contains("库位不存在"));
        verify(inboundOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("明细为空时抛业务异常")
    void createInboundOrder_emptyItems_throws() {
        InboundOrderCreateRequest request = new InboundOrderCreateRequest();
        request.setSupplierName("供应商A");
        request.setItems(List.of());

        assertThrows(BusinessException.class, () -> inboundOrderService.createInboundOrder(request));
    }
}
