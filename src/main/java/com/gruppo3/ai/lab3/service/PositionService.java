package com.gruppo3.ai.lab3.service;

import com.gruppo3.ai.lab3.model.InvalidPositionEntity;
import com.gruppo3.ai.lab3.model.PositionEntity;
import com.gruppo3.ai.lab3.model.PositionEntry;
import com.gruppo3.ai.lab3.model.PositionPacket;
import org.springframework.data.geo.Polygon;

import java.util.List;

public interface PositionService {

    List<PositionEntity> getAllPositions();

    List<PositionEntity> getAllPositionsTimestampBetween(Long minTime, Long maxTime);

    List<PositionEntity> getUserPositions(String username);

    List<PositionEntity> getUserPositionsTimestampBetween(String username, Long minTime, Long maxTime);

    List<InvalidPositionEntity> addPositions(String username, List<PositionEntry> entries);

    PositionPacket getPolygon(List<PositionEntry> entries, Long minTime, Long maxTime);

    List<String> getUsernameList(Polygon polygon, Long minTime, Long maxTime);
}
