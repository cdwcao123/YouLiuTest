package com.wms.controller;

import com.wms.common.ApiResponse;
import com.wms.common.PageResult;
import com.wms.dto.InboundOrderCreateRequest;
import com.wms.dto.InboundOrderResponse;
import com.wms.service.InboundOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 入库单 Controller — 任务 1。
 * 创建入库单时会自动累加对应库位库存（事务保证一致性）。
 */
@RestController
@RequestMapping("/api/inbound-orders")
@RequiredArgsConstructor
public class InboundOrderController {

    private final InboundOrderService inboundOrderService;

    /** 创建入库单：校验商品/库位，生成 IN-YYYYMMDD-XXX 单号并累加库存 */
    @PostMapping
    public ResponseEntity<ApiResponse<InboundOrderResponse>> create(
            @Valid @RequestBody InboundOrderCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("入库单创建成功", inboundOrderService.createInboundOrder(request)));
    }

    /** 入库单分页列表 */
    @GetMapping
    public ApiResponse<PageResult<InboundOrderResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(inboundOrderService.list(page, pageSize));
    }

    /** 入库单详情（含明细） */
    @GetMapping("/{id}")
    public ApiResponse<InboundOrderResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(inboundOrderService.getById(id));
    }
}
