package kz.iitu.springlab.catalog;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    // author == null -> все книги, иначе фильтр по фрагменту имени автора без учёта регистра
    public List<Book> findAll(String author) {
        if (author == null || author.isBlank()) {
            return repository.findAll();
        }
        String needle = author.toLowerCase(Locale.ROOT);
        return repository.findAll().stream()
                .filter(b -> b.author().toLowerCase(Locale.ROOT).contains(needle))
                .toList();
    }

    // Вариант 2: книги заданного года издания
    public List<Book> findByYear(int year) {
        return repository.findAll().stream()
                .filter(b -> b.year() == year)
                .toList();
    }

    public Optional<Book> findById(long id) {
        return repository.findById(id);
    }

    public Book create(Book book) {
        // id из тела запроса игнорируется: новый идентификатор выдаёт репозиторий
        return repository.save(new Book(null, book.title(), book.author(), book.year()));
    }

    public Optional<Book> replace(long id, Book book) {
        if (!repository.existsById(id)) {
            return Optional.empty();
        }
        return Optional.of(repository.save(new Book(id, book.title(), book.author(), book.year())));
    }

    public boolean delete(long id) {
        return repository.deleteById(id);
    }
}
