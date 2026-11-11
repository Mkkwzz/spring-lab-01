package kz.iitu.springlab;

import kz.iitu.springlab.catalog.BookService;
import kz.iitu.springlab.user.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class Lab10SecurityTests {

    @Autowired MockMvc mvc;
    @Autowired BookService books;
    @Autowired AppUserRepository users;
    @Autowired PasswordEncoder encoder;

    @Test
    void anonymousIsRedirectedToLogin() throws Exception {
        mvc.perform(get("/books")).andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void databaseUsersLogInWithBcryptPasswords() throws Exception {
        mvc.perform(formLogin().user("user").password("user123")).andExpect(authenticated().withRoles("USER"));
        mvc.perform(formLogin().user("admin").password("admin123")).andExpect(authenticated().withRoles("ADMIN", "USER"));
        mvc.perform(formLogin().user("user").password("wrong")).andExpect(unauthenticated());
        assertThat(users.findByUsername("user").orElseThrow().getPasswordHash()).startsWith("$2a$10$").hasSize(60);
    }

    @Test
    @WithMockUser(roles = "USER")
    void userIsForbiddenFromAdminAndFromCreatingBooks() throws Exception {
        mvc.perform(get("/books")).andExpect(status().isOk());
        mvc.perform(get("/admin")).andExpect(status().isForbidden());
        mvc.perform(post("/books").with(csrf()).param("title", "T").param("author", "A").param("year", "2020"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/nowhere")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void adminIsAllowedAndCsrfTokenIsRequired() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().isOk());
        mvc.perform(post("/books").param("title", "Forged").param("author", "A").param("year", "2020"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/books").with(csrf()).param("title", "Genuine").param("author", "A").param("year", "2020"))
                .andExpect(redirectedUrl("/books"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void preAuthorizeProtectsTheServiceEvenWithoutAUrlRule() {
        // no controller and no URL rule involved: the call goes straight to the service
        assertThatThrownBy(() -> books.delete(1)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void variant2RegistrationHashesThePasswordAndRejectsDuplicates() throws Exception {
        mvc.perform(post("/register").with(csrf()).param("username", "syrym").param("password", "user123"))
                .andExpect(redirectedUrl("/login?registered"));
        var saved = users.findByUsername("syrym").orElseThrow();
        assertThat(saved.getPasswordHash()).isNotEqualTo("user123").startsWith("$2a$10$");
        assertThat(encoder.matches("user123", saved.getPasswordHash())).isTrue();
        assertThat(saved.getRoles()).containsExactly("USER");

        mvc.perform(post("/register").with(csrf()).param("username", "syrym").param("password", "other123"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Username 'syrym' is already taken"));
        mvc.perform(formLogin().user("syrym").password("user123")).andExpect(authenticated().withRoles("USER"));
    }
}
