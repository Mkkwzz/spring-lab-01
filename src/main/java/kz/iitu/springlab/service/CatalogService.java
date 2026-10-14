package kz.iitu.springlab.service;

import kz.iitu.springlab.audit.Audited;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CatalogService {

    private final CatalogService self;

    private final List<String> items = new ArrayList<>(
            List.of(
                    "Item no. 1",
                    "Item no. 2",
                    "Item no. 3",
                    "Item no. 4",
                    "Item no. 5"
            )
    );

    public CatalogService(@Lazy CatalogService self) {
        this.self = self;
    }

    public String findById(long id) {
        sleep(50);
        return "Item no. " + id;
    }

    @Audited(action = "CATALOG_LIST", logArguments = true)
    public List<String> findAll(int limit) {
        sleep(300);

        return items.stream()
                .limit(limit)
                .toList();
    }

    @Audited(action = "CATALOG_REMOVE")
    public String remove(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Invalid identifier: " + id
            );
        }

        return "Removed item no. " + id;
    }

    // Демонстрация self-invocation:
    // внутренний вызов метода не проходит через Spring AOP proxy
    public String removeTwice(long id) {
        String first = remove(id);
        String second = remove(id + 1);

        return first + "; " + second;
    }

    // Исправленный вариант:
    // вызов через self проходит через Spring AOP proxy
    public String removeTwiceFixed(long id) {
        String first = self.remove(id);
        String second = self.remove(id + 1);

        return first + "; " + second;
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}