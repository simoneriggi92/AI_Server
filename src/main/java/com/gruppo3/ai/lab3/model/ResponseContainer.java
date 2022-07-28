package com.gruppo3.ai.lab3.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ResponseContainer {

    private boolean allPositionValid;
    private LinkedList<InvalidPositionEntity> invalidList;
    private List<String> archive_id;

    public ResponseContainer() {

        this.invalidList = new LinkedList<>();
        this.allPositionValid = true;
        this.archive_id = new LinkedList<>();

    }


    public boolean isAllPositionValid() {
        return allPositionValid;
    }

    public void setAllPositionValid(boolean allPositionValid) {
        this.allPositionValid = allPositionValid;
    }

    public LinkedList<InvalidPositionEntity> getInvalidList() {
        return invalidList;
    }

    public void setInvalidList(LinkedList<InvalidPositionEntity> invalidList) {
        this.invalidList = invalidList;
    }

    public List<String> getArchiveIdList() {
        return archive_id;
    }

    public void setArchive_id(List<String> archive_id) {
        this.archive_id = archive_id;
    }
}
