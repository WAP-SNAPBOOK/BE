package com.example.easybooking.availability;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=true",
        "spring.flyway.locations=classpath:db/migration-test"
})
class AvailabilityMigrationTablesTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void createsAvailabilityRelatedTables() {
        List<String> tableNames = jdbcTemplate.queryForList(
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'",
                String.class
        );

        assertThat(tableNames)
                .contains(
                        "SHOP_SETTINGS",
                        "SHOP_OPERATING_TIMES",
                        "STAFF_OPERATING_TIMES",
                        "SHOP_HOLIDAYS",
                        "PUBLIC_HOLIDAYS"
                );
    }
}
