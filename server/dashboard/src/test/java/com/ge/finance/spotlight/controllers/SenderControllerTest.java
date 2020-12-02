package com.ge.finance.spotlight.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ge.finance.spotlight.models.ClosePhase;
import com.ge.finance.spotlight.models.Sender;
import com.ge.finance.spotlight.models.User;
import com.ge.finance.spotlight.repositories.ProcessRepository;
import com.ge.finance.spotlight.repositories.SenderRepository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(SenderController.class)
public class SenderControllerTest {

    @MockBean
    private SenderRepository senderRepository;

    @MockBean
    private ProcessRepository processRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void getSenders() throws Exception {
        List<Sender> list = List.of(new Sender());
        when(senderRepository.findAll()).thenReturn(list);

        this.mockMvc.perform(get("/v1/senders/")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    @WithMockUser
    public void getExistingSenders() throws Exception {
        Sender sender = new Sender();
        sender.setName("S-TEST");
        Optional<Sender> oSender = Optional.of(sender);

        when(senderRepository.findById(1l)).thenReturn(oSender);

        this.mockMvc.perform(get("/v1/senders/{senderId}", 1)).andDo(print()).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("S-TEST")));
    }

    @Test
    @WithMockUser
    public void getNonExistingSenders() throws Exception {
        Optional<Sender> oSender = Optional.empty();

        when(senderRepository.findById(1l)).thenReturn(oSender);

        this.mockMvc.perform(get("/v1/senders/{senderId}", 1)).andDo(print()).andExpect(status().isNotFound());
    }

    // @Test
    // @WithMockUser(authorities = "admin")
    // public void createSender() throws Exception {
    //     Sender sender = new Sender();
    //     sender.setName("S-TEST");
    //     ClosePhase closePhase = new ClosePhase();
    //     closePhase.setId(1l);
    //     User appOwner = new User();
    //     appOwner.setId(1l);
    //     sender.setAppOwner(appOwner);
    //     sender.setClosePhase(closePhase);
    //     Sender updSender = new Sender();
    //     updSender.setId(1l);
    //     when(senderRepository.save(any(Sender.class))).thenReturn(updSender);

    //     String senderJson = new ObjectMapper().writeValueAsString(sender);
    //     this.mockMvc
    //             .perform(post("/v1/senders/").contentType(MediaType.APPLICATION_JSON).content(asJsonString(sender))
    //                     .accept(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
    //             .andExpect(jsonPath("$.id", is(equalTo(1))));
    // }

    @Test
    @WithMockUser
    public void createSender_nonAdmin() throws Exception {
        Sender sender = new Sender();
        sender.setName("S-TEST");
        ClosePhase closePhase = new ClosePhase();
        closePhase.setId(1l);
        User appOwner = new User();
        appOwner.setId(1l);
        sender.setAppOwner(appOwner);
        sender.setClosePhase(closePhase);

        when(senderRepository.save(sender)).thenReturn(sender);
        String senderJson = new ObjectMapper().writeValueAsString(sender);
        this.mockMvc.perform(post("/v1/senders/").contentType(MediaType.APPLICATION_JSON).content(asJsonString(sender))
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    public void EditSender_nonAdmin() throws Exception {
        Sender sender = new Sender();
        sender.setName("S-TEST-Mod");
        ClosePhase closePhase = new ClosePhase();
        closePhase.setId(1l);
        sender.setClosePhase(closePhase);

        this.mockMvc
                .perform(put("/v1/senders/{senderId}", 1).contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(sender)).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    public void deleteSender_nonAdmin() throws Exception {
        this.mockMvc.perform(delete("/v1/senders/{senderId}", 1).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    // @Test
    // @WithMockUser(authorities = "admin")
    // public void deleteSender_existingProcesses() throws Exception {
    //     when(processRepository.countBySenderId(1l)).thenReturn(1l);

    //     this.mockMvc.perform(delete("/v1/senders/{senderId}", 1).contentType(MediaType.APPLICATION_JSON)
    //             .accept(MediaType.APPLICATION_JSON)).andExpect(status().isConflict());
    // }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}