package kz.iitu.springlab.loan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

// a reader waiting in the queue for a book that is out on loan
@Entity
@Table(name = "hold")
public class Hold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(nullable = false, length = 60)
    private String reader;

    @Column(name = "placed_on", nullable = false)
    private LocalDate placedOn;

    protected Hold() {
    }

    public Hold(Long bookId, String reader, LocalDate placedOn) {
        this.bookId = bookId;
        this.reader = reader;
        this.placedOn = placedOn;
    }

    public Long getId() { return id; }
    public Long getBookId() { return bookId; }
    public String getReader() { return reader; }
    public LocalDate getPlacedOn() { return placedOn; }
}
