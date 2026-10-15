package com.emergencymed.search.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "maps")
public class MapsProperties {

    private String apiKey = "";
    private Directions directions = new Directions();

    public static class Directions {
        private String baseUrl = "https://www.google.com/maps/dir/?api=1";

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Directions getDirections() {
        return directions;
    }

    public void setDirections(Directions directions) {
        this.directions = directions;
    }
}
