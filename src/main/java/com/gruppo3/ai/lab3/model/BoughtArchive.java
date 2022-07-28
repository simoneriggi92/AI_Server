package com.gruppo3.ai.lab3.model;

import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BoughtArchive extends Archive {

    private String archive_id;
    private List<PositionEntity> positionList;
    private Date boughtDate;
    //private long n_sold;
    public BoughtArchive(){}

    public BoughtArchive(Archive archive){
        //(archive.getOwner(), archive.getMinTime(), archive.getMaxTime(), archive.getUploadTime());
        this.archive_id = archive.getId();
        positionList = archive.getPositions_list();
        this.setId(archive_id);
        this.setAliasID(archive.getAliasID());
       // n_sold = archive.getN_sold();
    }

    public List<PositionEntity> getPositionList() {
        return positionList;
    }

    public void setPositionList(List<PositionEntity> positionList) {
        this.positionList = positionList;
    }

    public String getArchive_id() {
        return archive_id;
    }

    public void setArchive_id(String archive_id) {
        this.archive_id = archive_id;
    }

    public Date getBoughtDate() {
        return boughtDate;
    }

    public void setBoughtDate(Date boughtDate) {
        this.boughtDate = boughtDate;
    }
}
