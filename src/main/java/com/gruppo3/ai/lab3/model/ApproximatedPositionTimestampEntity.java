package com.gruppo3.ai.lab3.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Document(collection = "approximated_timestamp")
public class ApproximatedPositionTimestampEntity {

    @JsonIgnore
    @Id
    private String id;
    private Long timestamp;
    @JsonIgnore
    private List<String> precise_positions_list;
    // @JsonIgnore
    private String my_precise_position_id;
    private String subject_id;
    private String alias;



    @JsonIgnore
    private String color;

    @JsonIgnore
    private boolean toAdd;

    public ApproximatedPositionTimestampEntity(){
        this.precise_positions_list = new ArrayList<>();
        toAdd = false;
    }

    public ApproximatedPositionTimestampEntity(PositionEntity positionEntity) {
        this.my_precise_position_id = positionEntity.getId();
        this.precise_positions_list = new ArrayList<>();
        this.timestamp = setApproximatedTimestamp(positionEntity.getTimestamp());

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getTimestamp() {
        return timestamp;
    }


    public List<String> getPrecise_positions_list() {
        return precise_positions_list;
    }

    public void setPrecise_positions_list(List<String> precise_positions_id) {
        this.precise_positions_list = precise_positions_id;
    }

    public boolean isToAdd() {
        return toAdd;
    }

    public void setToAdd(boolean toAdd) {
        this.toAdd = toAdd;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    //meotodo per approssimare timestamp al minuto
    public Long setApproximatedTimestamp(Long timestamp) {
        long approximated_timestamp = 0;
        Date date = new Date(timestamp * 1000L);
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(date);
//            if (cal.get(Calendar.SECOND) < 30) {
                //arrotondo per difetto
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
//            } else {
//                //arrotondo per eccesso
//                cal.set(Calendar.MINUTE, Calendar.MINUTE + 1);
//                cal.set(Calendar.SECOND, 0);
//                cal.set(Calendar.MILLISECOND, 0);
//            }
            Date date1 = cal.getTime();
            approximated_timestamp = cal.getTimeInMillis()/1000L;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return approximated_timestamp;
    }

    public void setTimestamp (long approximatedTimestamp){
        this.timestamp = approximatedTimestamp;
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

    @Override
    public String toString() {
        return "ApproximatedPositionTimestampEntity{" +
                "id='" + id + '\'' +
                ", timestamp=" + timestamp +
                ", precise_positions_list=" + precise_positions_list +
                ", my_precise_position_id='" + my_precise_position_id + '\'' +
                '}';
    }
}
