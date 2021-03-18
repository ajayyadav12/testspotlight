package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.Role;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
public class RoleRepositoryJpaTest {

    @Autowired private RoleRepository roleRepository;

    @Test
    public void testFindAll() {
        roleRepository.save(new Role());
        List<Role> roles = roleRepository.findAll();
        assertEquals(1, roles.size());
    }

    @Test
    public void testFindById() {
        Role role = roleRepository.save(new Role());
        Optional<Role> found = roleRepository.findById(role.getId());
        assertTrue(found.isPresent());
        assertEquals(role.getId(), found.get().getId());
    }

}
