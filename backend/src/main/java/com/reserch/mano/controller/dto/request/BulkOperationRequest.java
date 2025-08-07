package com.reserch.mano.controller.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkOperationRequest {

    private List<@Min(value = 1, message = "ID must be positive") Long> ids;

    @NotBlank(message = "Operation is required")
    private String operation; // DELETE, ACTIVATE, DEACTIVATE, etc.

    private Map<String, Object> parameters;
    private String reason;
}
