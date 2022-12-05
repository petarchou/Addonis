package com.final_project.addonis.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "invited_users")
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class InvitedUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "email")
    private String email;

    @Column(name = "invitation_date")
    private LocalDateTime lastInviteDate;

}
