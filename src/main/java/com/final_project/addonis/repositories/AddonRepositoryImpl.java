package com.final_project.addonis.repositories;

import com.final_project.addonis.models.Addon;
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
    public List<Addon> findAllAddonsByFilteringAndSorting(String keyword,
                                                          Optional<String> filterBy,
                                                          String sortOrDefault,
                                                          boolean descOrder,
                                                          int page,
                                                          int size) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Addon> criteriaQuery = criteriaBuilder.createQuery(Addon.class);

        Root<Addon> addon = criteriaQuery.from(Addon.class);
        Join<Addon, TargetIde> targetIdeJoin = addon.join("targetIde", JoinType.LEFT);
        List<Predicate> predicates = new ArrayList<>();
        String param = '%' + keyword + '%';

        if (filterBy.isPresent()) {
            if (filterBy.get().equalsIgnoreCase("name")) {
                predicates.add(criteriaBuilder.like(addon.get(filterBy.get()), param));
            } else {
                Predicate predicate = criteriaBuilder.like(targetIdeJoin.get("targetIdeName"), param);
                predicates.add(predicate);
            }
        } else {
            Predicate predicate = criteriaBuilder.like(addon.get("name"), param);
            predicate = criteriaBuilder.or(predicate, criteriaBuilder.like(targetIdeJoin.get("targetIdeName"), param));
            predicates.add(predicate);
        }

        predicates.add(criteriaBuilder.like(addon.get("state").get("name"), "approved"));

        if (descOrder) {
            criteriaQuery.select(addon).where(predicates.toArray(new Predicate[0]))
                    .orderBy(criteriaBuilder.desc(addon.get(sortOrDefault)));
        } else {
            criteriaQuery.select(addon).where(predicates.toArray(new Predicate[0]))
                    .orderBy(criteriaBuilder.asc(addon.get(sortOrDefault)));
        }

        TypedQuery<Addon> query = entityManager.createQuery(criteriaQuery)
                .setFirstResult(page * size)
                .setMaxResults(size);

        return query.getResultList();
    }
}
