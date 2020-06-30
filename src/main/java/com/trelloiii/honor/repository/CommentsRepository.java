package com.trelloiii.honor.repository;

import com.trelloiii.honor.model.Comments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface CommentsRepository extends JpaRepository<Comments,Long> {
    @Query("update Comments c set c.active=:active where c.id=:id")
    @Modifying
    @Transactional
    void setActive(boolean active,Long id);
}
