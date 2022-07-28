package com.gruppo3.ai.lab3.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "approximated_coordinates")
public class ApproximatedPositionCoordinatesEntity {

    @JsonIgnore
    @Id
    private String id;
    //private String subject;
    //private String archive_id;
    //private String archive_id;
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    @JsonDeserialize(using = GeoJsonDeserializer.class)
    private GeoJsonPoint position;
    @JsonIgnore
    private List<String> precise_positions_list;
    // @JsonIgnore
    private String my_precise_position_id;
    @Transient
    private String subject_id;
    @Transient
    private String alias;



    @JsonIgnore
    private String color;

    public ApproximatedPositionCoordinatesEntity() {

    }

    public ApproximatedPositionCoordinatesEntity(PositionEntity positionEntity) {
        //this.subject = positionEntity.getSubject();
        //this.archive_id = positionEntity.getArcihve_id();
        this.my_precise_position_id = positionEntity.getId();
        this.position = setApproximatedCoordinates(positionEntity.getposition());
        this.precise_positions_list = new ArrayList<>();
        this.subject_id = positionEntity.getSubject_id();
        this.alias = positionEntity.getAlias();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public GeoJsonPoint getPosition() {
        return position;
    }


    public List<String> getPrecise_positions_list() {
        return precise_positions_list;
    }

    public void setPrecise_positions_id(List<String> precise_positions_id) {
        this.precise_positions_list = precise_positions_id;
    }


    //meotodo per approssimare long e lat alla seconda cifra decimale
    public GeoJsonPoint setApproximatedCoordinates(GeoJsonPoint position) {

        GeoJsonPoint approximated_position = null;
        DecimalFormat df = new DecimalFormat("#.##");
        try {
            df.setRoundingMode(RoundingMode.CEILING);

            approximated_position = new GeoJsonPoint(
                    Double.parseDouble(df.format(position.getX()).replace(",", ".")),
                    Double.parseDouble(df.format(position.getY()).replace(",", "."))
            );
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return approximated_position;
    }


    public String getMy_precise_position_id() {
        return my_precise_position_id;
    }

    public void setMy_precise_position_id(String my_precise_position_id) {
        this.my_precise_position_id = my_precise_position_id;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "ApproximatedPositionCoordinatesEntity{" +
                "id='" + id + '\'' +
                ", position=" + position +
                ", precise_positions_list=" + precise_positions_list +
                ", my_precise_position_id='" + my_precise_position_id + '\'' +
                '}';
    }
}
