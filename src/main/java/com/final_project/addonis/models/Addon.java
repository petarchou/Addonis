package com.final_project.addonis.models;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
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
    @CreationTimestamp
    private LocalDateTime uploadedDate;

    @Column(name = "downloads")
    private int downloads;

    @ManyToOne
    @JoinColumn(name = "state_id")
    private State state;

    @Column(name = "open_issues_count")
    private int issuesCount;

    @Column(name = "pull_requests_count")
    private int pullRequests;

    @Column(name = "last_commit_date")
    private LocalDateTime lastCommitDate;

    @Column(name = "last_commit_message")
    private String lastCommitMessage;


    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "addons_tags",
            joinColumns = @JoinColumn(name = "addon_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags;

    @ManyToMany
    @JoinTable(name = "addons_ratings",
            joinColumns = @JoinColumn(name = "addon_id"),
            inverseJoinColumns = @JoinColumn(name = "rating_id"))
    @MapKeyJoinColumn(name = "user_id")
    private Map<User, Rating> rating;

    @ManyToMany
    @JoinTable(name = "addons_categories",
            joinColumns = @JoinColumn(name = "addon_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories;

    public Addon() {
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
