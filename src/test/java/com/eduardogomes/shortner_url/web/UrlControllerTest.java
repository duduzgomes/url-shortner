package com.eduardogomes.shortner_url.web;

import static org.mockito.Mockito.when;
import static com.eduardogomes.shortner_url.common.UrlConstants.VALID_LONG_URL;
import static com.eduardogomes.shortner_url.common.UrlConstants.VALID_SHORT_CODE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.eduardogomes.shortner_url.controllers.UrlController;
import com.eduardogomes.shortner_url.models.ShortenRequest;
import com.eduardogomes.shortner_url.models.exceptions.UrlNotFoundException;
import com.eduardogomes.shortner_url.service.UrlService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UrlController.class)
public class UrlControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UrlService urlService;

    @Test
    void shorten_WithValidUrl_ReturnsShortUrl() throws Exception {
        when(urlService.shorten(VALID_LONG_URL)).thenReturn(VALID_SHORT_CODE);

        mockMvc.perform(post("/api/v1/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new ShortenRequest(VALID_LONG_URL)
                )))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.shortUrl").value(
                "http://localhost:8080/" + VALID_SHORT_CODE
            ));
    }

    @Test
    void shorten_WithBlankUrl_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new ShortenRequest("")
                )))
            .andExpect(status().isBadRequest());
        
        mockMvc.perform(post("/api/v1/shorten")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new ShortenRequest("isso-nao-e-uma-url")
            )))
        .andExpect(status().isBadRequest());
    }

    @Test
    void redirect_WithExistingShortCode_ReturnsLocationHeader() throws Exception {
        when(urlService.resolve(VALID_SHORT_CODE)).thenReturn(VALID_LONG_URL);

        mockMvc.perform(get("/{shortCode}", VALID_SHORT_CODE))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", VALID_LONG_URL));
    }

    @Test
    void redirect_WithUnexistingShortCode_ReturnsNotFound() throws Exception {
        when(urlService.resolve(VALID_SHORT_CODE))
            .thenThrow(new UrlNotFoundException(VALID_SHORT_CODE));

        mockMvc.perform(get("/{shortCode}", VALID_SHORT_CODE))
            .andExpect(status().isNotFound());
    }
}
