package com.Sehaty.Sehaty.service;

import com.Sehaty.Sehaty.repository.SharedRecordRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service for cleaning up old shared records.
 * Runs periodically to delete expired or revoked sessions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SharedRecordCleanupService {

    private final SharedRecordRepository sharedRecordRepository;

    /**
     * Deletes shared sessions that have been expired or revoked for more than 24 hours.
     * Runs every hour.
     */
    @Transactional
    @Scheduled(fixedRate = 60 * 60 * 1000) // Every hour
    public void deleteOldEndedSessions() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        int deletedCount = sharedRecordRepository.deleteOldEndedSessions(cutoffTime);
        if (deletedCount > 0) {
            log.info("🧹 Deleted {} expired sessions older than 24 hours", deletedCount);
        }
    }
}
