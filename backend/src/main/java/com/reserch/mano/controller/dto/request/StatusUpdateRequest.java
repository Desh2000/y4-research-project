package com.reserch.mano.controller.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateRequest {

    @Min(value = 1, message = "ID must be positive")
    private Long id;

    @NotBlank(message = "Status is required")
    private String status;

    private String reason;
}
