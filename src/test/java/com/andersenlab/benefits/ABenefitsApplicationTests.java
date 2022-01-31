package com.andersenlab.benefits;

import com.andersenlab.benefits.controller.RoleController;
import com.andersenlab.benefits.controller.UserController;
import com.andersenlab.benefits.repository.RoleRepository;
import com.andersenlab.benefits.repository.UserRepository;
import com.andersenlab.benefits.service.RoleService;
import com.andersenlab.benefits.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class ABenefitsApplicationTests {
    @Autowired
    private RoleController roleController;
    @Autowired
    private UserController userController;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    
    @Container
    static final PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres")
                    .withDatabaseName("benefits")
                    .withUsername("benefits")
                    .withPassword("ben0147");
    
    @DynamicPropertySource
    static void postgreSQLProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    }
    
    @Test
    void contextLoads() {
        assertThat(roleController).isNotNull();
        assertThat(userController).isNotNull();
        assertThat(userService).isNotNull();
        assertThat(roleService).isNotNull();
        assertThat(roleRepository).isNotNull();
        assertThat(userRepository).isNotNull();
    }
}