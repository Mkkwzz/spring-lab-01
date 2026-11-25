package kz.iitu.springlab.web;

import kz.iitu.springlab.loan.LoanExtensionRefusedException;
import kz.iitu.springlab.loan.LoanNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// turns the loan exceptions into RFC 9457 problem details
@RestControllerAdvice(assignableTypes = LoanController.class)
public class LoanExceptionHandler {

    @ExceptionHandler(LoanNotFoundException.class)
    ProblemDetail notFound(LoanNotFoundException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("Loan not found");
        return problem;
    }

    @ExceptionHandler(LoanExtensionRefusedException.class)
    ProblemDetail refused(LoanExtensionRefusedException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problem.setTitle("Extension refused");
        return problem;
    }
}
