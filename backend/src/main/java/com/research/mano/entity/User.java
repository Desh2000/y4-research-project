package com.research.mano.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User Entity for Authentication and User Management
 * Main entity for users in the Man system
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        })
public class User extends BaseEntity {

    @NotBlank
    @Size(max = 50)
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @NotBlank
    @Size(max = 100)
    @Email
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotBlank
    @Size(max = 120)
    @Column(name = "password", nullable = false)
    private String password;

    @Size(max = 50)
    @Column(name = "first_name")
    private String firstName;

    @Size(max = 50)
    @Column(name = "last_name")
    private String lastName;

    @Size(max = 15)
    @Column(name = "phone")
    private String phone;

    @Column(name = "date_of_birth")
    private LocalDateTime dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "is_email_verified")
    private Boolean isEmailVerified = false;

    @Column(name = "is_phone_verified")
    private Boolean isPhoneVerified = false;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "email_verification_token")
    private String emailVerificationToken;

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_expires")
    private LocalDateTime passwordResetExpires;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "refresh_token_expires")
    private LocalDateTime refreshTokenExpires;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    // Mental Health Specific Fields
    @Column(name = "privacy_consent")
    private Boolean privacyConsent = false;

    @Column(name = "data_sharing_consent")
    private Boolean dataSharingConsent = false;

    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "mental_health_status")
    private MentalHealthStatus mentalHealthStatus;

    // Constructors
    public User() {
        super();
    }

    public User(String username, String email, String password) {
        this();
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public User(String username, String email, String password, String firstName, String lastName) {
        this(username, email, password);
        this.firstName = firstName;
        this.lastName = lastName;
    }


    // Utility methods
    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    public boolean hasRole(Role.RoleName roleName) {
        return roles.stream().anyMatch(role -> role.getName().equals(roleName));
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + getId() +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }

    /**
     * Gender Enum
     */
    public enum Gender {
        MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
    }

    /**
     * Mental Health Status Enum
     */
    public enum MentalHealthStatus {
        STABLE("Currently stable and managing well"),
        MILD_CONCERN("Some concerns but managing"),
        MODERATE_CONCERN("Moderate concerns, seeking help"),
        HIGH_CONCERN("High level concerns, needs immediate attention"),
        CRISIS("Crisis situation, needs emergency support");

        private final String description;

        MentalHealthStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}