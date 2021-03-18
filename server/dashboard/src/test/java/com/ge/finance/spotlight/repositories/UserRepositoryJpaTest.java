package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
public class UserRepositoryJpaTest {

    @Autowired private UserRepository userRepository;

    @Test
    public void testFindOptionalBySSOIsEmpty() {
        Optional<User> optionalUser = userRepository.findOptionalBySSO(1L);
        assertFalse(optionalUser.isPresent());
    }

    @Test
    public void testFindOptionalBySSOIsPresent() {
        User user = new User();
        user.setSso(1L);
        userRepository.save(user);
        Optional<User> optionalUser = userRepository.findOptionalBySSO(1L);
        assertTrue(optionalUser.isPresent());
        assertEquals((Long)1L, optionalUser.get().getSso());
    }

}
