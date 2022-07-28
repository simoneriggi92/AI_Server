package com.gruppo3.ai.lab3.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

public class OldPositionsResponse {
    private int n_users;
    private List<List<ApproximatedPositionCoordinatesEntity>> approxPositionsList;
    //lista che contiene i timestamp delle posizioni
    private List<ApproximatedPositionTimestampEntity>timelineList;
    private List<Archive> archiveList;
    private double total;
    private int n_positions;

    public OldPositionsResponse(){
        this.approxPositionsList = new ArrayList<>();
        this.archiveList = new ArrayList<>();
        n_users = 0;
        this.total = 0;
        this.n_positions = 0;
    }

    public int getN_users() {
        return n_users;
    }

    public void setN_users(int n_users) {
        this.n_users = n_users;
    }

    public List<List<ApproximatedPositionCoordinatesEntity>> getApproxPositionsList() {
        return approxPositionsList;
    }

    public void setApproxPositionsList(List<List<ApproximatedPositionCoordinatesEntity>> approxPositionsList) {
        this.approxPositionsList = approxPositionsList;
    }

    public List<ApproximatedPositionTimestampEntity> getTimelineList() {
        return timelineList;
    }

    public void setTimelineList(List<ApproximatedPositionTimestampEntity> timelineList) {
        this.timelineList = timelineList;
    }

    public List<Archive> getArchiveList() {
        return archiveList;
    }

    public void setArchiveList(List<Archive> archiveList) {
        this.archiveList = archiveList;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public int getN_positions() {
        return n_positions;
    }

    public void setN_positions(int n_positions) {
        this.n_positions = n_positions;
    }
}
