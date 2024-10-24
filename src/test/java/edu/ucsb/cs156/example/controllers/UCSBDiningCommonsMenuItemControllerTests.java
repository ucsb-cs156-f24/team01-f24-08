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

    // @Test
    // public void logged_out_users_cannot_get_by_id() throws Exception {
    //         mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem?id=0"))
    //                         .andExpect(status().is(403)); // logged out users can't get by id
    // }

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
}
