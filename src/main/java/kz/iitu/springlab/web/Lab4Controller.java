package kz.iitu.springlab.web;

import kz.iitu.springlab.aspect.SlowCallRegistry;
import kz.iitu.springlab.service.CatalogService;
import org.springframework.aop.support.AopUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lab4")
public class Lab4Controller {

    private final CatalogService catalogService;
    private final SlowCallRegistry slowCallRegistry;

    public Lab4Controller(CatalogService catalogService, SlowCallRegistry slowCallRegistry) {
        this.catalogService = catalogService;
        this.slowCallRegistry = slowCallRegistry;
    }

    @GetMapping("/item/{id}")
    public String item(@PathVariable long id) {
        return catalogService.findById(id);
    }

    @GetMapping("/items")
    public List<String> items(@RequestParam(defaultValue = "5") int limit) {
        return catalogService.findAll(limit);
    }

    @DeleteMapping("/item/{id}")
    public String remove(@PathVariable long id) {
        return catalogService.remove(id);
    }

    @GetMapping("/remove-twice/{id}")
    public String removeTwice(@PathVariable long id) {
        return catalogService.removeTwice(id);
    }

    @GetMapping("/remove-twice-fixed/{id}")
    public String removeTwiceFixed(@PathVariable long id) {
        return catalogService.removeTwiceFixed(id);
    }

    @GetMapping("/proxy")
    public Map<String, String> proxyInfo() {
        Class<?> beanClass = catalogService.getClass();
        return Map.of(
                "className", beanClass.getName(),
                "superClass", beanClass.getSuperclass().getSimpleName(),
                "targetClass", AopUtils.getTargetClass(catalogService).getName(),
                "isAopProxy", String.valueOf(AopUtils.isAopProxy(catalogService)),
                "isCglib", String.valueOf(AopUtils.isCglibProxy(catalogService)));
    }

    @GetMapping("/slow-calls")
    public Map<String, Object> slowCalls() {
        var calls = slowCallRegistry.findAll();
        return Map.of("count", calls.size(), "calls", calls);
    }
}
