package com.ge.finance.spotlight.endpoint;

import com.auth0.jwt.JWT;
import com.ge.finance.spotlight.models.Role;
import com.ge.finance.spotlight.models.User;
import com.ge.finance.spotlight.repositories.RoleRepository;
import com.ge.finance.spotlight.repositories.UserRepository;
import com.ge.finance.spotlight.security.Constants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RoleEndpointTest {

    @Value("${dash_secret}") String secret;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @LocalServerPort private int port;

    @Test
    public void testGetRolesUnauthenticated() {
        HttpEntity<String> entity = new HttpEntity<>(null, new HttpHeaders());
        ResponseEntity<String> response = new TestRestTemplate().exchange(String.format("http://localhost:%d/dashapi/v1/roles/", port), HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void testGetRolesAuthenticated() {
        // prepare user
        Role role = new Role();
        role.setDescription("role");
        role = roleRepository.save(role);
        User user = new User();
        user.setSso(999999999L);
        user.setRole(role);
        userRepository.save(user);
        // generate token
        String token = JWT.create().withSubject("999999999")                
                .sign(HMAC512(secret.getBytes()));
        // perform test
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", token);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<Role[]> response = new TestRestTemplate().exchange(String.format("http://localhost:%d/dashapi/v1/roles/", port), HttpMethod.GET, entity, Role[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().length);
    }

}
