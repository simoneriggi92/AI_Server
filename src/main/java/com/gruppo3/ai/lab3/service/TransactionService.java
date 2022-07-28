package com.gruppo3.ai.lab3.service;

import com.gruppo3.ai.lab3.model.*;

import java.util.List;

public interface TransactionService {

    List<TransactionEntity> getAllTransaction();

    List<TransactionEntity> getUserTransactions(String subject);

    //List<PositionEntity> buyPositions(List<PositionEntry> entries, Long minTime, Long maxTime, String subject);

    List<Archive> buyArchives(List<Archive> archiveList, String subject);
}
