package com.ge.finance.spotlight.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ge.finance.spotlight.models.ClosePhase;
import com.ge.finance.spotlight.models.Receiver;
import com.ge.finance.spotlight.repositories.ProcessRepository;
import com.ge.finance.spotlight.repositories.ReceiverRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

@RunWith(SpringRunner.class)
@WebMvcTest(ReceiverController.class)
public class ReceiverControllerTest {

    @MockBean
    private ReceiverRepository receiverRepository;

    @MockBean
    private ProcessRepository processRepository;

    @Autowired
    private MockMvc mockMvc;
 
    @Test     
    @WithMockUser
    public void getReceivers() throws Exception {
        List<Receiver> list = List.of(new Receiver());
        when(receiverRepository.findAll()).thenReturn(list);

        this.mockMvc.perform(get("/v1/receivers/"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content()
        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test     
    @WithMockUser
    public void getExistingReceiver() throws Exception {
        Receiver receiver = new Receiver();
        receiver.setName("R-TEST");
        Optional<Receiver> oReceiver = Optional.of(receiver);
        
        when(receiverRepository.findById(1l)).thenReturn(oReceiver);

        this.mockMvc.perform(get("/v1/receivers/{receiverId}", 1))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content()
        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.name", is("R-TEST")));
    }

    @Test     
    @WithMockUser
    public void getNonExistingReceiver() throws Exception {        
        Optional<Receiver> oReceiver = Optional.empty();
        
        when(receiverRepository.findById(1l)).thenReturn(oReceiver);

        this.mockMvc.perform(get("/v1/receivers/{receiverId}", 1))
        .andDo(print())
        .andExpect(status().isNotFound());
    }

    // @Test     
    // @WithMockUser(authorities="admin")
    // public void createReceiver() throws Exception {        
    //     Receiver receiver = new Receiver();
    //     receiver.setName("R-TEST");        
    //     ClosePhase closePhase = new ClosePhase();
    //     closePhase.setId(1l);
    //     receiver.setClosePhase(closePhase);
    //     Receiver updReceiver = new Receiver();
    //     updReceiver.setId(1l);
    //     when(receiverRepository.save(any(Receiver.class))).thenReturn(updReceiver);

    //     String receiverJson = new ObjectMapper().writeValueAsString(receiver);
    //     this.mockMvc.perform(
    //     post("/v1/receivers/")
    //         .contentType(MediaType.APPLICATION_JSON)
    //         .content(asJsonString(receiver))
    //         .accept(MediaType.APPLICATION_JSON))
    //     .andExpect(status().isOk())
    //     .andExpect(content()
    //         .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
    //     .andExpect(jsonPath("$.id", is(equalTo(1))));
    // }

    @Test     
    @WithMockUser
    public void createReceiver_nonAdmin() throws Exception {
        Receiver receiver = new Receiver();
        receiver.setName("R-TEST");
        ClosePhase closePhase = new ClosePhase();
        closePhase.setId(1l);
        receiver.setClosePhase(closePhase);
        
        when(receiverRepository.save(receiver)).thenReturn(receiver);
        String receiverJson = new ObjectMapper().writeValueAsString(receiver);
        this.mockMvc.perform(post("/v1/receivers/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(receiver))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
    }

    @Test     
    @WithMockUser
    public void EditReceiver_nonAdmin() throws Exception {     
        Receiver receiver = new Receiver();
        receiver.setName("R-TEST-Mod");
        ClosePhase closePhase = new ClosePhase();
        closePhase.setId(1l);
        receiver.setClosePhase(closePhase);            

        this.mockMvc.perform(put("/v1/receivers/{receiverId}", 1)
            .contentType(MediaType.APPLICATION_JSON)   
            .content(asJsonString(receiver))         
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
    }

    @Test     
    @WithMockUser
    public void deleteReceiver_nonAdmin() throws Exception {        
        this.mockMvc.perform(delete("/v1/receivers/{receiverId}", 1)
            .contentType(MediaType.APPLICATION_JSON)            
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
    }

    // @Test     
    // @WithMockUser(authorities="admin")
    // public void deleteReceiver_existingProcesses() throws Exception {
    //     when(processRepository.countByReceiverId(1l)).thenReturn(1l);

    //     this.mockMvc.perform(delete("/v1/receivers/{receiverId}", 1)
    //         .contentType(MediaType.APPLICATION_JSON)            
    //         .accept(MediaType.APPLICATION_JSON))
    //     .andExpect(status().isConflict());
    // }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}