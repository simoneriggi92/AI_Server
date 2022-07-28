package com.gruppo3.ai.lab3.service;

import com.gruppo3.ai.lab3.exception.AllInvalidPositionsException;
import com.gruppo3.ai.lab3.exception.InvalidPositionsException;
import com.gruppo3.ai.lab3.exception.NotValidPolygonPointsException;
import com.gruppo3.ai.lab3.exception.NotValidTimestampException;
import com.gruppo3.ai.lab3.model.*;
import com.gruppo3.ai.lab3.repository.PositionRepository;
import com.gruppo3.ai.lab3.utils.PositionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.data.geo.Polygon;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;

    @Autowired
    public PositionServiceImpl(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    @Override
    public List<PositionEntity> getAllPositions() {
        return positionRepository.findAll();
    }

    @Override
    public List<PositionEntity> getAllPositionsTimestampBetween(Long minTime, Long maxTime) {
        if (minTime <= maxTime) {
            return positionRepository.findByTimestampBetween(minTime, maxTime);
        }
        throw new NotValidTimestampException();
    }

    @Override
    public List<PositionEntity> getUserPositions(String username) {
        return positionRepository.findAllBySubject(username);

    }

    @Override
    public List<PositionEntity> getUserPositionsTimestampBetween(String username, Long minTime, Long maxTime) {
        if (minTime <= maxTime) {
            return positionRepository.findBySubjectAndTimestampBetween(username, minTime, maxTime);
        }
        throw new NotValidTimestampException();
    }

    @Override
    public List<InvalidPositionEntity> addPositions(String username, List<PositionEntry> entries) {

//        List<PositionEntity> entities = new ArrayList<>();
//
//        for (PositionEntry location : entries) {
//            GeoJsonPoint locationPoint = new GeoJsonPoint(
//                    location.getLongitude(),
//                    location.getLatitude());
//            entities.add(new PositionEntity(username, locationPoint, location.getTimestamp()));
//        }
//
//        /* validazione */
//        ResponseContainer invalidContainer;
//        invalidContainer = PositionUtils.addCandidate(entities, username, positionRepository);
//        positionRepository.saveAll(entities);
//        if (entities.isEmpty()) {
//            throw new AllInvalidPositionsException(invalidContainer.getInvalidList());
//        } else if (!invalidContainer.isAllPositionValid()) {
//            throw new InvalidPositionsException(invalidContainer.getInvalidList());
//        }
        return null;
    }

    @Override
    public PositionPacket getPolygon(List<PositionEntry> entries, Long minTime, Long maxTime) {
        if (minTime <= maxTime) {
            List<PositionEntity> list;
            List<String> listNoUserDuplication = new ArrayList<>();
            List<String> users;
            List<Point> points = new ArrayList<>();
            for (PositionEntry location : entries) {
                Point locationPoint = new Point(
                        location.getLongitude(),
                        location.getLatitude());
                points.add(locationPoint);
            }
            Polygon polygon = new Polygon(points);
            if (polygon.getPoints().size() >= 1) {
                list = positionRepository.findByPositionWithinAndTimestampBetween(polygon, minTime, maxTime);

                System.out.println("SIZE: " + list.size());
                users = getUsernameList(polygon, minTime, maxTime);

                //prendo lista username(duplicati), e ne salvo soltanto uno per utente
                if (users != null && users.size() != 0) {

                    for (String username : users) {

                        if (!listNoUserDuplication.contains(username))
                            listNoUserDuplication.add(username);
                    }
                }
                return new PositionPacket(list.size(), listNoUserDuplication.size());
            }

            throw new NotValidPolygonPointsException();

        }

        throw new NotValidTimestampException();

    }

    @Override
    public List<String> getUsernameList(Polygon polygon, Long minTime, Long maxTime) {

        List<PositionEntity> list = positionRepository.findByPositionWithinAndTimestampBetween(polygon, minTime, maxTime);
        if (!list.isEmpty()) {
            List<String> listUserPayment = new ArrayList<>();
            for (PositionEntity p : list) {
                listUserPayment.add(p.getSubject());
            }
            return listUserPayment;
        } else
            return null;
    }
}
