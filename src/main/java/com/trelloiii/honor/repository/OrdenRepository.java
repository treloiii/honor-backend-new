package com.trelloiii.honor.repository;

import com.trelloiii.honor.model.Ordens;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdenRepository extends JpaRepository<Ordens,Long> {
    Page<Ordens> findAll(Pageable page);
}
