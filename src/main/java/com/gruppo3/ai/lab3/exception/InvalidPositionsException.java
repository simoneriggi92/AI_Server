package com.gruppo3.ai.lab3.exception;

import com.gruppo3.ai.lab3.model.ResponseContainer;

import java.util.List;

public class InvalidPositionsException extends RuntimeException {

    private List<ResponseContainer> responseContainer;

    public InvalidPositionsException(List<ResponseContainer> responseContainer) {
        this.responseContainer = responseContainer;
    }

    public List<ResponseContainer> getResponseContainer() {
        return responseContainer;
    }
}
