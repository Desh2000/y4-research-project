package com.reserch.mano.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Permission Entity for fine-grained access control
 */
@Entity
@Table(name = "permissions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "resource"})
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Permission extends BaseEntity {

    @NotBlank
    @Column(name = "name", nullable = false, length = 50)
    private String name; // e.g., "READ", "WRITE", "DELETE"

    @NotBlank
    @Column(name = "resource", nullable = false, length = 50)
    private String resource; // e.g., "USER", "ML_MODEL", "REPORT"

    @Column(name = "description", length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    // Constructor for creating permission with name and resource
    public Permission(String name, String resource) {
        this.name = name;
        this.resource = resource;
    }

    // Constructor for creating permission with name, resource and description
    public Permission(String name, String resource, String description) {
        this.name = name;
        this.resource = resource;
        this.description = description;
    }
}
