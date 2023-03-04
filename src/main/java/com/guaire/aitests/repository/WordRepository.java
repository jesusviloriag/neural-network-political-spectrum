package com.guaire.aitests.repository;

import com.guaire.aitests.domain.Word;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Word entity.
 */
@SuppressWarnings("unused")
@Repository
public interface WordRepository extends JpaRepository<Word, Long> {
    public List<Word> findByName_IgnoreCase(String name);

    Word getByValue(Double s);
}
