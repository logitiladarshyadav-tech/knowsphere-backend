
package com.studentknowledge.backend.repository;

import com.studentknowledge.backend.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query(value = """
        SELECT * FROM documents
        WHERE extracted_text LIKE CONCAT('%', :keyword, '%')
        """, nativeQuery = true)
    List<Document> searchDocumentText(
            @Param("keyword") String keyword
    );
}
