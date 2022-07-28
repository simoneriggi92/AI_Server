package com.gruppo3.ai.lab3.controller;

import com.gruppo3.ai.lab3.model.*;
import com.gruppo3.ai.lab3.service.ArchiveService;
import com.gruppo3.ai.lab3.service.TokenBlackListService;
import com.gruppo3.ai.lab3.service.TransactionService;
import com.gruppo3.ai.lab3.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@Validated
public class RestApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestApiController.class);

    private static final String OWNER = "authentication.name == #username";
    private static final String ADMIN = "hasAuthority('ADMIN')";
    private static final String USER = "hasAuthority('USER')";
    private static final String CUSTOMER = "hasAuthority('CUSTOMER')";

    private final UserService userService;
    private final TransactionService transactionService;
    private final TokenBlackListService tokenBlackListService;
    private final ArchiveService archiveService;

    @Autowired
    public RestApiController(UserService userService, ArchiveService archiveService, TransactionService transactionService, TokenBlackListService tokenBlackListService) {
        this.userService = userService;
        this.archiveService = archiveService;
        this.transactionService = transactionService;
        this.tokenBlackListService = tokenBlackListService;
    }

    // tutti gli utenti
    @PreAuthorize(ADMIN)
    @GetMapping("/api/users")
    public List<User> getUsers() {
        LOGGER.info("Get all users");
        return userService.getAllUsers();
    }

    // id di tutti gli utenti da visualizzare nella lista del client
    @PreAuthorize(CUSTOMER)
    @GetMapping("/api/users_id")
    public List<String> getUsersIdList() {
        return userService.getAllUsersId();
    }

    // singolo utente
    @PreAuthorize(ADMIN + " or " + (OWNER + " and " + USER))
    @GetMapping("/api/users/{username}")
    public User getUser(@PathVariable String username) {
        LOGGER.info("Get single user: {}", username);
        return userService.getUser(username);
    }

    // singolo utente per check uguaglianza username
    @GetMapping(value = "enrollment/isUsernameUnique", params = {"username"})
    public User isEmailUnique(@RequestParam("username") String username) {
        LOGGER.info("Get user to check if is unique: {}", username);
        return userService.getUser(username);
    }


    // tutte le posizioni "approssimate" di tutti gli utenti in un intervallo di temppo
    // username avrà valore "all"
    @PreAuthorize(USER + " or " + ADMIN)
    @PostMapping(value = "/api/users/{username}/positions", params = {"mintime", "maxtime"})
    public PositionsResponseContainer getAllPositionsTimestampBetween(
            @PathVariable String username,
            @NotNull(message = "mintime must not be null") @RequestParam("mintime") Long minTime,
            @NotNull(message = "maxtime must not be null") @RequestParam("maxtime") Long maxTime,
            @RequestBody List<PositionEntry> entries) {
        LOGGER.info("Get all approximated positions of user: {}", username);
        return archiveService.getApproximatedPositonsByTimestampAndPolygon(entries, minTime, maxTime, username);
    }


    // API per ottenere le posizioni approssimate, archivi, timeline degli utenti specificati da client
    // quando l'utente vuole comprare le posizioni degli utenti selzionati
    @PreAuthorize(USER + " or " + ADMIN)
    @PostMapping(value = "/api/users/positions/area", params = {"mintime", "maxtime"})
    public PositionsResponseContainer getPositionsByTimestampPolygonUsers(
            @NotNull(message = "mintime must not be null") @RequestParam("mintime") Long minTime,
            @NotNull(message = "maxtime must not be null") @RequestParam("maxtime") Long maxTime,
            @RequestBody BuyRequest buyRequest) {

        LOGGER.info("Get area");
        PositionsResponseContainer pRC = archiveService.getAppximatedPositionsByTimestampPolygonUsers(buyRequest.getPolygonPoints(), minTime, maxTime, buyRequest.getUsersList());
        return pRC;
    }


    // post di un archivio con sequenze di posizioni di un utente
    @PreAuthorize(OWNER)
    @PostMapping("/api/users/{username}/archives")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ResponseContainer> addArchive(@PathVariable String username, @RequestBody List<PositionEntry> entries) {
        LOGGER.info("Post archives for user: {}", username);
        return archiveService.addArchive(username, entries);
    }

    // Get di tutti gli archivi caricati dall'utente specificato
    @PreAuthorize(OWNER)
    @GetMapping("/api/users/{username}/archives")
    public List<Archive> getArchives(@PathVariable String username) {
        LOGGER.info("Get all archives of user: {}", username);
        return archiveService.getArchives(username);
    }

    // Get degli archivi acquistati dall'utente specificato
    @PreAuthorize(CUSTOMER)
    @GetMapping("/api/customers/{username}/archives")
    public List<BoughtArchive> getBoughtArchives(@PathVariable String username) {
        LOGGER.info("Get all archives of user: {}", username);
        return archiveService.getBoughtArchives(username);
    }


    @PreAuthorize(OWNER)
    @PostMapping("/api/users/{username}/delete/archives/{archive_id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteArchive(@PathVariable String username, @PathVariable String archive_id) {
        LOGGER.info("Delete archive {} of user: {}", archive_id, username);
        archiveService.deleteArchive(archive_id, username);
    }

    @PreAuthorize(OWNER)
    @PostMapping("/api/users/{username}/delete/archives/all")
    @ResponseStatus(HttpStatus.OK)
    public void deleteArchive(@PathVariable String username) {
        LOGGER.info("Delete all archives of user: {}", username);
        archiveService.deleteAllArchives(username);
    }

    // acquisto gli archivi selezionati
    @PreAuthorize(CUSTOMER + "&&" + OWNER)
    @PostMapping("/api/customers/{username}/archives/area/buy")
    @ResponseStatus(HttpStatus.OK)
    public List<Archive> buyArchives(@PathVariable String username, @RequestBody List<Archive> archiveList) {
        LOGGER.info("Buy all selected archives of user: {}", username);
        return transactionService.buyArchives(archiveList, username);
    }


    // download soltanto di un archivio caricato dall'utente specificato
    @PreAuthorize(OWNER)
    @GetMapping("/api/users/{username}/archives/{archive_id}")
    public Archive getArchive(@PathVariable String username, @PathVariable String archive_id) {
        LOGGER.info("Download of uploaded archive {} of user {}", archive_id, username);
        return archiveService.getArchive(archive_id, username);
    }


    // download degli archivi acquistati dall'utente
    @PreAuthorize(USER + " or " + CUSTOMER)
    @GetMapping("/api/users/{username}/archivesbought/{archive_id}")
    public Archive getBoughtArchive(@PathVariable String username, @PathVariable String archive_id) {
        LOGGER.info("Download bought archive {} of user: {}", archive_id, username);
        return archiveService.getBoughtArchive(archive_id, username);
    }


    // get di tutte le transazioni
    @PreAuthorize(ADMIN)
    @GetMapping("/api/transactions")
    public List<TransactionEntity> getAllTransaction() {
        LOGGER.info("Get all transactions");
        return transactionService.getAllTransaction();
    }

    // get di un singolo customer
    @PreAuthorize(ADMIN + " or " + (OWNER + " and " + CUSTOMER))
    @GetMapping("/api/customers/{username}")
    public User getCustomer(@PathVariable String username) {
        LOGGER.info("Get customer: {}", username);
        return userService.getUser(username);
    }

    // get di tutte le transazioni di un customer
    @PreAuthorize(ADMIN + " or " + (OWNER + " and " + CUSTOMER))
    @GetMapping("/api/customers/{username}/transactions")
    public List<TransactionEntity> getUserTransactions(@PathVariable String username) {
        LOGGER.info("Get all transaction of user: {}", username);
        return transactionService.getUserTransactions(username);
    }

    // logout endpoint
    @PostMapping("/api/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout() {
        LOGGER.info("User logged out");
        tokenBlackListService.addToBlackList();
    }

    // registrazione
    @PostMapping("/enrollment")
    @ResponseStatus(HttpStatus.OK)
    public User enroll(@Valid @RequestBody CredentialEntry credentials) {
        LOGGER.info("Enrollment of User: {}", credentials.getUsername());
        User user = userService.saveUser(credentials.getUsername(), credentials.getPassword(), credentials.getConfirmPassword());
        LOGGER.info("User enrolled: {}", credentials.getUsername());
        return user;
    }

    @PutMapping("/api/updateEnrollment")
    @ResponseStatus(HttpStatus.OK)
    public User updateUser(@Valid @RequestBody UpdateCredentialEntry credentials) {
        LOGGER.info("Updating password for user: {}", credentials.getUsername());
        User existingUser = userService.getUser(credentials.getUsername());
        return userService.updateUser(existingUser, credentials.getOldPassword(), credentials.getPassword(), credentials.getConfirmNewPassword());

    }

    @PutMapping("/api/updateWallet")
    @ResponseStatus(HttpStatus.OK)
    public User updateWallet(@NotNull @RequestBody User currentUser) {
        LOGGER.info("Updating wallet for user: {}", currentUser.getUsername());
        return userService.updateWallet(currentUser.getAmount());
    }
}
