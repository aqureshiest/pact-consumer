package com.example.consumer;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRuleMk2;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponsePact;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static io.pactfoundation.consumer.dsl.LambdaDsl.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class ProviderPactTests {

    @Autowired
    RestTemplate restTemplate;

    @Rule
    public PactProviderRuleMk2 pactProvider = new PactProviderRuleMk2("provider", null, 8081, this);

    @Pact(consumer = "consumer")
    public RequestResponsePact pactProvider(PactDslWithProvider builder) {
        return builder
                .given("provider has people loaded in db")
                .uponReceiving("request for people list")
                .path("/people")
                .method("GET")
                .willRespondWith()
                .status(200)
                .body(newJsonArrayMinLike(1, (o) -> o.object((o1) -> {
                    o1.numberType("id");
                    o1.stringType("first");
                    o1.stringType("last");
                    o1.numberType("age");
                    o1.array("likes", (o2) -> o2.stringType("running"));
                })).build())
                .toPact();
    }

    @Pact(consumer = "consumer", state = "test user exists")
    public RequestResponsePact pactProviderWithTestUser(PactDslWithProvider builder) {
        return builder
                .given("test user exists")
                .uponReceiving("request for specific person with id")
                .path("/people/0")
                .method("GET")
                .willRespondWith()
                .status(200)
                .body("{\"id\": 0, \"first\": \"test\", \"last\": \"user\", \"age\": 30}", "application/json")
                .toPact();
    }

    @Autowired
    ProviderClient client;

    @PactVerification(fragment = "pactProvider")
    @Test
    public void get_people() throws JsonProcessingException {
        List<String> names = client.processPeople();
        Assertions.assertThat(names).isNotEmpty();
        Assertions.assertThat(names.iterator().next()).isNotEmpty();
    }

    @PactVerification(fragment = "pactProviderWithTestUser")
    @Test
    public void get_person() throws JsonProcessingException {
        String name = client.processPerson(0);
        Assertions.assertThat(name).isNotEmpty();
    }
}
