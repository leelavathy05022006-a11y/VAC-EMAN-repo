package com.emergencymed.request.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ReferenceCodeGenerator {

    private final JdbcTemplate jdbcTemplate;
    private final AtomicInteger sequence = new AtomicInteger(0);
    private volatile String lastDateStr = "";

    public ReferenceCodeGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public synchronized String nextReferenceCode() {
        String todayStr = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE); // YYYYMMDD
        if (!todayStr.equals(lastDateStr)) {
            lastDateStr = todayStr;
            int maxSeq = 0;
            try {
                String prefix = "EMR-" + todayStr + "-%";
                String sql = "SELECT reference_code FROM emergency_requests WHERE reference_code LIKE ? ORDER BY reference_code DESC LIMIT 1";
                String highest = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("reference_code"), prefix)
                        .stream().findFirst().orElse(null);
                if (highest != null && highest.length() >= 17) {
                    String seqPart = highest.substring(13);
                    maxSeq = Integer.parseInt(seqPart);
                }
            } catch (Exception ignored) {
            }
            sequence.set(maxSeq);
        }

        int nextVal = sequence.incrementAndGet();
        return String.format("EMR-%s-%04d", todayStr, nextVal);
    }
}
