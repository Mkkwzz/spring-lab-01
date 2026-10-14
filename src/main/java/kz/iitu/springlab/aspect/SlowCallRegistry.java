package kz.iitu.springlab.aspect;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SlowCallRegistry {

    private final List<SlowCall> calls = new CopyOnWriteArrayList<>();

    public void record(SlowCall call) {
        calls.add(call);
    }

    public List<SlowCall> findAll() {
        return List.copyOf(calls);
    }
}
