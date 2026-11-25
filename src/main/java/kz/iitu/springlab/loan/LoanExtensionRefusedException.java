package kz.iitu.springlab.loan;

public class LoanExtensionRefusedException extends RuntimeException {

    public LoanExtensionRefusedException(long id, String reason) {
        super("Loan " + id + " cannot be extended: " + reason);
    }
}
