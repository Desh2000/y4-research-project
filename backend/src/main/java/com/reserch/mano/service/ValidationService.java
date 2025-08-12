package com.reserch.mano.service;

import com.reserch.mano.controller.dto.request.RegisterRequest;
import com.reserch.mano.controller.dto.request.UpdateUserRequest;
import com.reserch.mano.controller.dto.request.UserProfileRequest;

import java.util.List;

public interface ValidationService {

    List<String> validateRegistration(RegisterRequest registerRequest);

    List<String> validateUserUpdate(UpdateUserRequest updateRequest);

    List<String> validateProfileUpdate(UserProfileRequest profileRequest);

    boolean isValidEmail(String email);

    boolean isValidPassword(String password);

    boolean isValidUsername(String username);

    boolean isValidPhoneNumber(String phoneNumber);

    List<String> validateMentalHealthMetrics(UserProfileRequest profileRequest);

    boolean isValidStressLevel(Integer level);

    boolean isValidAnxietyLevel(Integer level);

    boolean isValidMoodRating(Integer rating);

    boolean isValidSleepQuality(Integer quality);

    boolean isValidActivityLevel(Integer level);

    void validatePrivacyConsent(Boolean consent);

    void validateDataSharingConsent(Boolean consent);
}