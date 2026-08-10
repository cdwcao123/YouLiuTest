package com.wms.service;

import com.wms.common.BusinessException;
import com.wms.dto.OutboundOrderCreateRequest;
import com.wms.dto.OutboundOrderResponse;
import com.wms.entity.Inventory;
import com.wms.entity.OutboundOrder;
import com.wms.entity.Product;
import com.wms.repository.InventoryRepository;
import com.wms.repository.LocationRepository;
import com.wms.repository.OutboundOrderItemRepository;
import com.wms.repository.OutboundOrderRepository;
import com.wms.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 出库单 Service 层单元测试：库存扣减与并发安全核心逻辑
 */
@ExtendWith(MockitoExtension.class)
class OutboundOrderServiceTest {

    @Mock
    private OutboundOrderRepository outboundOrderRepository;
    @Mock
    private OutboundOrderItemRepository outboundOrderItemRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private OutboundOrderService outboundOrderService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder().id(1L).name("蓝牙耳机 Pro").sku("SKU-001").build();
    }

    private OutboundOrderCreateRequest buildRequest(int quantity) {
        OutboundOrderCreateRequest request = new OutboundOrderCreateRequest();
        request.setCustomerName("客户X");
        OutboundOrderCreateRequest.OutboundItemRequest item =
                new OutboundOrderCreateRequest.OutboundItemRequest();
        item.setProductId(1L);
        item.setQuantity(quantity);
        item.setLocationCode("WH-A-01-01");
        request.setItems(List.of(item));
        return request;
    }

    @Test
    @DisplayName("出库成功：库存充足时扣减库存并保存单据")
    void createOutboundOrder_success_deductsStock() {
        Inventory inventory = Inventory.builder()
                .id(1L).productId(1L).locationCode("WH-A-01-01").quantity(100).build();
        OutboundOrderCreateRequest request = buildRequest(10);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(locationRepository.existsByCode("WH-A-01-01")).thenReturn(true);
        when(inventoryRepository.findByProductIdAndLocationCodeForUpdate(1L, "WH-A-01-01"))
                .thenReturn(Optional.of(inventory));
        when(outboundOrderRepository.findTopByOrderNoStartingWithOrderByOrderNoDesc(anyString()))
                .thenReturn(Optional.empty());
        when(outboundOrderRepository.save(any(OutboundOrder.class))).thenAnswer(inv -> {
            OutboundOrder order = inv.getArgument(0);
            order.setId(1L);
            return order;
        });

        OutboundOrderResponse response = outboundOrderService.createOutboundOrder(request);

        assertEquals(90, inventory.getQuantity());
        assertEquals("COMPLETED", response.getStatus());
        assertTrue(response.getOrderNo().startsWith("OUT-"));
        verify(inventoryRepository).save(inventory);
        verify(outboundOrderRepository).save(any(OutboundOrder.class));
    }

    @Test
    @DisplayName("库存不足时抛业务异常，不创建出库单")
    void createOutboundOrder_insufficientStock_throws() {
        Inventory inventory = Inventory.builder()
                .id(1L).productId(1L).locationCode("WH-A-01-01").quantity(5).build();
        OutboundOrderCreateRequest request = buildRequest(10);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(locationRepository.existsByCode("WH-A-01-01")).thenReturn(true);
        when(inventoryRepository.findByProductIdAndLocationCodeForUpdate(1L, "WH-A-01-01"))
                .thenReturn(Optional.of(inventory));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> outboundOrderService.createOutboundOrder(request));
        assertTrue(ex.getMessage().contains("库存不足"));
        assertEquals(5, inventory.getQuantity());
        verify(outboundOrderRepository, never()).save(any());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("库存记录不存在时视为库存不足")
    void createOutboundOrder_noInventory_throws() {
        OutboundOrderCreateRequest request = buildRequest(1);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(locationRepository.existsByCode("WH-A-01-01")).thenReturn(true);
        when(inventoryRepository.findByProductIdAndLocationCodeForUpdate(1L, "WH-A-01-01"))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> outboundOrderService.createOutboundOrder(request));
        verify(outboundOrderRepository, never()).save(any());
    }
}
