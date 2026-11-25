package kz.iitu.springlab.loan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "loan")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(nullable = false, length = 60)
    private String reader;

    @Column(name = "issued_on", nullable = false)
    private LocalDate issuedOn;

    @Column(name = "due_on", nullable = false)
    private LocalDate dueOn;

    @Column(nullable = false)
    private int extensions;

    @Column(nullable = false)
    private boolean returned;

    protected Loan() {
    }

    public Loan(Long bookId, String reader, LocalDate issuedOn, LocalDate dueOn) {
        this.bookId = bookId;
        this.reader = reader;
        this.issuedOn = issuedOn;
        this.dueOn = dueOn;
    }

    // moves the due date and counts the extension
    public void extendBy(int days) {
        this.dueOn = dueOn.plusDays(days);
        this.extensions++;
    }

    public void markReturned() {
        this.returned = true;
    }

    public Long getId() { return id; }
    public Long getBookId() { return bookId; }
    public String getReader() { return reader; }
    public LocalDate getIssuedOn() { return issuedOn; }
    public LocalDate getDueOn() { return dueOn; }
    public int getExtensions() { return extensions; }
    public boolean isReturned() { return returned; }
}
