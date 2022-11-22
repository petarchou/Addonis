package com.final_project.addonis.repositories.contracts;

import com.final_project.addonis.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByPhoneNumber(String phone);

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phone);

    @Query("select u from User u where u.isDeleted=false ")
    List<User> getAllByDeletedFalse();
}
