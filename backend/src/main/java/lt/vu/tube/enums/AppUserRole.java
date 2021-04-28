package lt.vu.tube.enums;

import com.google.common.collect.Sets;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;
import static lt.vu.tube.enums.ApplicationUserPermission.*;

public enum AppUserRole {
    USER(Sets.newHashSet(USER_READ, USER_WRITE)),
    ADMIN(Sets.newHashSet(USER_READ, USER_WRITE));

    private final Set<ApplicationUserPermission> permisions;

    AppUserRole(Set<ApplicationUserPermission> permisions){
        this.permisions = permisions;
    }

    public Set<ApplicationUserPermission> getPermissions(){
        return permisions;
    }

    public Set<SimpleGrantedAuthority> getGrantedAuthorities() {
        Set<SimpleGrantedAuthority> permissions = getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toSet());
        permissions.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return permissions;
    }
}