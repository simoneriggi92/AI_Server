package com.gruppo3.ai.lab3.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PositionsResponseContainer {

    private int n_users;
    private List<List<ApproximatedPositionCoordinatesEntity>> approxPositionsList;
    //lista che contiene i timestamp delle posizioni
    private List<ApproximatedPositionTimestampEntity>timelineList;
    private List<Archive> archiveList;
    private double total;
    private int n_positions;

    public PositionsResponseContainer(){
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


    public boolean checkPositionsResponses(PositionsResponseContainer oldPositionsResponse, PositionsResponseContainer pRC) {
        boolean res = true;
        if (oldPositionsResponse.getN_positions() != pRC.getN_positions()) {
            System.out.println("CHECKING1: " + oldPositionsResponse.getN_positions() + " " + pRC.getN_positions());
            return false;
        }
        if (oldPositionsResponse.getN_users() != pRC.getN_users()) {
            System.out.println("CHECKING2: ");
            return false;
        }
        if (oldPositionsResponse.getTotal() != pRC.getTotal()) {
            System.out.println("CHECKING3: ");
            return false;
        }

        if(oldPositionsResponse.getArchiveList().size() != pRC.getArchiveList().size()) {
            System.out.println("CHECKING4-1: ");
            System.out.println(oldPositionsResponse.getArchiveList());
            System.out.println(pRC.getArchiveList());
            return false;
        }

        if(oldPositionsResponse.getTimelineList().size() != pRC.getTimelineList().size()) {
            return false;
        }
        for (int i = 0; i < oldPositionsResponse.getTimelineList().size(); i++) {
            if(!oldPositionsResponse.getTimelineList().get(i).getTimestamp().equals(pRC.getTimelineList().get(i).getTimestamp())) {
                System.out.println("CHECKING5: ");
                System.out.println(oldPositionsResponse.getTimelineList());
                System.out.println(pRC.getTimelineList());
                return false;
            }
        }

        if(oldPositionsResponse.getApproxPositionsList().size() != pRC.getApproxPositionsList().size()) {
            System.out.println("CHECKING6-1: ");
            System.out.println(oldPositionsResponse.getApproxPositionsList());
            System.out.println(pRC.getApproxPositionsList());
            return false;
        }
        for (int i = 0; i < oldPositionsResponse.getApproxPositionsList().size(); i++) {
            if(oldPositionsResponse.getApproxPositionsList().get(i).size() != pRC.getApproxPositionsList().get(i).size()) {
                System.out.println("CHECKING6-2: ");
                System.out.println(oldPositionsResponse.getApproxPositionsList());
                System.out.println(pRC.getApproxPositionsList());
                return false;
            }
            for (int j = 0; j < oldPositionsResponse.getApproxPositionsList().get(i).size(); j++) {

                if(!oldPositionsResponse.getApproxPositionsList().get(i).get(j).getMy_precise_position_id().equals(pRC.getApproxPositionsList().get(i).get(j).getMy_precise_position_id())) {
                    System.out.println("CHECKING6-3: ");
                    System.out.println(oldPositionsResponse.getApproxPositionsList());
                    System.out.println(pRC.getApproxPositionsList());
                    return false;
                }
            }
        }
        return res;
    }
}
