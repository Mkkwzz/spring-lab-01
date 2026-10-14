package kz.iitu.springlab.web;

import kz.iitu.springlab.scope.PrototypeThing;
import kz.iitu.springlab.scope.SingletonThing;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/lab2")
public class ScopeController {

    private final SingletonThing singleton;
    private final ObjectProvider<PrototypeThing> prototypes;

    public ScopeController(
            SingletonThing singleton,
            ObjectProvider<PrototypeThing> prototypes) {

        this.singleton = singleton;
        this.prototypes = prototypes;
    }

    @GetMapping("/scopes")
    public Map<String, Object> scopes() {

        return Map.of(
                "singleton1", singleton.id(),
                "singleton2", singleton.id(),
                "prototype1", prototypes.getObject().id(),
                "prototype2", prototypes.getObject().id()
        );
    }
}