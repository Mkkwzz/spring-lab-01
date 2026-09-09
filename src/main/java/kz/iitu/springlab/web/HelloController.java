package kz.iitu.springlab.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
public class HelloController {

    @Value("${app.owner:unknown}")
    private String owner;

    @GetMapping("/hello")
    public Greeting hello(@RequestParam(defaultValue = "world") String name) {
        return new Greeting(
                "Hello, " + name + "!",
                owner,
                LocalDateTime.now()
        );
    }
    @GetMapping("/reverse")
    public ReverseResult reverse(@RequestParam(required = false) String text) {
        if (text == null || text.isEmpty()) {
            return new ReverseResult(null, null, "Parameter 'text' is required");
        }

        String reversed = new StringBuilder(text).reverse().toString();

        return new ReverseResult(text, reversed, null);
    }
    @GetMapping("/info")
    public Info info() {
        return new Info(
                owner,
                System.getProperty("java.version"),
                Runtime.getRuntime().availableProcessors()
        );
    }
    public record ReverseResult(
            String original,
            String reversed,
            String error
    ) {}
    public record Greeting(
            String message,
            String owner,
            LocalDateTime timestamp
    ) {}

    public record Info(
            String owner,
            String javaVersion,
            int cpuCores
    ) {}
}