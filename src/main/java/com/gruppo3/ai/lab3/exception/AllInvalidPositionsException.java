package com.gruppo3.ai.lab3.exception;

import com.gruppo3.ai.lab3.model.ResponseContainer;

import java.util.List;

public class AllInvalidPositionsException extends RuntimeException {

    private List<ResponseContainer> responseContainer;

    public AllInvalidPositionsException(List<ResponseContainer> responseContainer) {
        this.responseContainer = responseContainer;
    }

    public List<ResponseContainer> getInvalidContainerList(){
        return responseContainer;
    }
}
