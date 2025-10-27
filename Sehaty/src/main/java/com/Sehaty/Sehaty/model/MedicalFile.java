package com.Sehaty.Sehaty.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a medical file uploaded by a user (typically a patient).
 *
 * Each MedicalFile belongs to one User (the owner).
 * Files can later be shared with others through the ShareRecord entity.
 */
@Entity
@Table(name = "medical_files")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedicalFile {

    /**
     * Unique identifier for the medical file.
     * UUID provides global uniqueness across the system.
     */
    @Id
    @GeneratedValue
    private UUID id;

    /** Name of the file (as stored or uploaded by the user). */
    private String fileName;

    /**
     * Type of the file — e.g., IMAGE, PDF, VIDEO.
     * This can be used to determine how to render or process the file.
     */
    private String fileType;

    /**
     * General medical category, e.g. "Radiology", "Lab Test", "Prescription".
     */
    @Enumerated(EnumType.STRING)
    private  FileCategory category;

    /**
     * Specific subcategory, e.g. "CT", "MRI", "Blood", "Urine".
     */
    private String subCategory;

    /**
     * URL or storage path pointing to the file's location in the cloud or local system.
     */
    private String url;

    /**
     * Timestamp indicating when the file was uploaded.
     */
    private LocalDateTime uploadedAt = LocalDateTime.now();

    /**
     * The owner (User) who uploaded the file.
     *
     * Relationship: Many files → One user.
     *
     * - @ManyToOne → Defines the relationship.
     * - @JoinColumn(name = "user_id") → Creates a foreign key column in the database.
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;


    private String StorageProvider  = " CLOUDINARY";

    public enum FileCategory {
        RADIOLOGY("أشعة", Map.of(
                "X-RAY", "أشعة عادية",
                "CT", "أشعة مقطعية",
                "MRI", "رنين مغناطيسي",
                "ULTRASOUND", "سونار"
        )),
        LABS("تحاليل", Map.of(
                "CBC", "صورة دم كاملة",
                "LFT", "وظائف كبد",
                "RFT", "وظائف كلى",
                "BLOOD_SUGAR", "سكر بالدم"
        )),
        REPORTS("تقارير و روشتات", Map.of(
                "MEDICAL_REPORT", "تقرير طبي",
                "PRESCRIPTION", "روشتة",
                "FOLLOW_UP", "متابعة"
        ));

        private final String arabicName;
        private final Map<String, String> subcategories;

        FileCategory(String arabicName, Map<String, String> subcategories) {
            this.arabicName = arabicName;
            this.subcategories = subcategories;
        }

        public String getArabicName() { return arabicName; }
        public Map<String, String> getSubcategories() { return subcategories; }
    }



    public MedicalFile(String fileName, FileCategory category , String subCategory)
    {
        this.fileName = fileName;
        this.category = category;
        this.subCategory = subCategory;
    }

    public MedicalFile( FileCategory category , String subCategory)
    {

        this.category = category;
        this.subCategory = subCategory;
    }


}
