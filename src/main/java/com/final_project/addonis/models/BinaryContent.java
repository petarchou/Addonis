package com.final_project.addonis.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "binary_contents")
@Getter
@Setter
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

    public BinaryContent() {
    }

    public BinaryContent(String name, byte[] data) {
        this.name = name;
        this.data = data;
    }
}
