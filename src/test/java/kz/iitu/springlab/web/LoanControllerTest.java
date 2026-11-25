package kz.iitu.springlab.web;

import kz.iitu.springlab.loan.Loan;
import kz.iitu.springlab.loan.LoanNotFoundException;
import kz.iitu.springlab.loan.LoanService;
import kz.iitu.springlab.security.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// web slice: only the controller, the advice and the real security chain are loaded;
// the service is a Mockito mock
@WebMvcTest(LoanController.class)
@Import(SecurityConfiguration.class)
class LoanControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean LoanService service;

    @Test
    @WithMockUser(roles = "USER")
    void anExistingLoanIsReturnedAsJson() throws Exception {
        given(service.byId(5L)).willReturn(
                new Loan(1L, "syrym", LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 16)));

        mockMvc.perform(get("/api/loans/5"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.reader").value("syrym"))
               .andExpect(jsonPath("$.bookId").value(1))
               .andExpect(jsonPath("$.dueOn").value("2026-11-16"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void aMissingLoanBecomes404WithAProblemDetail() throws Exception {
        given(service.byId(7L)).willThrow(new LoanNotFoundException(7L));

        mockMvc.perform(get("/api/loans/7"))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.status").value(404))
               .andExpect(jsonPath("$.title").value("Loan not found"))
               .andExpect(jsonPath("$.detail").value("Loan 7 does not exist"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void aLoanRequestThatBreaksTheConstraintsIsRejectedWith400() throws Exception {
        mockMvc.perform(post("/api/loans")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("{\"bookId\": -1, \"reader\": \"\"}"))
               .andExpect(status().isBadRequest());

        verifyNoInteractions(service);   // validation stopped the request before the service
    }

    @Test
    void anAnonymousCallerIsRejectedWith401() throws Exception {
        mockMvc.perform(get("/api/loans/5"))
               .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }
}
