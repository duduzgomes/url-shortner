package com.eduardogomes.shortner_url;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.cassandra.CassandraContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.eduardogomes.shortner_url.controllers.dto.ShortenResponse;
import com.eduardogomes.shortner_url.models.ShortenRequest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class UrlIT {

    @LocalServerPort
    private int port;

    private RestTestClient client;

    @Container
    @SuppressWarnings("resource")
    static CassandraContainer cassandra = new CassandraContainer(
        DockerImageName.parse("cassandra:5.0"))
        .withReuse(true)
        .withInitScript("schema.cql")
        .withStartupTimeout(Duration.ofMinutes(2));

    @Container
    @SuppressWarnings("resource")
    static GenericContainer<?> redis = new GenericContainer<>(
        DockerImageName.parse("redis:7.2-alpine"))
        .withExposedPorts(6379);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.cassandra.contact-points", cassandra::getHost);
        registry.add("spring.cassandra.port", () -> cassandra.getMappedPort(9042));
        registry.add("spring.cassandra.local-datacenter", cassandra::getLocalDatacenter);
        registry.add("spring.cassandra.keyspace-name", () -> "url_shortener");
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @BeforeEach
    void setUp() {
        client = RestTestClient
            .bindToServer()
            .baseUrl("http://localhost:" + port)
            .build();
    }

    @Test
    void shorten_WithValidUrl_ReturnsShortUrl() {
        client.post().uri("/api/v1/shorten")
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ShortenRequest("https://www.google.com"))
        .exchange()
        .expectStatus().isCreated()
        .expectBody(ShortenResponse.class)
        .consumeWith(result -> {
            ShortenResponse response = result.getResponseBody();
            assertThat(response).isNotNull();
            assertThat(response.shortUrl()).contains("http://localhost:");
        });
    }

    @Test
    void shorten_WithInvalidUrl_ReturnsBadRequest() {
        client.post().uri("/api/v1/shorten")
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ShortenRequest("isso-nao-e-uma-url"))
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void shorten_WithBlankUrl_ReturnsBadRequest() {
        client.post().uri("/api/v1/shorten")
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ShortenRequest(""))
            .exchange()
            .expectStatus().isBadRequest();
    }


    @Test
    void redirect_WithExistingShortCode_ReturnsFound() {
        ShortenResponse response = client.post().uri("/api/v1/shorten")
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ShortenRequest("https://www.google.com"))
        .exchange()
        .expectStatus().isCreated()
        .expectBody(ShortenResponse.class)
        .returnResult()
        .getResponseBody();

        String shortCode = response.shortUrl().substring(response.shortUrl().lastIndexOf("/") + 1);

        client.get().uri("/{shortCode}", shortCode)
            .exchange()
            .expectStatus().isFound()
            .expectHeader().valueEquals("Location", "https://www.google.com");
    }

    @Test
    void redirect_WithNonExistingShortCode_ReturnsNotFound() {
        client.get().uri("/{shortCode}", "naoexiste")
            .exchange()
            .expectStatus().isNotFound();
    }
}
