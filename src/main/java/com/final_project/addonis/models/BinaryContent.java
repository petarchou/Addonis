package com.final_project.addonis.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "binary_contents")
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class BinaryContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "binary_content_id")
    private int id;

    @Column(name = "name")
    private String name;

    @Lob
    @Column(name = "data")
    private byte[] data;

    public BinaryContent(String name, byte[] data) {
        this.name = name;
        this.data = data;
    }
}
