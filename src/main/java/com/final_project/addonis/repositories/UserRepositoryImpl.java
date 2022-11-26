package com.final_project.addonis.repositories;

import com.final_project.addonis.models.User;
import com.final_project.addonis.repositories.contracts.UserRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class UserRepositoryImpl implements UserRepositoryCustom {
    private final EntityManager entityManager;

    @Autowired
    public UserRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<User> findAllUsersByFilteringAndSorting(String keyword,
                                                        Map<String, String> arguments) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
        Root<User> root = criteriaQuery.from(User.class);
        List<Predicate> predicates = new ArrayList<>();
        String sortBy = "id";


        if (arguments.containsKey("filter")) {
            predicates.add(criteriaBuilder.like(root.get(arguments.get("filter")), '%' + keyword + '%'));
        } else {
            Predicate predicate = criteriaBuilder.like(root.get("username"), '%' + keyword + '%');
            predicate = criteriaBuilder.or(predicate, criteriaBuilder.like(root.get("email"), '%' + keyword + '%'));
            predicate = criteriaBuilder.or(predicate, criteriaBuilder.like(root.get("phoneNumber"), '%' + keyword + '%'));
            predicates.add(predicate);
        }

        if (arguments.containsKey("sortBy")) {
            sortBy = arguments.get("sortBy");
        }

        predicates.add(criteriaBuilder.isFalse(root.get("isDeleted")));
        predicates.add(criteriaBuilder.isTrue(root.get("isVerified")));

        if (arguments.containsKey("orderBy") && arguments.get("orderBy").equalsIgnoreCase("desc")) {
            criteriaQuery.select(root).where(predicates.toArray(new Predicate[0]))
                    .orderBy(criteriaBuilder.desc(root.get(sortBy)));
        } else {
            criteriaQuery.select(root).where(predicates.toArray(new Predicate[0]))
                    .orderBy(criteriaBuilder.asc(root.get(sortBy)));
        }

        return entityManager.createQuery(criteriaQuery).getResultList();
    }
}
