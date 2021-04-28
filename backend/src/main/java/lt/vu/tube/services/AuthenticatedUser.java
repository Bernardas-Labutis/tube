package lt.vu.tube.services;

import lt.vu.tube.entity.AppUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticatedUser {
    public static AppUser getAuthenticatedUser(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(principal instanceof AppUser) {
            return ((AppUser) principal);
        }else {
            return null;
        }
    }
}
