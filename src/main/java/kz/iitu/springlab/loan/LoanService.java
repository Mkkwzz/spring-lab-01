package kz.iitu.springlab.loan;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class LoanService {

    static final int LOAN_DAYS = 14;
    static final int MAX_EXTENSIONS = 2;

    private final LoanRepository loans;
    private final HoldRepository holds;

    public LoanService(LoanRepository loans, HoldRepository holds) {
        this.loans = loans;
        this.holds = holds;
    }

    public Loan byId(long id) {
        return loans.findById(id).orElseThrow(() -> new LoanNotFoundException(id));
    }

    public Page<Loan> activeLoansOf(String reader, Pageable pageable) {
        return loans.findByReaderAndReturnedFalse(reader, pageable);
    }

    public Loan open(long bookId, String reader) {
        LocalDate today = LocalDate.now();
        return loans.save(new Loan(bookId, reader, today, today.plusDays(LOAN_DAYS)));
    }

    // variant 2: a loan may not be extended while other readers are queued for the book
    @Transactional
    public Loan extend(long id) {
        Loan loan = byId(id);
        if (loan.isReturned()) {
            throw new LoanExtensionRefusedException(id, "the book has already been returned");
        }
        if (holds.existsByBookId(loan.getBookId())) {
            throw new LoanExtensionRefusedException(id, "other readers are queued for this book");
        }
        if (loan.getExtensions() >= MAX_EXTENSIONS) {
            throw new LoanExtensionRefusedException(id, "the limit of " + MAX_EXTENSIONS + " extensions is reached");
        }
        loan.extendBy(LOAN_DAYS);
        return loans.save(loan);
    }
}
