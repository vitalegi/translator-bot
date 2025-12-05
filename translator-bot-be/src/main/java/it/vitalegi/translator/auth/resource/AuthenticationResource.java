package it.vitalegi.translator.auth.resource;

import it.vitalegi.translator.model.UserIdentity;
import it.vitalegi.translator.model.UserPermissions;
import it.vitalegi.translator.auth.service.AuthenticationService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
@AllArgsConstructor
public class AuthenticationResource {

    AuthenticationService authenticationService;

    @GetMapping("/identity")
    public UserIdentity identity() {
        return authenticationService.identity();
    }

    @GetMapping("/permissions")
    public UserPermissions permissions() {
        return new UserPermissions(authenticationService.getPermissions().toList());
    }
}
