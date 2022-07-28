package com.gruppo3.ai.lab3.repository;

import com.gruppo3.ai.lab3.model.TokenBlackList;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenBlackListRepository extends MongoRepository<TokenBlackList, String> {

    TokenBlackList findByJti(String jti);

}
