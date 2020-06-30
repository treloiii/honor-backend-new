package com.trelloiii.honor.repository;

import com.trelloiii.honor.model.Veterans;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VeteransRepository extends JpaRepository<Veterans,Long> {
}
