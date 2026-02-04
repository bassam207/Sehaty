package com.Sehaty.Sehaty.repository;


import com.Sehaty.Sehaty.model.MedicalFile;
import com.Sehaty.Sehaty.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for managing MedicalFile entities.
 *
 * Provides methods for CRUD operations and custom queries related to user files.
 */

@Repository
public interface MedicalFileRepository extends JpaRepository<MedicalFile, UUID> {

    /**
     * Returns all medical files uploaded by a specific user.
     *
     * @param owner The user who owns the files.
     * @return List of medical files.
     */

    List<MedicalFile> findByOwner(User owner);

    /**
     * Returns files filtered by category (e.g. Radiology, Lab Test).
     *
     * @param category The file category.
     * @return List of matching files.
     */

    List<MedicalFile> findByCategory(String category);

    /**
     * Returns all medical files for a user by their ID.
     *
     * @param userId The ID of the user.
     * @return List of medical files.
     */
     List<MedicalFile> findByOwnerId(UUID userId) ;

     /**
      * Counts the number of files owned by a user.
      *
      * @param user The user.
      * @return The number of files.
      */
     long countByOwner(User user);

     /**
      * Returns all medical files for a user by their email.
      *
      * @param email The email of the user.
      * @return List of medical files.
      */
    List<MedicalFile> findByOwnerEmail(String email);
}
