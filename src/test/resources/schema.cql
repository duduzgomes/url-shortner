CREATE KEYSPACE IF NOT EXISTS url_shortener
    WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

CREATE TABLE IF NOT EXISTS url_shortener.urls (
    shortcode TEXT PRIMARY KEY,
    long_url TEXT,
    created_at TIMESTAMP
);