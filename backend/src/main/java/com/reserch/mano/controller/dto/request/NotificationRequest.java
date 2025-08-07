package com.reserch.mano.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationRequest {

    @Min(value = 1, message = "User ID must be positive")
    private Long userId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    private String type = "INFO"; // INFO, WARNING, ERROR, SUCCESS
    private String category;
    private Map<String, Object> data;
    private Boolean sendEmail = false;
    private Boolean sendPush = false;
}
