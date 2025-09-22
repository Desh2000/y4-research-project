package com.reserch.mano.repository;

import com.reserch.mano.model.CommunityMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for CommunityMembership entity.
 */
@Repository
public interface CommunityMembershipRepository extends JpaRepository<CommunityMembership, Long> {
}