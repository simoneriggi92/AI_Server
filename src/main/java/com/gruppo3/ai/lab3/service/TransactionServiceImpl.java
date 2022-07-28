package com.gruppo3.ai.lab3.service;

import com.gruppo3.ai.lab3.exception.ArchiveAlreadyOwnedException;
import com.gruppo3.ai.lab3.exception.ArchiveNotFoundException;
import com.gruppo3.ai.lab3.exception.NotEnoughMoneyException;
import com.gruppo3.ai.lab3.exception.NotValidPolygonPointsException;
import com.gruppo3.ai.lab3.model.*;
import com.gruppo3.ai.lab3.repository.ArchiveRepository;
import com.gruppo3.ai.lab3.repository.PositionRepository;
import com.gruppo3.ai.lab3.repository.TransactionRepository;
import com.gruppo3.ai.lab3.repository.UserRepository;
import com.gruppo3.ai.lab3.utils.Encryption;
import com.gruppo3.ai.lab3.utils.PositionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.data.geo.Polygon;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final UserRepository userRepository;
    private final PositionRepository positionRepository;
    private final TransactionRepository transactionRepository;
    private final PositionService positionService;
    private ArchiveRepository archiveRepository;
    private PositionUtils utilities;

    @Autowired
    public TransactionServiceImpl(PositionUtils utilities, ArchiveRepository archiveRepository, TransactionRepository transactionRepository, UserRepository userRepository, PositionRepository positionRepository, PositionService positionService) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.positionRepository = positionRepository;
        this.positionService = positionService;
        this.archiveRepository = archiveRepository;
        this.utilities = utilities;
    }

    @Override
    public List<TransactionEntity> getAllTransaction() {
        return transactionRepository.findAll();
    }

    @Override
    public List<TransactionEntity> getUserTransactions(String username) {
        return transactionRepository.findTransactionEntitiesBySender(username);
    }

//    @Override
//    public List<PositionEntity> buyPositions(List<PositionEntry> entries, Long minTime, Long maxTime, String subject) {
//        int amount = 0;
//        int price = 1;  //Per il momento costo unitario
//        System.out.println("DENTRO ");
//        List<TransactionEntity> transactionEntities = new ArrayList<>();
//        List<PositionEntity> list;
//        List<String> listNoUserDuplication = new ArrayList<>();
//        List<Point> points = new ArrayList<>();
//
//        for (PositionEntry location : entries) {
//            Point locationPoint = new Point(
//                    location.getLongitude(),
//                    location.getLatitude());
//            points.add(locationPoint);
//        }
//
//        Polygon polygon = new Polygon(points);
//        if (!polygon.getPoints().isEmpty()) {
//            List<String> listUsername = positionService.getUsernameList(polygon, minTime, maxTime);
//            list = positionRepository.findByPositionWithinAndTimestampBetween(polygon, minTime, maxTime);
//            if (listUsername != null && !listUsername.isEmpty()) {
//                for (String temp : listUsername) {
//                    amount = Collections.frequency(listUsername, temp) * price;
//                    if (!listNoUserDuplication.contains(temp)) {
//                        listNoUserDuplication.add(temp);
//                        System.out.println(temp + ": " + amount);
//                        transactionEntities.add(new TransactionEntity(subject,
//                                temp,
//                                amount,
//                                System.currentTimeMillis()));
//
//                        User user = userRepository.findByUsername(temp);
//                        user.setAmount(user.getAmount() + amount);
//                        userRepository.save(user);
//                    }
//                }
//
//                User customer = userRepository.findByUsername(subject);
//
//                if (customer.getAmount() > 0 && customer.getAmount() >= amount) {
//                    customer.setAmount(customer.getAmount() - amount);
//                    userRepository.save(customer);
//                } else
//                    throw new NotEnoughMoneyException();
//
//                transactionRepository.saveAll(transactionEntities);
//                list = Encryption.cryptId(listNoUserDuplication, list);
//                return list;
//            } else
//                return null;
//        }
//        throw new NotValidPolygonPointsException();
//    }

    @Override
    public List<Archive> buyArchives(List<Archive> required_archiveList, String subject) {

        List<BoughtArchive> alreadyBoughtList = new ArrayList<>();
        List<Archive> archiveList = new ArrayList<>();
        List<String> archiveIdList = new ArrayList<>();
        List<Archive> filledArchiveList = new ArrayList<>();
        //mi prendo il customer
        User user = userRepository.findByUsername(subject);

        //devo controllare che l'utente non abbia già quell'archivio
        //prendo la lista di tutti gli archivi già comprati dall'utente
        alreadyBoughtList = user.getBoughtArchiveList();
        //per tutti gli archivi richiesti dal client
        for (int i = 0; i < alreadyBoughtList.size(); i++) {
            String current_id = alreadyBoughtList.get(i).getArchive_id();
            //se trovo un archivio con id = a quelli i-esimo richiesto dall'utente
            Archive findedArchive = required_archiveList.stream()
                    .filter(archive -> archive.getId().equals(current_id))
                    .findAny()
                    .orElse(null);
            //se ho trovato un archvio già posseduto, lo rimuovo dalla lista di quelli richiesti
            if (findedArchive != null) {
                required_archiveList.remove(findedArchive);
            }

            //se sono giù tutti posseduti dall'utente, ritorno un'eccezione
            if (required_archiveList.size() == 0) {
                System.out.println("ARCHIVI GIA' TUTTI POSSEDUTI");
                throw new ArchiveAlreadyOwnedException();
            }
        }

        //controllo subito che l'utente abbia il credito necessario
        double tot = 0;
        for (int i = 0; i < required_archiveList.size(); i++)
            tot += required_archiveList.get(i).getPrice();
        //non ha abbastanza denaro
        if (user.getAmount() < tot)
            throw new NotEnoughMoneyException();

        required_archiveList.forEach(a -> archiveIdList.add(a.getId()));
        //recupero gli archivi in base al loro id
        //recupero le posizioni esatte per ogni lista degli id delle posizioni esatte
        Iterable<Archive> saved_archiveList = archiveRepository.findAllById(archiveIdList);

        if (!saved_archiveList.iterator().hasNext())
            throw new ArchiveNotFoundException(required_archiveList.toString());

        for (Archive a : saved_archiveList)
            archiveList.add(a);

        //dalla lista archivi recupero posizioni esatte
        //ritorno all'utente stessi archivi ma con positionsList piena
        for (int i = 0; i < archiveList.size(); i++) {
            Archive current_archive = archiveList.get(i);
            Archive modified_archive = new Archive();
            modified_archive.setId(current_archive.getId());
            modified_archive.setUploadTime(current_archive.getUploadTime());

            //incremento contatore numero_vendite
            long n_sold = current_archive.getN_sold() + 1;
            modified_archive.setN_sold(n_sold);
            modified_archive.setAliasID(current_archive.getAliasID());

            //incremento credito utente dell'utente
            User owner = userRepository.findByUsername(current_archive.getOwner());
            owner.setAmount(owner.getAmount() + 0.7);
            userRepository.save(owner);

            //aggiorno numero vendite nel db
            current_archive.setN_sold(n_sold);
            archiveRepository.save(current_archive);

            //trovo lista PositionEntity e le metto nell archivio
            List<PositionEntity> precisionList = positionRepository.findAllByArchiveId(current_archive.getId());

            if (!precisionList.isEmpty()) {
                modified_archive.setPositions_list(precisionList);
            }

            filledArchiveList.add(modified_archive);
            //creo archvio con lista poszioni
            BoughtArchive boughtArchive = new BoughtArchive(modified_archive);
            //salvo la data di acquisto
            boughtArchive.setBoughtDate(new Date(System.currentTimeMillis()));
            //salvo l'archivio acquistato nella lista utente degli archivi acquistati
            user.getBoughtArchiveList().add(boughtArchive);
            //decremento il suo credito
            user.setAmount(user.getAmount() - 0.7);

            System.out.println("TOT: " + tot);

        }
        //salvo modifiche
        userRepository.save(user);

        System.out.println("Numero archivi comprati: " + filledArchiveList.size());
        filledArchiveList.forEach(a -> System.out.println("Num_posizioni: " + a.getPositions_list().size()));
        return filledArchiveList;
    }

}
