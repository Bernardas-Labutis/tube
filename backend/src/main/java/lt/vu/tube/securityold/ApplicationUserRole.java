//package lt.vu.tube.securityold;
//
//import com.google.common.collect.Sets;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import static lt.vu.tube.securityold.ApplicationUserPermission.*;
//
//public enum ApplicationUserRole {
//    USER(Sets.newHashSet()),
//    ADMIN(Sets.newHashSet(USER_READ, USER_WRITE));
//
//    private final Set<ApplicationUserPermission> permisions;
//
//    ApplicationUserRole(Set<ApplicationUserPermission> permisions){
//        this.permisions = permisions;
//    }
//
//    public Set<ApplicationUserPermission> getPermissions(){
//        return permisions;
//    }
//
//    public Set<SimpleGrantedAuthority> getGrantedAuthorities() {
//        Set<SimpleGrantedAuthority> permissions = getPermissions().stream()
//                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
//                .collect(Collectors.toSet());
//        permissions.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
//        return permissions;
//    }
//}
