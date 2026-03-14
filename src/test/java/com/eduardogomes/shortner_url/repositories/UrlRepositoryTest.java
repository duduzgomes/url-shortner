package com.eduardogomes.shortner_url.repositories;

import com.eduardogomes.shortner_url.models.Url;

import static com.eduardogomes.shortner_url.common.UrlConstants.URL;
import static com.eduardogomes.shortner_url.common.UrlConstants.VALID_SHORT_CODE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.query.Criteria;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.cassandra.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
class UrlRepositoryTest {
    @SuppressWarnings("resource")
    @Container
    @ServiceConnection
    static CassandraContainer cassandra =
        new CassandraContainer(
            DockerImageName.parse("cassandra:5.0")
        )
        .withReuse(true)
        .withInitScript("schema.cql")
        .withStartupTimeout(Duration.ofMinutes(2));

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private CassandraTemplate cassandraTemplate;

    @DynamicPropertySource
    static void cassandraProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cassandra.contact-points", () -> cassandra.getHost());
        registry.add("spring.cassandra.port", () -> cassandra.getMappedPort(9042));
        registry.add("spring.cassandra.local-datacenter", () -> cassandra.getLocalDatacenter());
        registry.add("spring.cassandra.keyspace-name", () -> "url_shortener");
    }

    @BeforeEach
    void setUp() {
        Query query = Query.query(Criteria.where("shortcode").is(VALID_SHORT_CODE));
        cassandraTemplate.delete(query, Url.class);
    }

    @Test
    void save_WithValidUrl_ReturnsVoid() {
        assertThatCode(()-> urlRepository.save(URL)).doesNotThrowAnyException();
    }

    @Test
    void getUrl_WithExistingShortCode_ReturnsUrl() {
        cassandraTemplate.insert(URL);
        
        Optional<Url> sut = urlRepository.findById(VALID_SHORT_CODE);
        assertThat(sut).isPresent();
        assertThat(sut.get().getShortCode()).isEqualTo(VALID_SHORT_CODE);
    }

    @Test
    void getUrl_WithUneExistingShortCode_ReturnsVoid() {
        Optional<Url> found = urlRepository.findById("naoexiste");
        assertThat(found).isEmpty();
    }
}
