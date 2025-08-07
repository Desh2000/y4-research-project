package com.reserch.mano.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailRequest {

    @NotBlank(message = "Recipient email is required")
    private String to;

    private String cc;
    private String bcc;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Content is required")
    private String content;

    private String contentType = "text/html";
    private Map<String, Object> templateVariables;
    private String templateName;
    private List<String> attachments;
}
