package com.gruppo3.ai.lab3.service;

import com.gruppo3.ai.lab3.exception.DuplicateUserException;
import com.gruppo3.ai.lab3.exception.OldPasswordNotValidException;
import com.gruppo3.ai.lab3.exception.PassAndConfirmPassDontMatchException;
import com.gruppo3.ai.lab3.exception.UserNotFoundException;
import com.gruppo3.ai.lab3.model.CustomUserDetails;
import com.gruppo3.ai.lab3.model.Role;
import com.gruppo3.ai.lab3.model.User;
import com.gruppo3.ai.lab3.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService, UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);

        if (user != null) {
            return new CustomUserDetails(user);
        }
        throw new UsernameNotFoundException(username);

    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<String> getAllUsersId() {
        List<String> usersId = new ArrayList<>();
        userRepository.findAll().forEach(u -> usersId.add(u.getId()));
        return usersId;
    }

    @Override
    public User getUser(String username) {
        User user = userRepository.findByUsername(username);

        if (user != null) {
            return user;
        }

        throw new UserNotFoundException(username);
    }

    @Override
    public User saveUser(String username, String password, String confirmPassword) {
        if (userRepository.findByUsername(username) != null) {
            throw new DuplicateUserException();
        }
        System.out.println("PASSWORD: " + password);
        System.out.println("CONFIRM PASSWORD: " + confirmPassword);
        if (!password.equals(confirmPassword))
            throw new PassAndConfirmPassDontMatchException();
        List<Role> roles = new ArrayList<Role>();
        roles.add(new Role("USER"));
        roles.add(new Role("CUSTOMER"));
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder().encode(password));
        user.setRoles(roles);
        user.setAlias("User " + this.userRepository.findAll().size());
        user = userRepository.save(user);
        return user;
    }

    @Override
    public User updateUser(User oldUser, String oldPassword, String newPassword, String confirmPassword) {

        System.out.println("OLD PASSWORD: " + oldPassword);
        System.out.println("NEW PASSWORD: " + newPassword);
        System.out.println("CONFIRM PASSWORD: " + confirmPassword);
        if (!newPassword.equals(confirmPassword))
            throw new PassAndConfirmPassDontMatchException();

        User user = userRepository.findByUsername(oldUser.getUsername());
        String newPass = passwordEncoder().encode(newPassword);

        if (passwordEncoder().matches(oldPassword, user.getPassword())) {
            System.out.println("PSWS CORRISPONDENTI");
            user.setPassword(newPass);
            return userRepository.save(user);
        } else
            throw new OldPasswordNotValidException();
    }

    @Override
    public User updateWallet(double wallet) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User user = userRepository.findByUsername(currentUsername);
        user.setAmount(user.getAmount() + wallet);
        return userRepository.save(user);
    }
}

