package com.trelloiii.honor.repository;

import com.trelloiii.honor.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    User findByUsername(String username);
}
