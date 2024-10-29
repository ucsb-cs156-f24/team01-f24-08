package edu.ucsb.cs156.example.controllers;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.Valid;

@Tag(name = "Articles")
@RequestMapping("/api/articles")
@RestController
@Slf4j
public class ArticlesController extends ApiController {
    @Autowired
    ArticlesRepository articlesRepository;

    @Operation(summary = "List all articles")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_USER')")
    public Iterable<Articles> allArticles() {
        Iterable<Articles> article = articlesRepository.findAll();
        return article;
    }

    @Operation(summary = "Get a single article")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Articles getById(
            @Parameter(name = "id") @RequestParam Long id) {
        Articles article = articlesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Articles.class, id));
        return article;
    }

    @Operation(summary = "Create a new article")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Articles postArticles(
            @Parameter(name = "title") @RequestParam String title,
            @Parameter(name = "url") @RequestParam String url,
            @Parameter(name = "explanation") @RequestParam String explanation,
            @Parameter(name = "email") @RequestParam String email,
            @Parameter(name = "dateAdded", description = "date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)") @RequestParam("dateAdded") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateAdded)
            throws JsonProcessingException {

        // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        // See: https://www.baeldung.com/spring-date-parameters
        log.info("dateAdded={}", dateAdded);

        Articles article = new Articles();
        article.setTitle(title); // private String title;
        article.setUrl(url); // private String url;
        article.setExplanation(explanation); // private String explanation;
        article.setEmail(email); // private String email;
        article.setDateAdded(dateAdded); // private LocalDateTime dateAdded;

        Articles savedArticle = articlesRepository.save(article);
        return savedArticle;
    }

    /**
     * Delete an Article
     * 
     * @param id the id of the article to delete
     * @return a message indicating the article was deleted
     */
    @Operation(summary = "Delete an Article")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteArticle(
            @Parameter(name = "id") @RequestParam Long id) {
        Articles article = articlesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Articles.class, id));

        articlesRepository.delete(article);
        return genericMessage("Article with id %s deleted".formatted(id));
    }

    /**
     * Update a single date
     * 
     * @param id       id of the date to update
     * @param incoming the new date
     * @return the updated date object
     */
    @Operation(summary = "Update a single article")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Articles updateaArticles(
            @Parameter(name = "id") @RequestParam Long id,
            @RequestBody @Valid Articles incoming) {

        Articles article = articlesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Articles.class, id));

        article.setTitle(incoming.getTitle()); // private String title;
        article.setUrl(incoming.getUrl()); // private String url;
        article.setExplanation(incoming.getExplanation()); // private String explanation;
        article.setEmail(incoming.getEmail()); // private String email;
        article.setDateAdded(incoming.getDateAdded()); // private LocalDateTime dateAdded;

        articlesRepository.save(article);

        return article;
    }
}
