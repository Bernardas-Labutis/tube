package lt.vu.tube.security;

import com.google.common.collect.Sets;

import java.util.Set;

import static lt.vu.tube.security.ApplicationUserPermission.*;

public enum ApplicationUserRole {
    USER(Sets.newHashSet()),
    ADMIN(Sets.newHashSet(USER_READ, USER_WRITE));

    private final Set<ApplicationUserPermission> permisions;

    ApplicationUserRole(Set<ApplicationUserPermission> permisions){
        this.permisions = permisions;
    }

    public Set<ApplicationUserPermission> getPermisions(){
        return permisions;
    }
}
