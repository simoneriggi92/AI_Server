package com.gruppo3.ai.lab3.repository;

import com.gruppo3.ai.lab3.model.Archive;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArchiveRepository extends MongoRepository<Archive, String> {

    Archive findByIdAndOwner(String Id, String owner);
    List<Archive> findAllByOwner(String owner);
    void deleteAllByOwner(String owner);
}