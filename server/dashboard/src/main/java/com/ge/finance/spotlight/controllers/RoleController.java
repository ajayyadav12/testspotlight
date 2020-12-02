package com.ge.finance.spotlight.controllers;

import com.ge.finance.spotlight.models.Role;
import com.ge.finance.spotlight.repositories.RoleRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/roles")
public class RoleController {

    private RoleRepository roleRepository;

    public RoleController(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @GetMapping("/")
    List<Role> index() {
        return roleRepository.findAll();
    }

}