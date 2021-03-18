package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.ModuleFilter;
import com.ge.finance.spotlight.models.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ModuleFilterRepositoryJpaTest {

    @Autowired private UserRepository userRepository;
    @Autowired private ModuleFilterRepository moduleFilterRepository;

    public void testFindByUserIdAndModuleNameAndSettingsIsNotNull() {
        String moduleName = "submissions";
        // Prepare user
        User user = new User();
        user.setSso(999999999L);
        user = userRepository.save(user);
        // Prepare module filters
        ModuleFilter moduleFilter1 = new ModuleFilter();
        moduleFilter1.setUser(user);
        moduleFilter1.setModuleName(moduleName);
        moduleFilter1.setSettings("{}");
        moduleFilter1 = moduleFilterRepository.save(moduleFilter1);
        ModuleFilter moduleFilter2 = new ModuleFilter();
        moduleFilter2.setUser(user);
        moduleFilter2.setModuleName(moduleName);
        moduleFilter2.setSettings(null);
        moduleFilter2 = moduleFilterRepository.save(moduleFilter2);
        // perform test
        List<ModuleFilter> moduleFilters = moduleFilterRepository.findByUserIdAndModuleNameAndSettingsIsNotNull(999999999L, "submissions");
        assertEquals(1, moduleFilters.size());
        assertNotNull(moduleFilters.get(0).getSettings());
    }

    @Test
    public void testExistsAllForIdListAndSsoReturnsTrue() {
        // Prepare user
        User user = new User();
        user.setSso(999999999L);
        user = userRepository.save(user);
        // Prepare module filter
        ModuleFilter moduleFilter1 = new ModuleFilter();
        moduleFilter1.setUser(user);
        moduleFilter1 = moduleFilterRepository.save(moduleFilter1);
        ModuleFilter moduleFilter2 = new ModuleFilter();
        moduleFilter2.setUser(user);
        moduleFilter2 = moduleFilterRepository.save(moduleFilter2);
        // Perform test
        boolean result = moduleFilterRepository.existsAllForIdListAndSso(List.of(moduleFilter1.getId(), moduleFilter2.getId()), 999999999L);
        assertTrue(result);
    }

    @Test
    public void testExistsAllForIdListAndSsoReturnsFalse() {
        // Prepare user
        User user = new User();
        user.setSso(999999999L);
        user = userRepository.save(user);
        // Prepare module filter
        ModuleFilter moduleFilter1 = new ModuleFilter();
        moduleFilter1.setUser(user);
        moduleFilter1 = moduleFilterRepository.save(moduleFilter1);
        ModuleFilter moduleFilter2 = new ModuleFilter();
        moduleFilter2.setUser(user);
        moduleFilter2 = moduleFilterRepository.save(moduleFilter2);
        // Perform test
        boolean result = moduleFilterRepository.existsAllForIdListAndSso(List.of(moduleFilter1.getId(), moduleFilter2.getId()), 999999990L);
        assertFalse(result);
    }

}
