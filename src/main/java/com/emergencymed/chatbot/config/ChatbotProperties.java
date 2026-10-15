package com.emergencymed.chatbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "chatbot")
public class ChatbotProperties {

    private final Llm llm = new Llm();
    private final RateLimit rateLimit = new RateLimit();
    private String defaultCity = "Chennai";
    private int staleHours = 24;
    private String cleanupCron = "0 0 3 * * ?";

    public Llm getLlm() {
        return llm;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public String getDefaultCity() {
        return defaultCity;
    }

    public void setDefaultCity(String defaultCity) {
        this.defaultCity = defaultCity;
    }

    public int getStaleHours() {
        return staleHours;
    }

    public void setStaleHours(int staleHours) {
        this.staleHours = staleHours;
    }

    public String getCleanupCron() {
        return cleanupCron;
    }

    public void setCleanupCron(String cleanupCron) {
        this.cleanupCron = cleanupCron;
    }

    public static class Llm {
        private boolean enabled = false;
        private String baseUrl = "";
        private String model = "gpt-3.5-turbo";
        private int timeoutMs = 5000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public int getTimeoutMs() {
            return timeoutMs;
        }

        public void setTimeoutMs(int timeoutMs) {
            this.timeoutMs = timeoutMs;
        }
    }

    public static class RateLimit {
        private int perMinute = 20;

        public int getPerMinute() {
            return perMinute;
        }

        public void setPerMinute(int perMinute) {
            this.perMinute = perMinute;
        }
    }
}
