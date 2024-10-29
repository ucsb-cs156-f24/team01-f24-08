package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDiningCommons;
import edu.ucsb.cs156.example.entities.UCSBOrgs;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsRepository;
import edu.ucsb.cs156.example.repositories.UCSBOrgsRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class UCSBOrganizationControllerTests extends ControllerTestCase {

    @MockBean
    UCSBOrgsRepository ucsbOrgsRepository;

    @MockBean
    UserRepository userRepository;

    // Authorization tests for /api/ucsborganization/admin/all

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/ucsborganization/all"))
                    .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/ucsborganization/all"))
                    .andExpect(status().is(200)); // logged
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_ucsborgs() throws Exception {

        // arrange

        UCSBOrgs skiing = UCSBOrgs.builder()
            .orgCode("SKI")
            .orgTranslationShort("SKIING CLUB")
            .orgTranslation("SKIING CLUB AT UCSB")
            .inactive(false)
            .build();

        UCSBOrgs sigmanu = UCSBOrgs.builder()
            .orgCode("SNU")
            .orgTranslationShort("SIGMA NU")
            .orgTranslation("SIGMA NU FRATERNITY")
            .inactive(true)
            .build();

        ArrayList<UCSBOrgs> expectedOrgs = new ArrayList<>();
        expectedOrgs.addAll(Arrays.asList(skiing, sigmanu));

        when(ucsbOrgsRepository.findAll()).thenReturn(expectedOrgs);

        // act
        MvcResult response = mockMvc.perform(get("/api/ucsborganization/all"))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(ucsbOrgsRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(expectedOrgs);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }
    
    // @Test
    // public void logged_out_users_cannot_get_by_id() throws Exception {
    //     mockMvc.perform(get("/api/ucsborganization?orgCode=abc"))
    //                 .andExpect(status().is(403)); // logged out users can't get by id
    // }

    // Authorization tests for /api/ucsborganization/post
    // (Perhaps should also have these for put and delete)

    @Test
    public void logged_out_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/ucsborganization/post"))
                    .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/ucsborganization/post"))
                    .andExpect(status().is(403)); // only admins can post
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_org() throws Exception {
        // arrange

        UCSBOrgs sigmanu = UCSBOrgs.builder()
            .orgCode("SNU")
            .orgTranslationShort("SIGMA NU")
            .orgTranslation("SIGMA NU FRATERNITY")
            .inactive(true)
            .build();

        when(ucsbOrgsRepository.save(eq(sigmanu))).thenReturn(sigmanu);

        // act
        MvcResult response = mockMvc.perform(
            post("/api/ucsborganization/post?orgCode=SNU&orgTranslationShort=SIGMA NU&orgTranslation=SIGMA NU FRATERNITY&inactive=true")
                .with(csrf()))
            .andExpect(status().isOk()).andReturn();

        // assert
        verify(ucsbOrgsRepository, times(1)).save(sigmanu);
        String expectedJson = mapper.writeValueAsString(sigmanu);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }


    // Test get by id
    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception {
            mockMvc.perform(get("/api/ucsborganization?orgCode=SKY"))
                            .andExpect(status().is(403)); // logged out users can't get by id
    }
    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {
            // arrange
            UCSBOrgs skiing = UCSBOrgs.builder()
            .orgCode("SKI")
            .orgTranslationShort("SKIING CLUB")
            .orgTranslation("SKIING CLUB AT UCSB")
            .inactive(false)
            .build();
            when(ucsbOrgsRepository.findById(eq("SKI"))).thenReturn(Optional.of(skiing));
            // act
            MvcResult response = mockMvc.perform(get("/api/ucsborganization?orgCode=SKI"))
                            .andExpect(status().isOk()).andReturn();
            // assert
            verify(ucsbOrgsRepository, times(1)).findById(eq("SKI"));
            String expectedJson = mapper.writeValueAsString(skiing);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }
    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {
            // arrange
            when(ucsbOrgsRepository.findById(eq("ABC"))).thenReturn(Optional.empty());
            // act
            MvcResult response = mockMvc.perform(get("/api/ucsborganization?orgCode=ABC"))
                            .andExpect(status().isNotFound()).andReturn();
            // assert
            verify(ucsbOrgsRepository, times(1)).findById(eq("ABC"));
            Map<String, Object> json = responseToJson(response);
            assertEquals("EntityNotFoundException", json.get("type"));
            assertEquals("UCSBOrgs with id ABC not found", json.get("message"));
    }



    // Delete tests

    @Test
    public void logged_out_users_cannot_delete() throws Exception {
        mockMvc.perform(delete("/api/ucsborganization/delete"))
            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_delete() throws Exception {
        mockMvc.perform(delete("/api/ucsborganization/delete"))
            .andExpect(status().is(403)); // only admins can post
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_tries_to_delete_non_existant_orgnization_and_gets_right_error_message() throws Exception {
        // arrange

        when(ucsbOrgsRepository.findById(eq("WER"))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(
            delete("/api/ucsborganization?orgCode=WER")
                            .with(csrf()))
            .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(ucsbOrgsRepository, times(1)).findById("WER");
        Map<String, Object> json = responseToJson(response);
        assertEquals("UCSBOrgs with id WER not found", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_delete_an_orgnization() throws Exception {
        // arrange
        UCSBOrgs skiing = UCSBOrgs.builder()
            .orgCode("SKI")
            .orgTranslationShort("SKIING CLUB")
            .orgTranslation("SKIING CLUB AT UCSB")
            .inactive(false)
            .build();

        when(ucsbOrgsRepository.findById(eq("SKI"))).thenReturn(Optional.of(skiing));

        // act
        MvcResult response = mockMvc.perform(
            delete("/api/ucsborganization?orgCode=SKI")
                            .with(csrf()))
            .andExpect(status().isOk()).andReturn();
        
        // assert
        verify(ucsbOrgsRepository, times(1)).findById("SKI");
        verify(ucsbOrgsRepository, times(1)).delete(any());

        Map<String, Object> json = responseToJson(response);
        assertEquals("UCSBOrgs with id SKI deleted", json.get("message"));
    }
    
}
