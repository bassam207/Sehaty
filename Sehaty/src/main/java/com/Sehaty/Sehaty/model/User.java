package com.Sehaty.Sehaty.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a user in the Sehaty application.
 * A user can be a patient or a doctor (role management can be added later).
 *
 * Each user can:
 * - Upload multiple medical files.
 * - Share those files with others (via SharedRecords).
 */
@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    /**
     * Unique identifier for the user.
     * UUID is used instead of Long for better security and scalability.
     */
    @Id
    @GeneratedValue
    private UUID id;

    /** Full name of the user (patient or doctor). */
    @NotBlank(message = "الاسم مطلوب ")
    private String name;

    /** User's email address, used for login and communication. */
    @NotBlank(message = "الايميل مطلوب ")
    @Email(message = "يرجي ادخال صيغة ايميل صالحة")
    private String email;

    /** Encrypted password for authentication. */
    @NotBlank(message = "كلمة السر مطلوبة ")
    @Size(min = 8,message = "كلمة السر لازم تكون 8 احرف علي الاقل")
    private String password;


    /** Image of the user profile*/
    private String profileImageUrl;

    /**
     * List of medical files uploaded by the user.
     *
     * Relationship: One user → Many medical files.
     *
     * - mappedBy = "owner" → The "owner" field inside MedicalFile is the owning side.
     * - cascade = CascadeType.ALL → Any operation (save, delete, update) on User
     *   will automatically apply to their files.
     */
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private List<MedicalFile> files = new ArrayList<>();

    /**
     * List of share records created by the user.
     *
     * Relationship: One user → Many shared records.
     *
     * - mappedBy = "user" → The "user" field inside SharedRecords is the owning side.
     * - cascade = CascadeType.ALL → Ensures that when a user is removed,
     *   their related share records are also deleted.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<SharedRecords> shares = new ArrayList<>();
}
