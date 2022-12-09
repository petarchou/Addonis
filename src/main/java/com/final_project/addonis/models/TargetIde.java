package com.final_project.addonis.models;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "target_ides")
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class TargetIde {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "target_ide_id")
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "ide_logo")
    private String logo;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TargetIde targetIde = (TargetIde) o;
        return id == targetIde.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
