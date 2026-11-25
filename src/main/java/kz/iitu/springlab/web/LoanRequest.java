package kz.iitu.springlab.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// body of POST /api/loans
public record LoanRequest(
        @NotNull @Positive Long bookId,
        @NotBlank @Size(max = 60) String reader) {
}
