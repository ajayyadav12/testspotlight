package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.System;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
public class SystemRepositoryJpaTest {

    @Autowired private SystemRepository systemRepository;

    @Test
    public void testFindAll() {
        System system = new System();
        system.setName("system");
        systemRepository.save(system);
        List<System> systems = systemRepository.findAll();
        assertEquals(1, systems.size());
    }

}
