package com.reserch.mano.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for representing a member of a community.
 * Note: We only expose non-sensitive information like first name.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityMemberDto {
    private Long userId;
    private String firstName;
}