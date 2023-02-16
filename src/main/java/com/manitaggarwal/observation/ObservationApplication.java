package com.manitaggarwal.observation;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Date;
import java.util.Locale;
import java.util.Objects;

@SpringBootApplication
@EnableScheduling
public class ObservationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ObservationApplication.class, args);
    }

}

@Service
class GreetingService {
    private final ObservationRegistry registry;

    GreetingService(ObservationRegistry registry) {
        this.registry = registry;
    }

    public ResponseEntity<Greeting> getGreeting(@PathVariable String name) {
        if (!Character.isUpperCase(name.charAt(0))) {
            throw Observation
                    .createNotStarted("greeting.name.error", registry)
                    .observe(() -> new IllegalArgumentException("First letter should be capital. "));
        }
        return Observation
                .createNotStarted("greeting.name", registry)
                .observe(() -> ResponseEntity
                        .status(HttpStatus.OK).body(new Greeting("Hello, " + name + "!")));
    }
}

@Service
class SchedulerService {

    private final GreetingService greetingService;

    SchedulerService(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @Scheduled(cron = "*/5 * * * * *")
    public void callGreeting() {
        String name = "World";
        if ((new Date().getTime() % 2) == 0) {
            name = name.toLowerCase(Locale.ROOT);
        }
        System.out.println(Objects.requireNonNull(
                greetingService.getGreeting(name).getBody()).message());
    }
}

record Greeting(String message) {
}