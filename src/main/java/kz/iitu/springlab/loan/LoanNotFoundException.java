package kz.iitu.springlab.loan;

public class LoanNotFoundException extends RuntimeException {

    public LoanNotFoundException(long id) {
        super("Loan " + id + " does not exist");
    }
}
