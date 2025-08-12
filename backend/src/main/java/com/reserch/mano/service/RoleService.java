package com.reserch.mano.service;

import com.reserch.mano.model.Role;
import com.reserch.mano.model.User;

import java.util.List;
import java.util.Set;

public interface RoleService {

    Role getRoleByName(Role.RoleName roleName);

    Role createRole(Role.RoleName roleName, String description);

    void assignRoleToUser(Long userId, Role.RoleName roleName);

    void removeRoleFromUser(Long userId, Role.RoleName roleName);

    Set<Role> getUserRoles(Long userId);

    List<User> getUsersByRole(Role.RoleName roleName);

    boolean hasRole(Long userId, Role.RoleName roleName);

    void initializeDefaultRoles();
}
