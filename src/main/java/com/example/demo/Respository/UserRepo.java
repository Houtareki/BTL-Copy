package com.example.demo.Respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.Entity.User;
import com.example.demo.Model.CommonModel.Role;
import jakarta.transaction.Transactional;

import java.util.List;

@Repository
public interface UserRepo extends JpaRepository<User,Integer>{
    User findByUsername(String username);
    User findByEmail(String email);
    User findByUserId(int userId);

    List<User> findByRole(Role role);

    @Transactional
    @Modifying
    @Query(value = "update users set state = 'ACTIVE' where user_id = :userId", nativeQuery = true)
    void activateUser(@Param(value = "userId") int userId);

    @Transactional
    @Modifying
    @Query(value = "update users set password = :newPassword where user_id = :userId", nativeQuery = true)
    void changeUserPassword(@Param(value = "userId") int userId, @Param(value = "newPassword") String newPassword);
}
