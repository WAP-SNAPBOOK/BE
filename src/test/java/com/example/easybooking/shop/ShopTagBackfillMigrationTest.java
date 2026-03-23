package com.example.easybooking.shop;

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
        "spring.flyway.locations=classpath:db/migration-test",
        "spring.jpa.hibernate.ddl-auto=none"
})
class ShopTagBackfillMigrationTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void backfillsShopTagsAndShopTagIds() {
        List<String> shopTenTags = jdbcTemplate.queryForList(
                "SELECT name FROM shop_tags WHERE shop_id = 10 ORDER BY sort_order",
                String.class
        );
        List<String> shopTwentyTags = jdbcTemplate.queryForList(
                "SELECT name FROM shop_tags WHERE shop_id = 20 ORDER BY sort_order",
                String.class
        );
        Integer nullShopTagIdCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM shop_menu_tags WHERE shop_tag_id IS NULL",
                Integer.class
        );

        assertThat(shopTenTags).containsExactly("손관리", "발관리");
        assertThat(shopTwentyTags).containsExactly("손관리");
        assertThat(nullShopTagIdCount).isZero();
    }
}
