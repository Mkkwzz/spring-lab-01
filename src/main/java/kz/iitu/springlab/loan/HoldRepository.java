package kz.iitu.springlab.loan;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldRepository extends JpaRepository<Hold, Long> {

    // is anybody queued for this book?
    boolean existsByBookId(Long bookId);
}
