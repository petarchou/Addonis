package com.final_project.addonis.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;


@Table(name = "password_reset_tokens")
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    @Column(name  = "token")
    private String token;

    @OneToOne
    @JoinColumn(name="user_id")
    private User user;


    @Column(name="expiration_date")
    private LocalDateTime expirationDate;

    public boolean isExpired() {
        return expirationDate.isBefore(LocalDateTime.now());
    }
}
