package com.gruppo3.ai.lab3.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "transactions")
public class TransactionEntity {

    private String sender;
    private String receiver;
    private int amount;
    private Long timestamp;

    public TransactionEntity(String sender, String receiver, int amount, Long timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}