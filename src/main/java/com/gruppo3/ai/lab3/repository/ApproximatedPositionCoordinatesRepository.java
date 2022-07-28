package com.gruppo3.ai.lab3.repository;

import com.gruppo3.ai.lab3.model.ApproximatedPositionCoordinatesEntity;
import org.springframework.data.geo.Polygon;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApproximatedPositionCoordinatesRepository extends MongoRepository<ApproximatedPositionCoordinatesEntity, String> {

    List<ApproximatedPositionCoordinatesEntity>findByPositionWithin(Polygon polygon);
}
