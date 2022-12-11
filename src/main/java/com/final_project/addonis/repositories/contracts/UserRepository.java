package com.final_project.addonis.repositories.contracts;

import com.final_project.addonis.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>, UserRepositoryCustom {

    Optional<User> findByIdAndIsDeletedFalse(int id);
    Optional<User> findByUsernameAndIsDeletedFalse(String username);

    Optional<User> findByEmailAndIsDeletedFalse(String email);

    Optional<User> findByPhoneNumberAndIsDeletedFalse(String phone);


    boolean existsUserByUsernameAndIsDeletedFalse(String username);
    boolean existsUserByPhoneNumberAndIsDeletedFalse(String phone);
    boolean existsUserByEmailAndIsDeletedFalse(String email);

}
