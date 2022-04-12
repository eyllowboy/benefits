package com.andersenlab.benefits;

import com.andersenlab.benefits.controller.*;
import com.andersenlab.benefits.repository.*;
import com.andersenlab.benefits.service.*;
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
public class ABenefitsApplicationTests {
    private final CategoryController categoryController;
    private final CompanyController companyController;
    private final CsvDiscountLoaderController csvDiscountLoaderController;
    private final DiscountController discountController;
    private final LocationController locationController;
    private final RoleController roleController;
    private final UserController userController;

    private final CategoryService categoryService;
    private final CompanyService companyService;
    private final CsvDiscountLoaderService csvDiscountLoaderService;
    private final DiscountService discountService;
    private final LocationService locationService;
    private final RoleService roleService;
    private final UserService userService;

    private final CategoryRepository categoryRepository;
    private final CompanyRepository companyRepository;
    private final DiscountRepository discountRepository;
    private final LocationRepository locationRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Autowired
    public ABenefitsApplicationTests(final CategoryController categoryController,
                                     final CompanyController companyController,
                                     final CsvDiscountLoaderController csvDiscountLoaderController,
                                     final DiscountController discountController,
                                     final LocationController locationController,
                                     final RoleController roleController,
                                     final UserController userController,
                                     final CategoryService categoryService,
                                     final CompanyService companyService,
                                     final CsvDiscountLoaderService csvDiscountLoaderService,
                                     final DiscountService discountService,
                                     final LocationService locationService,
                                     final RoleService roleService,
                                     final UserService userService,
                                     final CategoryRepository categoryRepository,
                                     final CompanyRepository companyRepository,
                                     final DiscountRepository discountRepository,
                                     final LocationRepository locationRepository,
                                     final RoleRepository roleRepository,
                                     final UserRepository userRepository) {
        this.categoryController = categoryController;
        this.companyController = companyController;
        this.csvDiscountLoaderController = csvDiscountLoaderController;
        this.discountController = discountController;
        this.locationController = locationController;
        this.roleController = roleController;
        this.userController = userController;
        this.categoryService = categoryService;
        this.companyService = companyService;
        this.csvDiscountLoaderService = csvDiscountLoaderService;
        this.discountService = discountService;
        this.locationService = locationService;
        this.roleService = roleService;
        this.userService = userService;
        this.categoryRepository = categoryRepository;
        this.companyRepository = companyRepository;
        this.discountRepository = discountRepository;
        this.locationRepository = locationRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Container
    public static final PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres")
                    .withDatabaseName("benefits")
                    .withUsername("benefits")
                    .withPassword("ben0147");
    
    @DynamicPropertySource
    public static void postgreSQLProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    }
    
    @Test
    public void whenContextLoadsIsSuccess() {
        // then
        assertThat(this.companyController).isNotNull();
        assertThat(this.categoryController).isNotNull();
        assertThat(this.csvDiscountLoaderController).isNotNull();
        assertThat(this.discountController).isNotNull();
        assertThat(this.locationController).isNotNull();
        assertThat(this.roleController).isNotNull();
        assertThat(this.userController).isNotNull();

        assertThat(this.categoryService).isNotNull();
        assertThat(this.companyService).isNotNull();
        assertThat(this.csvDiscountLoaderService).isNotNull();
        assertThat(this.discountService).isNotNull();
        assertThat(this.locationService).isNotNull();
        assertThat(this.roleService).isNotNull();
        assertThat(this.userService).isNotNull();

        assertThat(this.categoryRepository).isNotNull();
        assertThat(this.companyRepository).isNotNull();
        assertThat(this.discountRepository).isNotNull();
        assertThat(this.locationRepository).isNotNull();
        assertThat(this.roleRepository).isNotNull();
        assertThat(this.userRepository).isNotNull();
    }
}