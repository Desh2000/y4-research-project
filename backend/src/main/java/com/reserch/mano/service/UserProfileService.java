package com.reserch.mano.service;

import com.reserch.mano.controller.dto.request.UserProfileRequest;
import com.reserch.mano.controller.dto.response.UserProfileResponse;
import com.reserch.mano.securuty.UserPrincipal;

import java.util.List;

public interface UserProfileService {

    UserProfileResponse getUserProfile(UserPrincipal userPrincipal);

    UserProfileResponse getUserProfile(Long userId);

    UserProfileResponse updateUserProfile(UserPrincipal userPrincipal, UserProfileRequest profileRequest);

    UserProfileResponse createUserProfile(Long userId, UserProfileRequest profileRequest);

    void updateResilienceScore(Long userId, Double score);

    void updateRiskScore(Long userId, Double score);

    void updatePrivacyConsent(UserPrincipal userPrincipal, Boolean consent);

    void updateDataSharingConsent(UserPrincipal userPrincipal, Boolean consent);

    List<UserProfileResponse> getHighRiskUsers(Double riskThreshold);

    List<UserProfileResponse> getLowResilienceUsers(Double resilienceThreshold);

    List<UserProfileResponse> getUsersWithoutConsent();

    Double getAverageStressLevel();

    Double getAverageAnxietyLevel();

    Double getAverageResilienceScore();

    Double getAverageRiskScore();

    long getConsentedUsersCount();

    boolean hasPrivacyConsent(Long userId);

    boolean hasDataSharingConsent(Long userId);
}