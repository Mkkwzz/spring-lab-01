package kz.iitu.springlab.catalog;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

// Books now live in the "book" table (Flyway V1/V2) instead of a HashMap.
@Repository
public class BookRepository {

    private static final String SELECT = "select id, title, author, published_year from book";

    private final JdbcClient jdbc;

    public BookRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<Book> findAll() {
        return jdbc.sql(SELECT + " order by id").query(this::map).list();
    }

    public Optional<Book> findById(long id) {
        return jdbc.sql(SELECT + " where id = ?").param(id).query(this::map).optional();
    }

    public boolean existsById(long id) {
        return findById(id).isPresent();
    }

    public Book save(Book book) {
        if (book.id() != null) {
            jdbc.sql("update book set title = ?, author = ?, published_year = ? where id = ?")
                    .params(book.title(), book.author(), book.year(), book.id())
                    .update();
            return book;
        }
        var keys = new GeneratedKeyHolder();
        jdbc.sql("insert into book (title, author, published_year) values (?, ?, ?)")
                .params(book.title(), book.author(), book.year())
                .update(keys, "id");
        return new Book(keys.getKey().longValue(), book.title(), book.author(), book.year());
    }

    public boolean deleteById(long id) {
        return jdbc.sql("delete from book where id = ?").param(id).update() > 0;
    }

    private Book map(ResultSet rs, int row) throws SQLException {
        return new Book(rs.getLong("id"), rs.getString("title"),
                rs.getString("author"), rs.getInt("published_year"));
    }
}
