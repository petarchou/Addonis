package com.final_project.addonis.repositories.contracts;

import com.final_project.addonis.models.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>, UserRepositoryCustom {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phone);

    List<User> findAllByIsDeletedFalseAndIsVerifiedTrue(Pageable pageable);

}
