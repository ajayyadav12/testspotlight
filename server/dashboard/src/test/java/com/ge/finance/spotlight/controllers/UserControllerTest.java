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
import com.ge.finance.spotlight.models.MessageGateway;
import com.ge.finance.spotlight.models.Role;
import com.ge.finance.spotlight.models.User;
import com.ge.finance.spotlight.repositories.ProcessRepository;
import com.ge.finance.spotlight.repositories.ProcessUserRepository;
import com.ge.finance.spotlight.repositories.UserRepository;

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
@WebMvcTest(UserController.class)
public class UserControllerTest {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ProcessUserRepository processUserRepository;

    @MockBean
    private ProcessRepository processRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void getUsers() throws Exception {
        List<User> list = List.of(new User());
        when(userRepository.findAll()).thenReturn(list);

        this.mockMvc.perform(get("/v1/users/")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    @WithMockUser
    public void getExistingUsers() throws Exception {
        User user = new User();
        user.setName("U-TEST");

        Optional<User> oUser = Optional.of(user);

        when(userRepository.findById(1l)).thenReturn(oUser);

        this.mockMvc.perform(get("/v1/users/{userId}", 1)).andDo(print()).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("U-TEST")));
    }

    @Test
    @WithMockUser
    public void getNonExistingUsers() throws Exception {
        Optional<User> oUser = Optional.empty();

        when(userRepository.findById(1l)).thenReturn(oUser);

        this.mockMvc.perform(get("/v1/users/{userId}", 1)).andDo(print()).andExpect(status().isNotFound());
    }

    // @Test
    // @WithMockUser(authorities = "admin")
    // public void createUser() throws Exception {
    //     User user = new User();
    //     user.setName("U-TEST");
    //     MessageGateway carrier = new MessageGateway();
    //     carrier.setId(1l);
    //     user.setCarrier(carrier);
    //     user.setPhoneNumber(1l);
    //     Role role = new Role();
    //     role.setId(1l);
    //     user.setRole(role);
    //     user.setSso(1l);

    //     User updUser = new User();
    //     updUser.setId(1l);
    //     when(userRepository.save(any(User.class))).thenReturn(updUser);

    //     String userJson = new ObjectMapper().writeValueAsString(user);
    //     this.mockMvc
    //             .perform(post("/v1/users/").contentType(MediaType.APPLICATION_JSON).content(asJsonString(user))
    //                     .accept(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
    //             .andExpect(jsonPath("$.id", is(equalTo(1))));
    // }

    @Test
    @WithMockUser
    public void createUser_nonAdmin() throws Exception {
        User user = new User();
        user.setName("U-TEST");
        MessageGateway carrier = new MessageGateway();
        carrier.setId(1l);
        user.setCarrier(carrier);
        user.setPhoneNumber(1l);
        Role role = new Role();
        role.setId(1l);
        user.setRole(role);
        user.setSso(1l);

        when(userRepository.save(user)).thenReturn(user);
        String userJson = new ObjectMapper().writeValueAsString(user);
        this.mockMvc.perform(post("/v1/users/").contentType(MediaType.APPLICATION_JSON).content(asJsonString(user))
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    public void EditUser_nonAdmin() throws Exception {
        User user = new User();
        user.setName("U-TEST-Mod");
        MessageGateway carrier = new MessageGateway();
        carrier.setId(1l);
        user.setCarrier(carrier);

        this.mockMvc
                .perform(put("/v1/users/{userId}", 1).contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(user)).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    public void deleteUser_nonAdmin() throws Exception {
        this.mockMvc.perform(delete("/v1/users/{userId}", 1).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    // @Test
    // @WithMockUser(authorities = "admin")
    // public void deleteUser_existingProcesses() throws Exception {
    //     when(processRepository.countByAppOwnerId(1l)).thenReturn(1l);

    //     this.mockMvc.perform(delete("/v1/users/{userId}", 1).contentType(MediaType.APPLICATION_JSON)
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