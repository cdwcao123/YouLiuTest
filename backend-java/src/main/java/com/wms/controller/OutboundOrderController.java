package com.wms.controller;

import com.wms.common.ApiResponse;
import com.wms.dto.OutboundOrderCreateRequest;
import com.wms.dto.OutboundOrderResponse;
import com.wms.service.OutboundOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 出库单 Controller — 选做任务 A。
 * 创建出库单时会检查并扣减库存（悲观锁防止并发超卖）。
 */
@RestController
@RequestMapping("/api/outbound-orders")
@RequiredArgsConstructor
public class OutboundOrderController {

    private final OutboundOrderService outboundOrderService;

    /** 创建出库单：校验商品/库位，生成 OUT-YYYYMMDD-XXX 单号并扣减库存 */
    @PostMapping
    public ResponseEntity<ApiResponse<OutboundOrderResponse>> create(
            @Valid @RequestBody OutboundOrderCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("出库单创建成功", outboundOrderService.createOutboundOrder(request)));
    }
}
