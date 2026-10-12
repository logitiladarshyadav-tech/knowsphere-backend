
package com.studentknowledge.backend.controller;

import com.studentknowledge.backend.entity.Resource;
import com.studentknowledge.backend.repository.ResourceRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Locale;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/resources")
@CrossOrigin(origins = "http://localhost:5173")
public class ResourceController {

    private final ResourceRepository repository;

    public ResourceController(ResourceRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Resource> getAllResources() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Resource getResourceById(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource not found"));
    }

    @PostMapping
    public Resource addResource(@RequestBody Resource resource) {
        return repository.save(resource);
    }

    @DeleteMapping("/{id}")
    public void deleteResource(@PathVariable Long id) {
        repository.deleteById(id);
    }

    
@GetMapping("/search")
public List<Resource> searchResources(@RequestParam String q) {
    String keyword = q.trim();

    if (keyword.isEmpty()) {
        return List.of();
    }

    return repository
        .findByTitleContainingIgnoreCaseOrSubjectContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            keyword,
            keyword,
            keyword
        );
}

@GetMapping("/retrieve")
public List<Resource> retrieveKnowledge(@RequestParam String q) {
    if (q == null || q.isBlank()) {
        return List.of();
    }

    Set<String> stopWords = Set.of(
        "what", "is", "are", "the", "a", "an",
        "in", "on", "of", "for", "to", "and",
        "explain", "describe", "tell", "me",
        "about", "how", "does", "do", "with"
    );

    List<String> keywords = Arrays.stream(
            q.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")
        )
        .filter(word -> word.length() > 1)
        .filter(word -> !stopWords.contains(word))
        .distinct()
        .collect(Collectors.toList());

    if (keywords.isEmpty()) {
        return List.of();
    }

    return repository.findAll().stream()
        .map(resource -> new java.util.AbstractMap.SimpleEntry<>(
            resource,
            keywords.stream()
                .filter(word -> (
                    resource.getTitle() + " " +
                    resource.getSubject() + " " +
                    resource.getDescription() + " " +
                    resource.getType()
                ).toLowerCase(Locale.ROOT).contains(word))
                .count()
        ))
        .filter(entry -> entry.getValue() > 0)
        .sorted(
            Comparator.comparingLong(
                (java.util.Map.Entry<Resource, Long> entry) ->
                    entry.getValue()
            ).reversed()
        )
        .map(java.util.Map.Entry::getKey)
        .collect(Collectors.toList());
}


}
