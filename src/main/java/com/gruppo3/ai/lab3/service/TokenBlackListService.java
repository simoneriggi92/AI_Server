package com.gruppo3.ai.lab3.service;

import com.gruppo3.ai.lab3.model.TokenBlackList;
import com.gruppo3.ai.lab3.repository.TokenBlackListRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class TokenBlackListService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenBlackListService.class);

    private final TokenBlackListRepository tokenBlackListRepo;
    private final TokenStore tokenStore;

    @Autowired
    public TokenBlackListService(TokenBlackListRepository tokenBlackListRepo, TokenStore tokenStore) {
        this.tokenBlackListRepo = tokenBlackListRepo;
        this.tokenStore = tokenStore;
    }

    public void addToBlackList() {

        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) authentication.getDetails();
        String jti = tokenStore.readAccessToken(details.getTokenValue()).getAdditionalInformation().get("jti").toString();
        Date expire = tokenStore.readAccessToken(details.getTokenValue()).getExpiration();
        TokenBlackList tokenBlackList = new TokenBlackList(jti, expire);
        tokenBlackListRepo.save(tokenBlackList);
    }

    public int getFromBlackList() {
        int blackToken = 0;
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            // nessun token presentato -> permetti la registrazione ma non permettere di accedere alle risorse api
            return -1;
        }
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) authentication.getDetails();
        String jti = tokenStore.readAccessToken(details.getTokenValue()).getAdditionalInformation().get("jti").toString();
        if (tokenBlackListRepo.findByJti(jti) == null) {
            // token presentato ancora naturalmente valido -> non permettere registrazione ma permetti di accedere alle risorse api
            blackToken = 1;
        } else {
            // token presentato blackato -> permetti la registrazione ma non permettere di accedere alle risorse api
            blackToken = 0;
        }
        return blackToken;
    }

    @Async
    public void checkBlackTokensValidity() {
        int i = 0;
        while (true) {
            // System.out.println(Thread.currentThread().getName() + "[" + i++ + "] " + "Controllo validità temporale dei token nella blacklist...");
            List<TokenBlackList> blackTokens = tokenBlackListRepo.findAll();
            if (blackTokens == null || blackTokens.isEmpty()) {
                // System.out.println(Thread.currentThread().getName() + "[" + i++ + "] " + "La lista è vuota!");
            } else {
                for (TokenBlackList blackToken : blackTokens) {
                    if (blackToken.getExpires().before(new Date())) {
                        // rimuovi il black token
                        tokenBlackListRepo.delete(blackToken);
                        LOGGER.info(Thread.currentThread().getName() + "[" + i++ + "] " + "Rimosso token (jti: " + blackToken.getJti() + ")");
                        // System.out.println(Thread.currentThread().getName() + "[" + i++ + "] " + "Rimosso token (jti: " + blackToken.getJti() + ")");
                    }
                }
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
        }
    }
}


