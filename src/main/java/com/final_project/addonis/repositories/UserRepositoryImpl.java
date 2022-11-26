package com.final_project.addonis.repositories;

import com.final_project.addonis.models.User;
import com.final_project.addonis.repositories.contracts.UserRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class UserRepositoryImpl implements UserRepositoryCustom {
    private final EntityManager entityManager;

    @Autowired
    public UserRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<User> findAllUsersByFilteringAndSorting(String keyword,
                                                        Optional<String> filterBy,
                                                        String sortOrDefault,
                                                        boolean descOrder,
                                                        int page, int size) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
        Root<User> root = criteriaQuery.from(User.class);
        List<Predicate> predicates = new ArrayList<>();
        String param = '%' + keyword + '%';


        if (filterBy.isPresent()) {
            predicates.add(criteriaBuilder.like(root.get(filterBy.get()), param));
        } else {
            Predicate predicate = criteriaBuilder.like(root.get("username"), param);
            predicate = criteriaBuilder.or(predicate, criteriaBuilder.like(root.get("email"), param));
            predicate = criteriaBuilder.or(predicate, criteriaBuilder.like(root.get("phoneNumber"), param));
            predicates.add(predicate);
        }

        predicates.add(criteriaBuilder.isFalse(root.get("isDeleted")));
        predicates.add(criteriaBuilder.isTrue(root.get("isVerified")));

        if (descOrder) {
            criteriaQuery.select(root).where(predicates.toArray(new Predicate[0]))
                    .orderBy(criteriaBuilder.desc(root.get(sortOrDefault)));
        } else {
            criteriaQuery.select(root).where(predicates.toArray(new Predicate[0]))
                    .orderBy(criteriaBuilder.asc(root.get(sortOrDefault)));
        }

        TypedQuery<User> query = entityManager.createQuery(criteriaQuery)
                .setFirstResult(page * size)
                .setMaxResults(size);

        return query.getResultList();
    }
}
