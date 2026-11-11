package kz.iitu.springlab.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// Lab 10, step 2.2: prints BCrypt hashes for the two teaching passwords.
// Run it twice and compare: the same password gives a different hash each time.
public class HashGenerator {

    public static void main(String[] args) {
        var encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("user123"));
        System.out.println(encoder.encode("admin123"));
    }
}
