package com.final_project.addonis.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "addons")
@Getter
@Setter
public class Addon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "addon_id")
    private int id;

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "target_ide_id")
    private TargetIde targetIde;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

    @Column(name = "description")
    private String description;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "binary_content_id")
    private BinaryContent data;

    @Column(name = "origin_url")
    private String originUrl;

    @Column(name = "upload_date")
    private LocalDateTime uploadedDate;

    @Column(name = "downloads")
    private int downloads;

    @ManyToOne
    @JoinColumn(name = "state_id")
    private State state;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "addons_tags",
            joinColumns = @JoinColumn(name = "addon_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags;

    public Addon() {
    }

    public void addTags(Tag... tagsToAdd) {
        tags.addAll(Arrays.asList(tagsToAdd));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Addon addon = (Addon) o;
        return id == addon.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}