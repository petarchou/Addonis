package com.final_project.addonis.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="invited_users")
@Getter
@Setter
public class InvitedUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name="email")
    private String email;

    @Column(name ="invitation_date")
    private LocalDateTime lastInviteDate;

}
