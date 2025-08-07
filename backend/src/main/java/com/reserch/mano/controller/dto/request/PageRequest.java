package com.reserch.mano.controller.dto.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {

    @Min(value = 0, message = "Page number must be non-negative")
    private Integer page = 0;

    @Min(value = 1, message = "Page size must be positive")
    private Integer size = 20;

    private String sortBy = "id";
    private String sortDirection = "ASC";
    private String search;
    private Map<String, Object> filters;
}
