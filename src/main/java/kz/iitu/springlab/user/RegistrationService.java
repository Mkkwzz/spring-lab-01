package kz.iitu.springlab.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

// Variant 2: self-registration. The password is hashed before it is saved; a duplicate name is rejected.
@Service
public class RegistrationService {

    private final AppUserRepository users;
    private final PasswordEncoder encoder;

    public RegistrationService(AppUserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Transactional
    public AppUser register(String username, String rawPassword) {
        if (users.existsByUsername(username)) {
            throw new UsernameTakenException(username);
        }
        // a new account always gets the lowest role; nobody can register as ADMIN
        return users.save(new AppUser(username, encoder.encode(rawPassword), Set.of("USER")));
    }

    public static class UsernameTakenException extends RuntimeException {
        public UsernameTakenException(String username) {
            super("Username '" + username + "' is already taken");
        }
    }
}
