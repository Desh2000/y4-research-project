package com.reserch.mano.controller.dto.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdRequest {

    @Min(value = 1, message = "ID must be positive")
    private Long id;
}
