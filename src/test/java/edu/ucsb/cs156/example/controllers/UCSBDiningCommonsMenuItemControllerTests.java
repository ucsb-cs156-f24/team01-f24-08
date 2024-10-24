package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.repositories.UCSBDateRepository;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.glassfish.jaxb.runtime.v2.runtime.unmarshaller.XsiNilLoader.Array;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
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

@WebMvcTest(controllers = UCSBDiningCommonsMenuItemController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonsMenuItemControllerTests extends ControllerTestCase{
    @MockBean
    UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

    @MockBean
    UserRepository userRepository;

    //TESTS FOR GET
    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
            mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem/all"))
                            .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
            mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem/all"))
                            .andExpect(status().is(200)); // logged
    }

    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception {
            mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem?id=0"))
                            .andExpect(status().is(403)); // logged out users can't get by id
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_by_id() throws Exception {
            UCSBDiningCommonsMenuItem ucsbDCMI1 = UCSBDiningCommonsMenuItem.builder()
                        .diningCommonsCode("Test1")
                        .name("TEST2")
                        .station("TEST3")
                        .build();

            when(ucsbDiningCommonsMenuItemRepository.findById(eq(15L)))
                    .thenReturn(Optional.of(ucsbDCMI1));

            MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem?id=15"))
                                    .andExpect(status().isOk()).andReturn();// logged in users can get by id

            verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(15L);
            String expected = mapper.writeValueAsString(ucsbDCMI1);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expected, responseString);
            
            mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem?id=1"))
                            .andExpect(status().is(404));

    }

    //TESTS FOR POST
    @Test
    public void logged_out_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/ucsbdiningcommonsmenuitem/post"))
                            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/ucsbdiningcommonsmenuitem/post"))
                            .andExpect(status().is(403)); // only admins can post
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void logged_in_admins_can_post() throws Exception {
            UCSBDiningCommonsMenuItem ucsbDCMI = UCSBDiningCommonsMenuItem.builder()
                            .diningCommonsCode("DL")
                            .name("Pizza")
                            .station("Main")
                            .build();

            when(ucsbDiningCommonsMenuItemRepository.save(eq(ucsbDCMI)))
                            .thenReturn(ucsbDCMI);

            MvcResult response = mockMvc.perform(post("/api/ucsbdiningcommonsmenuitem/post?name=Pizza&station=Main&diningCommonsCode=DL")
                            .with(csrf()))
                            .andExpect(status().isOk())
                            .andReturn(); // only admins can post
            //verify
            verify(ucsbDiningCommonsMenuItemRepository, times(1))
                        .save(ucsbDCMI);
            
            String expectedJson = mapper.writeValueAsString(ucsbDCMI);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);

            assertEquals(ucsbDCMI.getDiningCommonsCode(), "DL");
            assertEquals(ucsbDCMI.getName(), "Pizza");
            assertEquals(ucsbDCMI.getStation(), "Main");

    }

    @WithMockUser(roles = {"USER"})
    @Test
    public void logged_in_users_can_get_everything() throws Exception {
        UCSBDiningCommonsMenuItem ucsbDCMI1 = UCSBDiningCommonsMenuItem.builder()
                            .diningCommonsCode("PL")
                            .name("Pasta")
                            .station("Stop")
                            .build();
        
        UCSBDiningCommonsMenuItem ucsbDCMI2 = UCSBDiningCommonsMenuItem.builder()
                            .diningCommonsCode("Test")
                            .name("Soup")
                            .station("Carillo")
                            .build();
        

        ArrayList<UCSBDiningCommonsMenuItem> ucsbDCMIList = new ArrayList<>();
        ucsbDCMIList.addAll(Arrays.asList(ucsbDCMI1, ucsbDCMI2));

        when (ucsbDiningCommonsMenuItemRepository.findAll())
            .thenReturn(ucsbDCMIList);
        
        MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem/all"))
                            .andExpect(status().isOk())
                            .andReturn();
        
        verify(ucsbDiningCommonsMenuItemRepository, times(1)).findAll();
        String expected = mapper.writeValueAsString(ucsbDCMIList);
        String responseString = response.getResponse().getContentAsString();

        assertEquals(expected, responseString);

    }

    //TESTS FOR DELETE
    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_delete_a_row() throws Exception {
            // arrange

            UCSBDiningCommonsMenuItem ucsbDCMI1 = UCSBDiningCommonsMenuItem.builder()
                            .diningCommonsCode("Test1")
                            .name("TEST2")
                            .station("TEST3")
                            .build();

            when(ucsbDiningCommonsMenuItemRepository.findById(eq(15L)))
                            .thenReturn(Optional.of(ucsbDCMI1));

            // act
            MvcResult response = mockMvc.perform(
                            delete("/api/ucsbdiningcommonsmenuitem?id=15")
                                            .with(csrf()))
                            .andExpect(status().isOk()).andReturn();

            // assert
            verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(15L);
            verify(ucsbDiningCommonsMenuItemRepository, times(1)).delete(any());

            Map<String, Object> json = responseToJson(response);
            assertEquals("UCSBDiningCommonsMenuItem with id 15 deleted", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_tries_to_delete_non_existant_ucsbDCMI_and_gets_right_error_message()
                    throws Exception {
            // arrange

            when(ucsbDiningCommonsMenuItemRepository.findById(eq(15L))).thenReturn(Optional.empty());

            // act
            MvcResult response = mockMvc.perform(
                            delete("/api/ucsbdiningcommonsmenuitem?id=15")
                                            .with(csrf()))
                            .andExpect(status().isNotFound()).andReturn();

            // assert
            verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(15L);
            Map<String, Object> json = responseToJson(response);
            assertEquals("UCSBDiningCommonsMenuItem with id 15 not found", json.get("message"));
    }

    //TESTS FOR PUT
    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_edit_an_existing_ucsbDCMI() throws Exception {
        UCSBDiningCommonsMenuItem ucsbDCMI1 = UCSBDiningCommonsMenuItem.builder()
                        .diningCommonsCode("Test1")
                        .name("TEST2")
                        .station("TEST3")
                        .build();

        UCSBDiningCommonsMenuItem ucsbDCMI2 = UCSBDiningCommonsMenuItem.builder()
                        .diningCommonsCode("Test4")
                        .name("TEST5")
                        .station("TEST6")
                        .build();

        String requestBody = mapper.writeValueAsString(ucsbDCMI2);

        when(ucsbDiningCommonsMenuItemRepository.findById(eq(15L)))
                        .thenReturn(Optional.of(ucsbDCMI1));

        MvcResult response = mockMvc.perform(
                        put("/api/ucsbdiningcommonsmenuitem?id=15")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody)
                                        .with(csrf()))
                        .andExpect(status().isOk()).andReturn();
        
        verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(15L);
        verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(ucsbDCMI2);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(requestBody, responseString);
           
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_cannot_edit_ucsbDCMI_that_DNI() throws Exception {
        UCSBDiningCommonsMenuItem ucsbDCMI1 = UCSBDiningCommonsMenuItem.builder()
                        .diningCommonsCode("Test1")
                        .name("TEST2")
                        .station("TEST3")
                        .build();
        
        String requestBody = mapper.writeValueAsString(ucsbDCMI1);

        when(ucsbDiningCommonsMenuItemRepository.findById(eq(15L)))
                        .thenReturn(Optional.empty());
        
        MvcResult response = mockMvc.perform(
                        put("/api/ucsbdiningcommonsmenuitem?id=15")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody)
                                        .with(csrf()))
                        .andExpect(status().isNotFound()).andReturn();
        
        verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(15L);
        Map<String, Object> json = responseToJson(response);
        assertEquals("UCSBDiningCommonsMenuItem with id 15 not found", json.get("message"));
    }
}
