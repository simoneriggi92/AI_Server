package com.gruppo3.ai.lab3.repository;


import com.gruppo3.ai.lab3.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    User findByUsername(String username);
    User save(User user);

}
