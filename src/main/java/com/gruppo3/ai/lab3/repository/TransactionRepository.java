package com.gruppo3.ai.lab3.repository;

import com.gruppo3.ai.lab3.model.TransactionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends MongoRepository<TransactionEntity, String> {
    List<TransactionEntity> findTransactionEntitiesBySender(String username);
}
