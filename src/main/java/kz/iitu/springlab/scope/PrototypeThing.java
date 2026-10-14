package kz.iitu.springlab.scope;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope("prototype")
public class PrototypeThing {

    private final String id = UUID.randomUUID().toString();

    public String id() {
        return id;
    }
}