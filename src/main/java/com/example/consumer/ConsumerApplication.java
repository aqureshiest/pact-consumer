package com.example.consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
public class ConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Profile("!test")
    @Bean
    ApplicationRunner init(ProviderClient client) {
        return args -> {
            client.processPeople();
        };
    }
}

@Component
class ProviderClient {

    private final RestTemplate restTemplate;
    private final String providerUrl;

    ProviderClient(RestTemplate restTemplate, @Value("${provider.url}") String server) {
        this.restTemplate = restTemplate;
        this.providerUrl = server;
    }

    public List<String> processPeople() {
        Person[] people = restTemplate.getForObject(providerUrl + "/people", Person[].class);
        return Stream.of(people)
                .map(p -> p.getFirst() + " " + p.getLast())
                .collect(Collectors.toList());
    }

    public String processPerson(int id) {
        Person p = restTemplate.getForObject(providerUrl + "/people/" + id, Person.class);
        return p.getFirst() + " " + p.getLast();
    }
}

