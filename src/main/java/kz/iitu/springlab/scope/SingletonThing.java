package kz.iitu.springlab.scope;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SingletonThing {

    private final String id = UUID.randomUUID().toString();

    public String id() {
        return id;
    }
}
