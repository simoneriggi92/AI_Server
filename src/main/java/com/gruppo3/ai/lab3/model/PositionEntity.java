package com.gruppo3.ai.lab3.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "positions")
public class PositionEntity {

    @Id
//    @JsonIgnore
    @JsonIgnore
    private String id;
    @JsonIgnore
    private String subject;


    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint position;
    private Long timestamp;
    //id archivio
    @JsonIgnore
    private String archiveId = null;
    @Transient
    private String subject_id;
    @Transient
    private String alias;

    public PositionEntity(){}

    public PositionEntity(String subject, GeoJsonPoint position, Long timestamp) {
        this.subject = subject;
        this.position = position;
        this.timestamp = timestamp;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public GeoJsonPoint getposition() {
        return position;
    }

    public void setposition(GeoJsonPoint position) {
        this.position = position;
    }

    public String getArchiveId() {
        return archiveId;
    }

    public void setArchiveId(String archiveId) {
        this.archiveId = archiveId;
    }

    public String getSubject_id() {
        return subject_id;
    }

    public void setSubject_id(String subject_id) {
        this.subject_id = subject_id;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
