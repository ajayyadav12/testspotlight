package com.ge.finance.spotlight.controllers;

import com.ge.finance.spotlight.exceptions.ForbiddenException;
import com.ge.finance.spotlight.exceptions.NotFoundException;
import com.ge.finance.spotlight.models.ModuleFilter;
import com.ge.finance.spotlight.models.User;
import com.ge.finance.spotlight.repositories.ModuleFilterRepository;
import com.ge.finance.spotlight.repositories.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/module-filters")
public class ModuleFilterController {

    private ModuleFilterRepository moduleFilterRepository;
    private UserRepository userRepository;

    public ModuleFilterController(ModuleFilterRepository moduleFilterRepository, UserRepository userRepository) {
        this.moduleFilterRepository = moduleFilterRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/{moduleName}")
    List<ModuleFilter> index(Authentication authentication, @PathVariable(name = "moduleName") String moduleName) {
        Long sso = (Long) authentication.getPrincipal();
        User user = userRepository.findFirstBySso(sso);
        return moduleFilterRepository.findByUserIdAndModuleNameAndSettingsIsNotNull(user.getId(), moduleName);
    }

    @PostMapping("/")    
    ModuleFilter create(@RequestBody ModuleFilter moduleFilter, Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        User user = userRepository.findFirstBySso(sso);
        moduleFilter.setUser(user);
        return moduleFilterRepository.save(moduleFilter);
    }

    @PutMapping("/")
    Collection<ModuleFilter> updateMultiple(@RequestBody Collection<ModuleFilter> moduleFilters, Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        if (moduleFilters.isEmpty()) return Collections.emptyList();
        List<Long> idList = moduleFilters.stream().map(ModuleFilter::getId).collect(Collectors.toList());
        if (moduleFilterRepository.existsAllForIdListAndSso(idList, sso)) {
            return moduleFilterRepository.saveAll(moduleFilters);
        } else {
            throw new ForbiddenException();
        }
    }

    @PutMapping("/{moduleFilterId}")
    ModuleFilter update(@PathVariable(name = "moduleFilterId") Long moduleFilterId,
            @RequestBody ModuleFilter moduleFilter) {
        if (moduleFilterRepository.existsById(moduleFilterId)) {
            moduleFilter.setId(moduleFilterId);
            return moduleFilterRepository.save(moduleFilter);
        } else {
            throw new NotFoundException();
        }
    }

    @DeleteMapping("/{moduleFilterId}")
    ModuleFilter delete(@PathVariable(name = "moduleFilterId") Long moduleFilterId) {
        ModuleFilter moduleFilter = moduleFilterRepository.findById(moduleFilterId).orElseThrow(NotFoundException::new);
        moduleFilterRepository.deleteById(moduleFilterId);
        return moduleFilter;
    }

}