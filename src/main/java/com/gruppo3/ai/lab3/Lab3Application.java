package com.gruppo3.ai.lab3;

import com.gruppo3.ai.lab3.model.Role;
import com.gruppo3.ai.lab3.model.User;
import com.gruppo3.ai.lab3.repository.*;
import com.gruppo3.ai.lab3.service.TokenBlackListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SpringBootApplication
public class Lab3Application {

    private final UserRepository userRepository;

    @Autowired
    private TokenBlackListService tokenBlackListService;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private ApproximatedPositionCoordinatesRepository approximatedPositionCoordinatesRepository;

    @Autowired
    private ApproximatedPositionTimestampRepository approximatedPositionTimestampRepository;

    @Autowired
    private ArchiveRepository archiveRepository;

    @Autowired
    public Lab3Application(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(Lab3Application.class, args);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public CommandLineRunner initDB(final UserRepository userRepository) {
        String defaultPassword = "Pass1234%";
        // controlla se il token è scaduto
        tokenBlackListService.checkBlackTokensValidity();

        return args -> {
            List<User> users = new ArrayList<>();
            /*
             * Create 3 users with 4 different roles
              - ADMIN
              - USER
              - CUSTOMER
             */

            //per debug cancello tutto all'inizio
            approximatedPositionTimestampRepository.deleteAll();;
            approximatedPositionCoordinatesRepository.deleteAll();
            archiveRepository.deleteAll();
            positionRepository.deleteAll();


            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder().encode(defaultPassword));
            admin.setRoles(Collections.singletonList(new Role("ADMIN")));
            users.add(admin);
            List<Role> roles = new ArrayList<Role>();
            roles.add(new Role("USER"));
            roles.add(new Role("CUSTOMER"));
            User user = new User();
            user.setUsername("simone");
            user.setPassword(passwordEncoder().encode(defaultPassword));
            user.setRoles(roles);
            user.setAlias("User 1");
            user.setAmount(1000);
            users.add(user);
            User user2 = new User();
            user2.setUsername("chiara");
            user2.setPassword(passwordEncoder().encode(defaultPassword));
            user2.setRoles(roles);
            user2.setAlias("User 2");
            user2.setAmount(1000);
            users.add(user2);
            User user3 = new User();
            user3.setUsername("alberto");
            user3.setPassword(passwordEncoder().encode(defaultPassword));
            user3.setRoles(roles);
            user3.setAlias("User 3");
            user3.setAmount(1000);
            // user3.setRoles(Collections.singletonList(new Role("USER")));
            users.add(user3);
            User user4 = new User();
            user4.setUsername("gianluca");
            user4.setPassword(passwordEncoder().encode(defaultPassword));
            user4.setRoles(roles);
            user4.setAlias("User 4");
            user4.setAmount(1000);
            // user3.setRoles(Collections.singletonList(new Role("USER")));
            users.add(user4);
            User customer = new User();
            customer.setUsername("gtt");
            customer.setPassword(passwordEncoder().encode(defaultPassword));
            customer.setRoles(roles);
            customer.setAlias("User 5");
            customer.setAmount(1000);
            users.add(customer);
            User customer2 = new User();
            customer2.setUsername("enjoy");
            customer2.setPassword(passwordEncoder().encode(defaultPassword));
            customer2.setRoles(roles);
            customer2.setAmount(2000);
            customer2.setAlias("User 6");
            users.add(customer2);
            // delete previous users
            userRepository.deleteAll();
            // store the 3 users
            userRepository.saveAll(users);
        };
    }

}
