package kz.iitu.springlab;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

// integration test: the whole application on a random port against a real PostgreSQL 17
// in a Docker container; Flyway V1..V4 run on an empty database every build.
// disabledWithoutDocker: on a machine without Docker the class is reported as skipped
// (with that reason) instead of silently falling back to H2.
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureRestTestClient
class LoanApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired RestTestClient client;

    @Test
    void opensALoanThenReadsItBackThroughTheApi() {
        // the loan API is protected (lab 10 users from V3__security.sql): HTTP Basic as "user"
        client.post().uri("/api/loans")
              .headers(h -> h.setBasicAuth("user", "user123"))
              .contentType(MediaType.APPLICATION_JSON)
              .body("{\"bookId\": 1, \"reader\": \"syrym\"}")
              .exchange()
              .expectStatus().isCreated();

        client.get().uri("/api/loans?reader=syrym")
              .headers(h -> h.setBasicAuth("user", "user123"))
              .exchange()
              .expectStatus().isOk()
              .expectBody()
              .jsonPath("$[0].bookId").isEqualTo(1)
              .jsonPath("$[0].reader").isEqualTo("syrym")
              .jsonPath("$[0].extensions").isEqualTo(0);
    }
}
