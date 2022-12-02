package com.final_project.addonis.repositories;

import com.final_project.addonis.models.Addon;
import com.final_project.addonis.models.Category;
import com.final_project.addonis.models.TargetIde;
import com.final_project.addonis.repositories.contracts.CustomAddonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class AddonRepositoryImpl implements CustomAddonRepository {


    private final EntityManager entityManager;

    @Autowired
    public AddonRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Addon> findAllAddonsByFilteringAndSorting(Optional<String> keyword,
                                                          Optional<String> targetIde,
                                                          Optional<String> category,
                                                          boolean order,
                                                          int page,
                                                          int size) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Addon> criteriaQuery = criteriaBuilder.createQuery(Addon.class);

        Root<Addon> addon = criteriaQuery.from(Addon.class);
        Join<Addon, TargetIde> targetIdeJoin = addon.join("targetIde", JoinType.LEFT);
        Join<Addon, Category> categoryJoin = addon.join("categories", JoinType.LEFT);
        List<Predicate> predicates = new ArrayList<>();

        keyword.ifPresent(value -> predicates.add(criteriaBuilder
                .like(addon.get("name"), "%" + value + "%")));
        targetIde.ifPresent(value -> predicates.add(criteriaBuilder
                .like(targetIdeJoin.get("targetIdeName"), "%" + value + "%")));
        category.ifPresent(value -> predicates.add(criteriaBuilder
                .like(categoryJoin.get("name"), "%" + value + "%")));

        predicates.add(criteriaBuilder.like(addon.get("state").get("name"), "approved"));

        if (order) {
            criteriaQuery.select(addon).where(predicates.toArray(new Predicate[0]))
                    .orderBy(criteriaBuilder.asc(addon.get("name"))).distinct(true);
        } else {
            criteriaQuery.select(addon).where(predicates.toArray(new Predicate[0]))
                    .orderBy(criteriaBuilder.desc(addon.get("name"))).distinct(true);
        }

        TypedQuery<Addon> query = entityManager.createQuery(criteriaQuery)
                .setFirstResult(page * size)
                .setMaxResults(size);

        return query.getResultList();
    }
}
