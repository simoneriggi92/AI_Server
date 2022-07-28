package com.gruppo3.ai.lab3.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "blacklist")
public class TokenBlackList {

    @Id
    private String jti;
    private Date expires;

    public TokenBlackList(String jti, Date expires) {
        this.jti = jti;
        this.expires = expires;
    }

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }
}
