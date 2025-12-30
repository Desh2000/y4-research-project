package com.research.mano.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Role Entity for User Authorization
 * Defines different roles in the Man system
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "name", length = 20)
    private RoleName name;

    @Column(name = "description")
    private String description;

    // Constructors
    public Role() {
        super();
    }

    public Role(RoleName name) {
        this();
        this.name = name;
    }

    public Role(RoleName name, String description) {
        this(name);
        this.description = description;
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + getId() +
                ", name=" + name +
                ", description='" + description + '\'' +
                '}';
    }

    /**
     * Enum for Role Names
     * Defines all possible roles in the Man system
     */
    public enum RoleName {
        ROLE_USER("Standard user with access to basic features"),
        ROLE_THERAPIST("Mental health professional with extended access"),
        ROLE_RESEARCHER("Researcher with access to anonymized data"),
        ROLE_ADMIN("System administrator with full access"),
        ROLE_MODERATOR("Content moderator for chat and community features");

        private final String description;

        RoleName(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}