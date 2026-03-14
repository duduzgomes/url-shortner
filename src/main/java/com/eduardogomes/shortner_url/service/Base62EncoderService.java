package com.eduardogomes.shortner_url.service;

import org.hashids.Hashids;
import org.springframework.stereotype.Service;

@Service
public class Base62EncoderService {
    private static final String ALPHABET =
    "abcdefghijklmnopqrstuvwxyz" +
    "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
    "0123456789";

    private final Hashids hashids;

    public Base62EncoderService() {
        this.hashids = new Hashids("dev-secret-key", 4, ALPHABET);
    }

    public String encode(long id) {
        return hashids.encode(id);
    }

    public long decode(String hash) {
        long[] decoded = hashids.decode(hash);
        return decoded.length > 0 ? decoded[0] : -1;
    }
}
