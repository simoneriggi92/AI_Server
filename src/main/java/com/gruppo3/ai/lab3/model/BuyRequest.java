package com.gruppo3.ai.lab3.model;

import java.util.ArrayList;
import java.util.List;

//classe creata per ricevere una richiesta dal client di acquisto
//formata da :
//1 ) lista di archivi dentro il poligono
//2) sotto-poligono selezionato dall'utente

public class BuyRequest {

    private List<Archive> archiveList;
    private List<PositionEntry> polygonPoints;
    private List<User> usersList;


    private PositionsResponseContainer previousResponse;

    public BuyRequest() {
        this.archiveList = new ArrayList<>();
        this.polygonPoints = new ArrayList<>();
        this.usersList = new ArrayList<>();
        this.previousResponse = new PositionsResponseContainer();
    }

    public List<Archive> getArchiveList() {
        return archiveList;
    }

    public void setArchiveList(List<Archive> archiveList) {
        this.archiveList = archiveList;
    }

    public List<PositionEntry> getPolygonPoints() {
        return polygonPoints;
    }

    public void setPolygonPoints(List<PositionEntry> polygonPoints) {
        this.polygonPoints = polygonPoints;
    }

    public List<User> getUsersList() {
        return usersList;
    }

    public void setUsersList(List<User> usersList) {
        this.usersList = usersList;
    }

    public PositionsResponseContainer getPreviousResponse() {
        return previousResponse;
    }

    public void setPreviousResponse(PositionsResponseContainer previousResponse) {
        this.previousResponse = previousResponse;
    }
}
