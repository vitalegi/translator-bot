package it.vitalegi.translator.exception;

import it.vitalegi.translator.auth.model.Permission;
import lombok.Getter;

import java.util.List;

@Getter
public class UnauthorizedAccessException extends RuntimeException {
    List<Permission> userPermissions;
    Permission missingPermission;

    public UnauthorizedAccessException(List<Permission> userPermissions, Permission missingPermission) {
        super("Missing permission " + missingPermission + ". Available: " + userPermissions);
        this.userPermissions = userPermissions;
        this.missingPermission = missingPermission;
    }
}
