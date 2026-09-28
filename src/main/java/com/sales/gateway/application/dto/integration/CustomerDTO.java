package com.sales.gateway.application.dto.integration;

import java.util.UUID;

public record CustomerDTO(UUID accountId, String document, String fullName, String email) {}
