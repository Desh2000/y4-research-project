package com.reserch.mano.controller.dto.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdsRequest {

    private List<@Min(value = 1, message = "ID must be positive") Long> ids;
}
