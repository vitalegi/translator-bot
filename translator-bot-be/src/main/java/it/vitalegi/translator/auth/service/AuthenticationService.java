package it.vitalegi.translator.auth.service;

import it.vitalegi.translator.configuration.RbacProperties;
import it.vitalegi.translator.exception.UnauthorizedAccessException;
import it.vitalegi.translator.auth.model.Permission;
import it.vitalegi.translator.model.UserIdentity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@Slf4j
public class AuthenticationService {

    Map<String, List<Permission>> rbac;

    public AuthenticationService(RbacProperties rbacProperties) {
        rbac = new HashMap<>(rbacProperties.getRbac());
    }

    public UserIdentity identity() {
        var principal = getPrincipal();
        return UserIdentity.builder().id(getSubject(principal)).roles(getGroups(principal)).build();
    }

    public void checkPermission(Permission permission) {
        if (!hasPermission(permission)) {
            throw new UnauthorizedAccessException(getPermissions().toList(), permission);
        }
    }

    public boolean hasPermission(Permission permission) {
        return getPermissions().anyMatch(p -> p == permission);
    }


    public Stream<Permission> getPermissions() {
        var roles = getGroups(getPrincipal());
        if (roles == null || roles.isEmpty()) {
            return Stream.empty();
        }
        return roles.stream().flatMap(this::getPermissions).distinct();
    }


    protected String getSubject(Jwt jwt) {
        return jwt.getSubject();
    }

    protected List<String> getGroups(Jwt jwt) {
        if (jwt == null) {
            return Collections.emptyList();
        }
        return jwt.getClaimAsStringList("cognito:groups");
    }

    protected Jwt getPrincipal() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        if (authentication.getPrincipal() instanceof Jwt) {
            return (Jwt) authentication.getPrincipal();
        }
        return null;
    }


    public Stream<Permission> getPermissions(String role) {
        var permissions = rbac.get(role);
        if (permissions == null) {
            return Stream.empty();
        }
        return permissions.stream();
    }
}
