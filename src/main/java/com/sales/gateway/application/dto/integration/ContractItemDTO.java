package com.sales.gateway.application.dto.integration;

import java.math.BigDecimal;

public record ContractItemDTO(
        String productId, String productName, Integer quantity, BigDecimal unitPrice) {}
