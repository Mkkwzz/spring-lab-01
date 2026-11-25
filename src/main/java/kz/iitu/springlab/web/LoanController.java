package kz.iitu.springlab.web;

import jakarta.validation.Valid;
import kz.iitu.springlab.loan.Loan;
import kz.iitu.springlab.loan.LoanService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService service;

    public LoanController(LoanService service) {
        this.service = service;
    }

    // GET /api/loans/{id} -> 200, or 404 with a problem detail
    @GetMapping("/{id}")
    public Loan byId(@PathVariable long id) {
        return service.byId(id);
    }

    // GET /api/loans?reader=...&page=0&size=10 -> the reader's active loans, earliest due date first
    @GetMapping
    public List<Loan> activeLoans(@RequestParam String reader,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        return service.activeLoansOf(reader, PageRequest.of(page, size, Sort.by("dueOn"))).getContent();
    }

    // POST /api/loans -> 201 + Location, or 400 when the body breaks the constraints
    @PostMapping
    public ResponseEntity<Loan> open(@Valid @RequestBody LoanRequest request) {
        Loan loan = service.open(request.bookId(), request.reader());
        return ResponseEntity.created(URI.create("/api/loans/" + loan.getId())).body(loan);
    }

    // POST /api/loans/{id}/extend -> 200, 404 or 409
    @PostMapping("/{id}/extend")
    public Loan extend(@PathVariable long id) {
        return service.extend(id);
    }
}
