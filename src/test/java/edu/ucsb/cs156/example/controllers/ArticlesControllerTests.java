package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = ArticlesController.class)
@Import(TestConfig.class)
public class ArticlesControllerTests extends ControllerTestCase {

        @MockBean
        ArticlesRepository articlesRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/articles/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/articles/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/articles/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/articles?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/articles/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/articles/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/articles/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                Articles article = Articles.builder()
                                .title("Who is the best professor at UCSB?")
                                .url("https://www.reddit.com/r/UCSantaBarbara/comments/1e46yoe/who_is_the_best_professor_youve_had_who_is_the/")
                                .explanation("A Reddit thread discussing the best professors at UCSB.")
                                .email("noreply@reddit.com")
                                .dateAdded(LocalDateTime.parse("2022-01-03T00:00:00"))
                                .build();

                when(articlesRepository.findById(eq(7L))).thenReturn(Optional.of(article)); // Check not sure why id is
                                                                                            // 7

                // act
                MvcResult response = mockMvc.perform(get("/api/articles?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(articlesRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(article);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(articlesRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/articles?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(articlesRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Articles with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_articles() throws Exception {

                // arrange
                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                Articles article1 = Articles.builder()
                                .title("Who is the best professor at UCSB?")
                                .url("https://www.reddit.com/r/UCSantaBarbara/comments/1e46yoe/who_is_the_best_professor_youve_had_who_is_the/")
                                .explanation("A Reddit thread discussing the best professors at UCSB.")
                                .email("noreply@reddit.com")
                                .dateAdded(ldt1)
                                .build();

                LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

                Articles article2 = Articles.builder()
                                .title("UCSB Academic Calendar")
                                .url("https://registrar.sa.ucsb.edu/calendars/academic-calendar")
                                .explanation("The official UCSB academic calendar.")
                                .email("Registration@sa.ucsb.edu")
                                .dateAdded(ldt2)
                                .build();

                ArrayList<Articles> expectedDates = new ArrayList<>();
                expectedDates.addAll(Arrays.asList(article1, article2));

                when(articlesRepository.findAll()).thenReturn(expectedDates);

                // act
                MvcResult response = mockMvc.perform(get("/api/articles/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(articlesRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedDates);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_article() throws Exception {
                // arrange
                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                Articles article1 = Articles.builder()
                                .title("Who is the best professor at UCSB?")
                                .url("https://www.reddit.com/r/UCSantaBarbara/comments/1e46yoe/who_is_the_best_professor_youve_had_who_is_the/")
                                .explanation("A Reddit thread discussing the best professors at UCSB.")
                                .email("noreply@reddit.com")
                                .dateAdded(ldt1)
                                .build();

                when(articlesRepository.save(eq(article1))).thenReturn(article1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/articles/post?title=Who is the best professor at UCSB?&url=https://www.reddit.com/r/UCSantaBarbara/comments/1e46yoe/who_is_the_best_professor_youve_had_who_is_the/&explanation=A Reddit thread discussing the best professors at UCSB.&email=noreply@reddit.com&dateAdded=2022-01-03T00:00:00")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(articlesRepository, times(1)).save(article1);
                String expectedJson = mapper.writeValueAsString(article1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

                @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_an_article() throws Exception {
                // arrange

                Articles article = Articles.builder()
                        .title("Original Title")
                        .url("http://original.com")
                        .explanation("Original explanation")
                        .email("hello@original.com")
                        .dateAdded(LocalDateTime.parse("2022-01-03T00:00:00"))
                        .id(67L)
                        .build();

                when(articlesRepository.findById(eq(67L))).thenReturn(Optional.of(article));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/articles?id=67")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(articlesRepository, times(1)).findById(67L);
                verify(articlesRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Article with id 67 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_article_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(articlesRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/articles?id=67")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(articlesRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Articles with id 67 not found", json.get("message"));
        }

        // Test class modifications
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_article() throws Exception {
                // arrange
                LocalDateTime origDateTime = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime editedDateTime = LocalDateTime.parse("2023-12-25T12:30:00");

                Articles articleOrig = Articles.builder()
                                .title("Original Title")
                                .url("http://original.com")
                                .explanation("Original explanation")
                                .email("hello@original.com")
                                .dateAdded(origDateTime)
                                .id(67L)
                                .build();

                Articles articleEdited = Articles.builder()
                                .title("Who is the best professor at UCSB?")
                                .url("https://www.reddit.com/r/UCSantaBarbara/comments/1e46yoe/who_is_the_best_professor_youve_had_who_is_the/")
                                .explanation("A Reddit thread discussing the best professors at UCSB.")
                                .email("noreply@reddit.com")
                                .dateAdded(editedDateTime)
                                .id(67L)
                                .build();

                String requestBody = mapper.writeValueAsString(articleEdited);

                when(articlesRepository.findById(eq(67L))).thenReturn(Optional.of(articleOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/articles?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(articlesRepository, times(1)).findById(67L);

                ArgumentCaptor<Articles> articlesCaptor = ArgumentCaptor.forClass(Articles.class);
                verify(articlesRepository, times(1)).save(articlesCaptor.capture());
                Articles savedArticle = articlesCaptor.getValue();

                assertEquals(articleEdited.getTitle(), savedArticle.getTitle());
                assertEquals(articleEdited.getUrl(), savedArticle.getUrl());
                assertEquals(articleEdited.getExplanation(), savedArticle.getExplanation());
                assertEquals(articleEdited.getEmail(), savedArticle.getEmail());
                assertEquals(articleEdited.getDateAdded(), savedArticle.getDateAdded());

                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_article_that_does_not_exist() throws Exception {
                // arrange

                Articles editedArticles = Articles.builder()
                                .title("Who is the best professor at UCSB?")
                                .url("https://www.reddit.com/r/UCSantaBarbara/comments/1e46yoe/who_is_the_best_professor_youve_had_who_is_the/")
                                .explanation("A Reddit thread discussing the best professors at UCSB.")
                                .email("noreply@reddit.com")
                                .dateAdded(LocalDateTime.parse("2022-01-03T00:00:00"))
                                .build();

                String requestBody = mapper.writeValueAsString(editedArticles);

                when(articlesRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/articles?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(articlesRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Articles with id 67 not found", json.get("message"));

        }
}
