package com.Sehaty.Sehaty.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Token {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    private boolean revoked;
    private boolean expired;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}