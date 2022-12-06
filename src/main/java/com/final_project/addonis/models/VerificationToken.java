package com.final_project.addonis.models;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "verify_account_tokens")
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "token")
    private String token;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

}
