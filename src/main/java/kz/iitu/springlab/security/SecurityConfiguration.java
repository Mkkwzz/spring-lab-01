package kz.iitu.springlab.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/login", "/css/**", "/error").permitAll()
                .requestMatchers("/books/**").authenticated()
                .anyRequest().denyAll())
            .formLogin(form -> form.permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/?logout"));
        return http.build();
    }

    @Bean                                 // TEMPORARY — removed in task 2
    UserDetailsService users() {
        return new InMemoryUserDetailsManager(
                User.withUsername("user")
                    .password("{noop}user123").roles("USER").build());
    }
}
