package kz.iitu.springlab.loan;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    // derived query: the reader's loans that are still out, one page at a time
    Page<Loan> findByReaderAndReturnedFalse(String reader, Pageable pageable);
}
