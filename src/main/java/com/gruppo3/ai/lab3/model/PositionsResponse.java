package com.gruppo3.ai.lab3.model;

import java.util.List;

public class PositionsResponse {
    private String subject_id;
    private List<ApproximatedPositionCoordinatesEntity> approxList;
    private List<ApproximatedPositionTimestampEntity> timelineList;

    public PositionsResponse(String subject_id, List<ApproximatedPositionCoordinatesEntity> approxList, List<ApproximatedPositionTimestampEntity> timelineList) {
        this.subject_id = subject_id;
        this.approxList = approxList;
        this.timelineList = timelineList;
    }

    public String getSubject_id() {
        return subject_id;
    }

    public void setSubject_id(String subject_id) {
        this.subject_id = subject_id;
    }

    public List<ApproximatedPositionCoordinatesEntity> getApproxList() {
        return approxList;
    }

    public void setApproxList(List<ApproximatedPositionCoordinatesEntity> approxList) {
        this.approxList = approxList;
    }

    public List<ApproximatedPositionTimestampEntity> getTimelineList() {
        return timelineList;
    }

    public void setTimelineList(List<ApproximatedPositionTimestampEntity> timelineList) {
        this.timelineList = timelineList;
    }
}
