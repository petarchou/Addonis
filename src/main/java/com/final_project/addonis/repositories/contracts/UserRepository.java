package com.final_project.addonis.repositories.contracts;

import com.final_project.addonis.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByPhoneNumber(String phone);

    @Query("select u from User u where u.isDeleted=false ")
    List<User> findAllByDeletedFalse();
}