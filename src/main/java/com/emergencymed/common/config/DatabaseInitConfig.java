package com.emergencymed.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

@Configuration
public class DatabaseInitConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitConfig.class);

    @Bean
    @Order(10) // Run before DemoDataLoader (order 100)
    public CommandLineRunner databaseSchemaRunner(DataSource dataSource) {
        return args -> {
            log.info("Running database migration scripts: V100 and V110...");
            try {
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                populator.setContinueOnError(true);
                populator.addScript(new ClassPathResource("db/migration/V100__m1_users_profiles.sql"));
                populator.addScript(new ClassPathResource("db/migration/V110__m1_pharmacies_delivery_profiles.sql"));
                populator.execute(dataSource);
                log.info("Database migration scripts executed successfully.");
            } catch (Exception ex) {
                log.error("Error executing database migration scripts: {}", ex.getMessage());
            }
        };
    }
}
