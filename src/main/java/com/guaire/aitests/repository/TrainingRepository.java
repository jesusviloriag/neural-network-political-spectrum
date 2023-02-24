package com.guaire.aitests.repository;

import com.guaire.aitests.domain.Training;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Training entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {
    Training getByStatus(String status);
}
