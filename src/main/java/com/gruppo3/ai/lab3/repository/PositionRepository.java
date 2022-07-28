package com.gruppo3.ai.lab3.repository;

import com.gruppo3.ai.lab3.model.Archive;
import com.gruppo3.ai.lab3.model.PositionEntity;
import org.springframework.data.geo.Polygon;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PositionRepository extends MongoRepository<PositionEntity, String> {
    List<PositionEntity> findAllBySubject(String subject_id);

    List<PositionEntity> findBySubjectAndTimestampBetween(String subject, Long minTime, Long maxTime);

    List<PositionEntity> findByTimestampBetween(Long minTime, Long maxTime);

    List<PositionEntity> findByPositionWithinAndTimestampBetween(Polygon polygon, Long minTime, Long maxTime);

    List<PositionEntity> findByPositionWithin(Polygon polygon);

    List<PositionEntity> findAllBySubjectOrderByTimestamp(String subject_id);

    List<PositionEntity> findAllByArchiveId(String archiveId);

    PositionEntity findByArchiveId(String archiveId);

}
