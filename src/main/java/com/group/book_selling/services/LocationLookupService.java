package com.group.book_selling.services;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LocationLookupService {

    private final RestClient restClient;

    public LocationLookupService(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("https://provinces.open-api.vn/api").build();
    }

    @Cacheable(value = "provinces", key = "#code")
    public String resolveProvinceName(String code) {
        return fetchName(code, "p");
    }

    @Cacheable(value = "districts", key = "#code")
    public String resolveDistrictName(String code) {
        return fetchName(code, "d");
    }

    @Cacheable(value = "wards", key = "#code")
    public String resolveWardName(String code) {
        return fetchName(code, "w");
    }

    private String fetchName(String code, String type) {
        if (code == null || code.isBlank() || !code.matches("\\d+")) return code;

        try {
            // Jackson will automatically map this to a Map or a Record
            Map<String, Object> response = restClient.get()
                    .uri("/{type}/{code}", type, code)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "API Error");
                    })
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});
            
            return response != null ? (String) response.get("name") : code;
        } catch (Exception e) {
            return code; // Fallback to code if API is down
        }
    }
}