package lt.vu.tube.services;


import lombok.AllArgsConstructor;
import lt.vu.tube.entity.AppUser;
import lt.vu.tube.enums.AppUserRole;
import lt.vu.tube.requests.RegistrationRequest;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RegistrationService {

    private final AppUserService appUserService;
    private final EmailValidator emailValidator;

    public String register(RegistrationRequest request) {
        boolean isValidEmail = emailValidator.
                test(request.getEmail());

        if (!isValidEmail) {
            throw new IllegalStateException("email not valid");
        }

        String response = appUserService.signUpUser(
                new AppUser(
                        request.getFirstName(),
                        request.getLastName(),
                        request.getEmail(),
                        request.getPassword(),
                        AppUserRole.USER
                )
        );
        return response;
    }
}
