package com.gruppo3.ai.lab3.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "archives")
public class Archive {

    //id univoco mongo
    @Id
    private String id;

    private String aliasID;

    @JsonIgnore
    private List<String> positions_id;

    @JsonIgnore
    //proprietario
    private String owner;
    //numero di volte che archivio è stato venduto
    private long n_sold;

    private int n_positions;

    //timestamp min e max per ritornare un archvio che rientri nel range
    @JsonIgnore
    private long minTime;
    @JsonIgnore
    private long maxTime;
    //timestamp di caricamento dell'archivio
    private long uploadTime;
    //lista posizioni usata solo per ritornare archivio all'utente quando da download
    //Transient non salva il campo in mongodb
    @Transient
    private List<PositionEntity> positions_list;

    @Transient
    private boolean just_bought;

    private double price;

    public Archive(){
        this.positions_list = new ArrayList<>();
    }

    public Archive(String owner, long minTime, long maxTime, long uploadTime) {
        this.owner = owner;
        this.minTime = minTime;
        this.maxTime = maxTime;
        this.uploadTime = uploadTime;
        this.positions_id = new ArrayList<>();
        this.positions_list = new ArrayList<>();
        n_sold = 0;
        just_bought = false;
        price = 0;
    }

    public Archive(String owner, long minTime, long maxTime, long uploadTime, String aliasID) {
        this.owner = owner;
        this.minTime = minTime;
        this.maxTime = maxTime;
        this.uploadTime = uploadTime;
        this.positions_id = new ArrayList<>();
        this.positions_list = new ArrayList<>();
        this.aliasID = aliasID;
        n_sold = 0;
        just_bought = false;
        price = 0;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public long getN_sold() {
        return n_sold;
    }

    public void setN_sold(long n_sold) {
        this.n_sold = n_sold;
    }

    public List<String> getPositionsIdList() {
        return positions_id;
    }

    public void setPositionsIdList(List<String> positions_id) {
        this.positions_id = positions_id;
    }

    public long getMinTime() {
        return minTime;
    }

    public void setMinTime(long minTime) {
        this.minTime = minTime;
    }

    public long getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(long maxTime) {
        this.maxTime = maxTime;
    }

    public long getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(long uploadTime) {
        this.uploadTime = uploadTime;
    }

    public List<PositionEntity> getPositions_list() {
        return positions_list;
    }

    public void setPositions_list(List<PositionEntity> positions_list) {
        this.positions_list = positions_list;
    }

    public boolean isJust_bought() {
        return just_bought;
    }

    public void setJust_bought(boolean just_bought) {
        this.just_bought = just_bought;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getN_positions() {
        return n_positions;
    }

    public void setN_positions(int n_positions) {
        this.n_positions = n_positions;
    }

    public String getAliasID() {
        return aliasID;
    }

    public void setAliasID(String aliasID) {
        this.aliasID = aliasID;
    }

    @Override
    public String toString() {
        return "Archive{" +
                "id='" + id + '\'' +
                ", owner='" + owner + '\'' +
                ", n_sold=" + n_sold +
                ", positions=" + positions_id +
                ", minTime=" + minTime +
                ", maxTime=" + maxTime +
                ", uploadTime=" + uploadTime +
                '}';
    }
}