package com.ecommerce.project.config;

import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Role;
import com.ecommerce.project.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role(AppRole.ROLE_USER));
            roleRepository.save(new Role(AppRole.ROLE_ADMIN));
            roleRepository.save(new Role(AppRole.ROLE_SELLER));
            System.out.println("✅ Default roles added to database");
        } else {
            System.out.println("✅ Roles already exist, skipping initialization");
        }
    }
}

