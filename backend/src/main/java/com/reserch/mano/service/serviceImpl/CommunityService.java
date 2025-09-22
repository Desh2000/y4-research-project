package com.reserch.mano.service.serviceImpl;

import com.reserch.mano.controller.dto.request.CommunityDto;
import com.reserch.mano.model.User;

import java.util.Optional;

/**
 * Service interface for community-related operations.
 */
public interface CommunityService {
    Optional<CommunityDto> getCurrentCommunityForUser(User user);
    void assignUserToCommunity(User user); // Placeholder for clustering logic
}
