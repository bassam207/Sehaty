package com.Sehaty.Sehaty.service;

import com.Sehaty.Sehaty.repository.SharedRecordRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SharedRecordCleanupService {

    private final SharedRecordRepository sharedRecordRepository;

    @Transactional
    @Scheduled(fixedRate = 60 * 60 * 1000) // كل ساعة
    public void deleteOldEndedSessions() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        int deletedCount = sharedRecordRepository.deleteOldEndedSessions(cutoffTime);
        System.out.println("🧹 تم حذف " + deletedCount + " جلسة منتهية وعدّى عليها أكثر من 24 ساعة");
    }
}
