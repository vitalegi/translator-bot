package it.vitalegi.translator.model;

import it.vitalegi.translator.auth.model.Permission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissions {
    List<Permission> permissions;
}
