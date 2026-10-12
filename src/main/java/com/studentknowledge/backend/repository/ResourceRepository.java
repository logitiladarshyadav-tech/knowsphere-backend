
package com.studentknowledge.backend.repository;

import com.studentknowledge.backend.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    List<Resource> findByTitleContainingIgnoreCaseOrSubjectContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
        String title,
        String subject,
        String description
    );
}
