package kz.iitu.springlab.config;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ContainerReport {

    private final ApplicationContext ctx;

    public ContainerReport(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    public String[] allBeanNames() {
        return ctx.getBeanDefinitionNames();
    }

    public String[] notifierBeans() {
        return Arrays.stream(ctx.getBeanNamesForType(
                        kz.iitu.springlab.notify.Notifier.class))
                .toArray(String[]::new);
    }
}