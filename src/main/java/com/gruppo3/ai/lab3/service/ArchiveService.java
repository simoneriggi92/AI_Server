package com.gruppo3.ai.lab3.service;

import com.gruppo3.ai.lab3.model.*;

import java.util.List;

public interface ArchiveService {

    List<ResponseContainer> addArchive(String username, List<PositionEntry> entries);
    void deleteArchive(String archive_id, String username);
    void deleteAllArchives(String username);
    Archive getArchive(String archive_id, String username);
    PositionsResponseContainer getApproximatedPositonsByTimestampAndPolygon(List<PositionEntry> entries, Long minTime, Long maxTime, String subject);
    PositionsResponseContainer getAppximatedPositionsByTimestampPolygonUsers(List<PositionEntry> entries, Long minTime, Long maxTime, List<User> users);
    Archive getBoughtArchive(String archive_id, String username);
    List<BoughtArchive> getBoughtArchives(String username);
    List<Archive> getArchivesInsidePolygon(BuyRequest buyRequest, String subject);
    List<Archive> getArchives(String username);
}
