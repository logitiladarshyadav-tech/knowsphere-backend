
package com.studentknowledge.backend.document;

import com.studentknowledge.backend.entity.Document;
import com.studentknowledge.backend.repository.DocumentRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:5173")
public class DocumentController {

    private final DocumentRepository documentRepository;

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(".pdf", ".docx", ".txt");

    public DocumentController(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @PostMapping(
        value = "/upload",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Map<String, String> uploadDocument(
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        if (file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please select a file."
            );
        }

        String filename = file.getOriginalFilename();

        if (filename == null || filename.contains("..")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid filename."
            );
        }

        String lowerName = filename.toLowerCase();

        boolean allowed = ALLOWED_EXTENSIONS.stream()
                .anyMatch(lowerName::endsWith);

        if (!allowed) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only PDF, DOCX, and TXT files are supported."
            );
        }

        String extractedText;
        String fileType;

        if (lowerName.endsWith(".txt")) {
            extractedText = new String(
                    file.getBytes(),
                    java.nio.charset.StandardCharsets.UTF_8
            );
            fileType = "TXT";

        } else if (lowerName.endsWith(".pdf")) {
            try (PDDocument pdf = Loader.loadPDF(file.getBytes())) {
                extractedText = new PDFTextStripper().getText(pdf);
            }
            fileType = "PDF";

        } else {
            try (XWPFDocument word =
                         new XWPFDocument(file.getInputStream())) {
                extractedText = word.getParagraphs().stream()
                        .map(p -> p.getText())
                        .collect(java.util.stream.Collectors.joining("\n"));
            }
            fileType = "DOCX";
        }

        if (extractedText.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No readable text was found in the document."
            );
        }

        Document document = new Document(
                filename,
                fileType,
                extractedText
        );

        Document savedDocument = documentRepository.save(document);

        return Map.of(
                "message", "Document uploaded and saved successfully.",
                "filename", savedDocument.getFilename(),
                "id", String.valueOf(savedDocument.getId()),
                "fileType", savedDocument.getFileType(),
                "extractedCharacters",
                String.valueOf(extractedText.length())
        );
    }
    
@GetMapping("/search")
public List<Map<String, Object>> searchDocuments(
        @RequestParam("q") String query
) {
    if (query == null || query.isBlank()) {
        return List.of();
    }

    List<Document> documents =
            documentRepository.searchDocumentText(query.trim());

    List<Map<String, Object>> results = new ArrayList<>();

    for (Document document : documents) {
        Map<String, Object> result = new java.util.LinkedHashMap<>();

        result.put("id", document.getId());
        result.put("filename", document.getFilename());
        result.put("fileType", document.getFileType());

        String text = document.getExtractedText();
        int index = text.toLowerCase().indexOf(query.trim().toLowerCase());

        int start = Math.max(0, index - 100);
        int end = Math.min(text.length(), index + query.trim().length() + 200);

        result.put("excerpt", text.substring(start, end));
        results.add(result);
    }

    return results;
}

}
