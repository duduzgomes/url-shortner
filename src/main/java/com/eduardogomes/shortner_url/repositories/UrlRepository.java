package com.eduardogomes.shortner_url.repositories;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.data.cassandra.core.query.Criteria;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.stereotype.Repository;

import com.eduardogomes.shortner_url.models.Url;

@Repository
public class UrlRepository {

    private static final Duration TTL = Duration.ofDays(365L * 10);

    private final CassandraTemplate cassandraTemplate;

    public UrlRepository(CassandraTemplate cassandraTemplate) {
        this.cassandraTemplate = cassandraTemplate;
    }

    public void save(Url url) {
        cassandraTemplate.insert(url,
            InsertOptions.builder()
                .ttl(TTL)
                .build());
    }

    public Optional<Url> findById(String shortCode) {
        Query query = Query.query(
            Criteria.where("shortcode").is(shortCode)
        );
        return Optional.ofNullable(
            cassandraTemplate.selectOne(query, Url.class)
        );
    }
}
