
package com.SportGrid.web_backend;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/turfs")
public class TurfController {

    private final JdbcTemplate jdbcTemplate;

    public TurfController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public List<Map<String, Object>> getTurfs() {
        String sql = """
            SELECT turf_id, name, location, sport_type,
                   price_per_hour
            FROM turfs
            WHERE is_approved = 1
            """;

        return jdbcTemplate.queryForList(sql);
    }
}
