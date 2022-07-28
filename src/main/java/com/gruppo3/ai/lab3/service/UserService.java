package com.gruppo3.ai.lab3.service;

import com.gruppo3.ai.lab3.model.User;

import java.util.List;

public interface UserService {

    List<User> getAllUsers();

    List<String> getAllUsersId();

    User getUser(String username);

    User saveUser(String username, String password, String confirmPassword);

    User updateUser(User oldUser, String oldPassword, String newPassword, String confirmPassword);

    User updateWallet(double wallet);
}
