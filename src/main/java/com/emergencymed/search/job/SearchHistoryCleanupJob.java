package com.emergencymed.search.job;

import com.emergencymed.search.repository.SearchHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class SearchHistoryCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(SearchHistoryCleanupJob.class);

    private final SearchHistoryRepository repository;

    @Autowired
    public SearchHistoryCleanupJob(SearchHistoryRepository repository) {
        this.repository = repository;
    }

    /**
     * Nightly scheduled cleanup job running at 02:00 AM.
     * Deletes search history rows older than 30 days per specification.
     */
    @Scheduled(cron = "${search.cleanup.cron:0 0 2 * * ?}")
    @Transactional
    public long cleanOldSearchHistory() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        long deletedCount = repository.deleteByCreatedAtBefore(threshold);
        log.info("[CLEANUP JOB] Deleted {} search history records older than 30 days (before {})",
                deletedCount, threshold);
        return deletedCount;
    }
}
