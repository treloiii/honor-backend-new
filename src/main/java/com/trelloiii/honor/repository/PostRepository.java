package com.trelloiii.honor.repository;

import com.trelloiii.honor.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface PostRepository extends JpaRepository<Post,Long> {
}
