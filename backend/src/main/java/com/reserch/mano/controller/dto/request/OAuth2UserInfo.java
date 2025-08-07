package com.reserch.mano.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OAuth2UserInfo {
    private String id;
    private String email;
    private String name;
    private String firstName;
    private String lastName;
    private String profilePicture;
    private String provider;
    private Boolean emailVerified;
}