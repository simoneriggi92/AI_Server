package com.gruppo3.ai.lab3.repository;

import com.gruppo3.ai.lab3.model.ApproximatedPositionTimestampEntity;
import com.gruppo3.ai.lab3.model.PositionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApproximatedPositionTimestampRepository extends MongoRepository<ApproximatedPositionTimestampEntity, String> {
    List<ApproximatedPositionTimestampEntity> findByTimestampBetween(Long minTime, Long maxTime);

}
