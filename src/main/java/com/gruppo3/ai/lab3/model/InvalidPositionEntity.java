package com.gruppo3.ai.lab3.model;

import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

public class InvalidPositionEntity extends PositionEntity {

    private String description;
    //campo che identifica id_archivio salvato nonostante alcune posizioni non valide

    public InvalidPositionEntity(PositionEntity position, String description) {
        super(position.getSubject(), position.getposition(), position.getTimestamp());
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}