package com.reserch.mano.service.serviceImpl.impl;

import com.reserch.mano.controller.dto.request.CommunityDto;
import com.reserch.mano.controller.dto.request.CommunityMemberDto;
import com.reserch.mano.model.Community;
import com.reserch.mano.model.CommunityMembership;
import com.reserch.mano.model.User;
import com.reserch.mano.repository.CommunityMembershipRepository;
import com.reserch.mano.repository.CommunityRepository;
import com.reserch.mano.service.serviceImpl.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for community-related operations.
 */
@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private CommunityRepository communityRepository;
    private CommunityMembershipRepository communityMembershipRepository;

    @Override
    public Optional<CommunityDto> getCurrentCommunityForUser(User user) {
        // Find the user's most recent community membership
        Optional<CommunityMembership> latestMembership = user.getMemberships().stream()
                .findFirst(); // Assuming the list is ordered by join date descending, which it is not yet.
        // For now, we'll just get the first one we find.

        return latestMembership.map(membership -> {
            Community community = membership.getCommunity();
            return mapToDto(community);
        });
    }

    /**
     * This is a simulated method to mimic the GMM clustering process.
     * In a real application, a scheduled job would call the Python GMM service,
     * get back user-cluster assignments, and then update the database.
     * @param user The user to assign to a community.
     */
    @Override
    public void assignUserToCommunity(User user) {
        // TODO: Replace this with actual clustering logic.
        // 1. Check if a default community exists, if not, create it.
        Community defaultCommunity = communityRepository.findByName("Resilience Builders")
                .orElseGet(() -> {
                    Community newCommunity = Community.builder()
                            .name("Resilience Builders")
                            .description("A supportive group for individuals working on building mental resilience.")
                            .build();
                    return communityRepository.save(newCommunity);
                });

        // 2. Check if the user is already in this community.
        boolean alreadyMember = user.getMemberships().stream()
                .anyMatch(m -> m.getCommunity().getId().equals(defaultCommunity.getId()));

        // 3. If not a member, create the membership.
        if (!alreadyMember) {
            CommunityMembership membership = CommunityMembership.builder()
                    .user(user)
                    .community(defaultCommunity)
                    .build();
            communityMembershipRepository.save(membership);
        }
    }


    private CommunityDto mapToDto(Community community) {
        List<CommunityMemberDto> memberDtos = community.getMemberships().stream()
                .map(membership -> new CommunityMemberDto(
                        membership.getUser().getId(),
                        membership.getUser().getFirstName()
                ))
                .collect(Collectors.toList());

        return CommunityDto.builder()
                .id(community.getId())
                .name(community.getName())
                .description(community.getDescription())
                .members(memberDtos)
                .build();
    }
}

