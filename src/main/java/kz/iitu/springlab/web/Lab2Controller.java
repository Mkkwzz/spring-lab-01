package kz.iitu.springlab.web;

import kz.iitu.springlab.config.ContainerReport;
import kz.iitu.springlab.notify.NotificationService;
import kz.iitu.springlab.notify.Notifier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/lab2")
public class Lab2Controller {

    private final NotificationService notifications;
    private final ContainerReport report;
    private final Notifier reversed;

    public Lab2Controller(
            NotificationService notifications,
            ContainerReport report,
            @Qualifier("reversed") Notifier reversed) {

        this.notifications = notifications;
        this.report = report;
        this.reversed = reversed;
    }

    @GetMapping("/notify")
    public Map<String, Object> notify(
            @RequestParam(defaultValue = "Hello") String text) {

        return Map.of(
                "primary", notifications.viaPrimary(text),
                "console", notifications.viaConsole(text),
                "all", notifications.viaAll(text),
                "beanNames", notifications.names()
        );
    }

    @GetMapping("/beans")
    public Map<String, Object> beans() {
        return Map.of(
                "allBeanNames", report.allBeanNames(),
                "notifierBeans", report.notifierBeans()
        );
    }

    @GetMapping("/custom")
    public String custom(
            @RequestParam(defaultValue = "Hello") String text) {

        return reversed.send(text);
    }
}