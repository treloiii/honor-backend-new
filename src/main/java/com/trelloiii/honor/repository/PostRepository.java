package com.trelloiii.honor.repository;

import com.trelloiii.honor.model.Post;
import com.trelloiii.honor.model.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface PostRepository extends JpaRepository<Post,Long> {
    Post getDistinctFirstByType(PostType type);
    Page<Post> findAllByType(Pageable page,PostType type);
}
