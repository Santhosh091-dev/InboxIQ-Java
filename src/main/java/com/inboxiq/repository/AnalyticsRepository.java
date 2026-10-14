package com.inboxiq.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class AnalyticsRepository {

    @Autowired private JdbcTemplate jdbcTemplate;

    // Raw JDBC query, used alongside Hibernate/JPA
    public List<Map<String, Object>> decisionSummary(Long userId) {
        String sql = "SELECT ai_decision AS decision, COUNT(*) AS total " +
                     "FROM senders WHERE user_id = ? GROUP BY ai_decision";
        return jdbcTemplate.queryForList(sql, userId);
    }
}