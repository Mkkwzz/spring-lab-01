package kz.iitu.springlab.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

// switches on @PreAuthorize: the rule travels with the service method
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfiguration {
}
