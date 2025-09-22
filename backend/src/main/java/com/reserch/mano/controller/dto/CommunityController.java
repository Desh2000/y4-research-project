package com.reserch.mano.controller.dto;

import com.reserch.mano.controller.dto.request.CommunityDto;
import com.reserch.mano.model.User;
import com.reserch.mano.service.serviceImpl.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling community-related API requests.
 */
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    /**
     * Endpoint to get the current community for the authenticated user.
     * @param user The currently authenticated user.
     * @return The user's community DTO, or 404 Not Found if they are not in a community.
     */
    @GetMapping("/current")
    public ResponseEntity<CommunityDto> getCurrentCommunity(@AuthenticationPrincipal User user) {
        return communityService.getCurrentCommunityForUser(user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * A test endpoint to trigger the simulated assignment of the current user to a default community.
     * In a real system, this logic would be handled by a scheduled, automated process.
     * @param user The currently authenticated user.
     * @return A success message.
     */
    @PostMapping("/join-default")
    public ResponseEntity<String> joinDefaultCommunity(@AuthenticationPrincipal User user) {
        communityService.assignUserToCommunity(user);
        return ResponseEntity.ok("User has been assigned to the default community.");
    }
}