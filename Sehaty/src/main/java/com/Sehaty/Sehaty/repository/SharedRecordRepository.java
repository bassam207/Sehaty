package com.Sehaty.Sehaty.repository;

import com.Sehaty.Sehaty.model.SharedRecords;
import com.Sehaty.Sehaty.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing SharedRecord entities.
 *
 * Used to track file-sharing activities (QR-based sharing).
 */
@Repository
public interface SharedRecordRepository extends JpaRepository<SharedRecords, UUID> {

    /**
     * Returns all records shared by a specific user.
     *
     * @param user The user who shared files.
     * @return List of shared records.
     */
    List<SharedRecords> findByUser(User user);


    Optional<SharedRecords> findByQrCode(String qr);
}
