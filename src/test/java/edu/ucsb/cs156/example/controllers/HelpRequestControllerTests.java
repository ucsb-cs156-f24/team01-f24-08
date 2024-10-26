package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import edu.ucsb.cs156.example.repositories.UCSBDateRepository;

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

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = HelpRequestController.class)
@Import(TestConfig.class)
public class HelpRequestControllerTests extends ControllerTestCase{
    @MockBean
    HelpRequestRepository helpRequestRepository;

    @MockBean
    UserRepository userRepository;

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_helprequests() throws Exception {
        // arrange
        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

        
        HelpRequest helpRequest1 = HelpRequest.builder()
                        .requesterEmail("requester_email")
                        .teamId("team_id")
                        .tableOrBreakoutRoom("table_or_breakout")
                        .requestTime(ldt1) 
                        .explanation("explanation")
                        .solved(false)
                        .build(); 


        LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

        HelpRequest helpRequest2 = HelpRequest.builder()
                        .requesterEmail("requester_email")
                        .teamId("team_id")
                        .tableOrBreakoutRoom("table_or_breakout")
                        .requestTime(ldt2) 
                        .explanation("explanation")
                        .solved(false)
                        .build(); 

        ArrayList<HelpRequest> expectedHelpRequests = new ArrayList<>();
        expectedHelpRequests.addAll(Arrays.asList(helpRequest1, helpRequest2));

        when(helpRequestRepository.findAll()).thenReturn(expectedHelpRequests);

        // act
        MvcResult response = mockMvc.perform(get("/api/helprequests/all"))
                        .andExpect(status().isOk()).andReturn();

        // assert

        verify(helpRequestRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(expectedHelpRequests);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }   

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_helprequest() throws Exception {
        // arrange

        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

        HelpRequest helpRequest1 = HelpRequest.builder()
                            .requesterEmail("requester_email")
                            .teamId("team_id")
                            .tableOrBreakoutRoom("table_or_breakout")
                            .requestTime(ldt1) 
                            .explanation("explain")
                            .solved(true)
                            .build(); 


        when(helpRequestRepository.save(eq(helpRequest1))).thenReturn(helpRequest1);
        // act
        MvcResult response = mockMvc.perform(
                        post("/api/helprequests/post?requesterEmail=requester_email&teamId=team_id&tableOrBreakoutRoom=table_or_breakout&requestTime=2022-01-03T00:00:00&explanation=explain&solved=true")
                                .with(csrf()))
                        .andExpect(status().isOk()).andReturn();

        
        // assert
        verify(helpRequestRepository, times(1)).save(helpRequest1);
        String expectedJson = mapper.writeValueAsString(helpRequest1);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }
}