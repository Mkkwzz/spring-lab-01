package kz.iitu.springlab.loan;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

// data slice: JPA + Flyway against the embedded H2 database (PostgreSQL mode).
// This is NOT the integration test; the real PostgreSQL run is LoanApiIntegrationTest.
// Book ids 1..3 come from the Flyway seed V2__seed_books.sql.
@DataJpaTest
class LoanRepositoryTest {

    @Autowired LoanRepository repository;
    @Autowired TestEntityManager em;

    private Loan loan(long bookId, String reader, LocalDate due) {
        return new Loan(bookId, reader, due.minusDays(14), due);
    }

    @Test
    void findsTheLoansAReaderStillHas() {
        em.persist(loan(1L, "syrym", LocalDate.of(2026, 12, 1)));
        em.persist(loan(2L, "syrym", LocalDate.of(2026, 12, 5)));
        em.flush();
        em.clear();          // force the query to hit the database

        Page<Loan> found = repository.findByReaderAndReturnedFalse("syrym", PageRequest.of(0, 10));

        assertThat(found.getContent())
                .extracting(Loan::getBookId)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void excludesReturnedLoansAndOtherReadersLoans() {
        Loan returned = loan(1L, "syrym", LocalDate.of(2026, 12, 1));
        returned.markReturned();
        em.persist(returned);
        em.persist(loan(2L, "aliya", LocalDate.of(2026, 12, 5)));
        em.flush();
        em.clear();

        Page<Loan> found = repository.findByReaderAndReturnedFalse("syrym", PageRequest.of(0, 10));

        assertThat(found.getContent()).isEmpty();
        assertThat(found.getTotalElements()).isZero();
    }

    // variant 2: pagination assertion
    @Test
    void splitsTheActiveLoansIntoPagesOrderedByDueDate() {
        em.persist(loan(3L, "syrym", LocalDate.of(2026, 12, 9)));
        em.persist(loan(1L, "syrym", LocalDate.of(2026, 12, 1)));
        em.persist(loan(2L, "syrym", LocalDate.of(2026, 12, 5)));
        em.flush();
        em.clear();

        PageRequest firstTwo = PageRequest.of(0, 2, Sort.by("dueOn"));
        Page<Loan> first = repository.findByReaderAndReturnedFalse("syrym", firstTwo);
        Page<Loan> second = repository.findByReaderAndReturnedFalse("syrym", firstTwo.next());

        assertThat(first.getTotalElements()).isEqualTo(3);
        assertThat(first.getTotalPages()).isEqualTo(2);
        assertThat(first.getContent()).extracting(Loan::getBookId).containsExactly(1L, 2L);
        assertThat(first.hasNext()).isTrue();
        assertThat(second.getContent()).extracting(Loan::getBookId).containsExactly(3L);
        assertThat(second.isLast()).isTrue();
    }
}
