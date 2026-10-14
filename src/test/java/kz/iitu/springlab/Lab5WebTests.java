package kz.iitu.springlab;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class Lab5WebTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void listAndFilter() throws Exception {
        mvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
        mvc.perform(get("/api/books").param("author", "Bloch").param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Effective Java"));
    }

    @Test
    void findOneOrNotFound() throws Exception {
        mvc.perform(get("/api/books/1")).andExpect(status().isOk());
        mvc.perform(get("/api/books/999")).andExpect(status().isNotFound());
    }

    @Test
    void createReplaceDelete() throws Exception {
        mvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Pro Spring 6\",\"author\":\"Cosmina\",\"year\":2023}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/books/4"));
        mvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Effective Java 3rd\",\"author\":\"Joshua Bloch\",\"year\":2018}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Effective Java 3rd"));
        mvc.perform(put("/api/books/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"X\",\"author\":\"Y\",\"year\":2000}"))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/books/2")).andExpect(status().isNoContent());
        mvc.perform(delete("/api/books/2")).andExpect(status().isNotFound());
    }

    @Test
    void variant2ByYear() throws Exception {
        mvc.perform(get("/api/books/by-year/2018"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/books/by-year/1999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/books/by-year/18")).andExpect(status().isNotFound());
        mvc.perform(get("/api/books/by-year/abcd")).andExpect(status().isNotFound());
    }

    @Test
    void errorStatuses() throws Exception {
        mvc.perform(get("/api/books/abc")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/nothing")).andExpect(status().isNotFound());
        mvc.perform(delete("/api/books")).andExpect(status().isMethodNotAllowed());
        mvc.perform(post("/api/books").contentType(MediaType.TEXT_PLAIN).content("x"))
                .andExpect(status().isUnsupportedMediaType());
        mvc.perform(get("/api/books").accept(MediaType.APPLICATION_XML))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void pageAndPostRedirectGet() throws Exception {
        mvc.perform(get("/books").param("author", "Bloch"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"))
                .andExpect(model().attribute("books", hasSize(1)));
        mvc.perform(post("/books")
                        .param("title", "Java Precisely")
                        .param("author", "Sestoft")
                        .param("year", "2016"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/books"))
                .andExpect(flash().attribute("message", "Book «Java Precisely» has been added"));
        mvc.perform(get("/css/app.css"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/css"));
    }
}
