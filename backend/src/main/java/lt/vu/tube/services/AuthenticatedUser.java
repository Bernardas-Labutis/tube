package lt.vu.tube.services;

import lombok.AllArgsConstructor;
import lt.vu.tube.entity.AppUser;
import lt.vu.tube.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthenticatedUser {
    private final static String USER_NOT_FOUND_MSG =
            "user with email %s not found";

    private final AppUserRepository appUserRepository;

    public AppUser getAuthenticatedUser() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return null;
        }
        Object email = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (email instanceof String) {
            return appUserRepository.findByEmail(((String)email))
                    .orElseThrow(() ->
                            new UsernameNotFoundException(
                                    String.format(USER_NOT_FOUND_MSG, email)));
        } else {
            return null;
        }
    }
}
