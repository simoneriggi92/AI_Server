package com.gruppo3.ai.lab3.service;

import com.gruppo3.ai.lab3.exception.AllInvalidPositionsException;
import com.gruppo3.ai.lab3.exception.ArchiveNotFoundException;
import com.gruppo3.ai.lab3.exception.PositionsNotFoundException;
import com.gruppo3.ai.lab3.exception.UserNotFoundException;
import com.gruppo3.ai.lab3.model.*;
import com.gruppo3.ai.lab3.repository.*;
import com.gruppo3.ai.lab3.utils.PositionUtils;
import javafx.scene.shape.Arc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Polygon;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ArchiveServiceImpl implements ArchiveService {

    private final ArchiveRepository archiveRepository;
    private final PositionRepository positionRepository;
    private final ApproximatedPositionCoordinatesRepository approxPositionCoordinatesRepository;
    private final ApproximatedPositionTimestampRepository approxPositionTimestampRepository;
    private final UserRepository userRepository;
    private PositionUtils utilities;
    private double single_position_price = 0.7;

    @Autowired
    public ArchiveServiceImpl(ArchiveRepository archiveRepository, PositionRepository positionRepository,
                              ApproximatedPositionCoordinatesRepository approxPositionCoordinatesRepository,
                              ApproximatedPositionTimestampRepository approxPositionTimestampRepository,
                              UserRepository userRepository,
                              PositionUtils utilities) {
        this.archiveRepository = archiveRepository;
        this.positionRepository = positionRepository;
        this.approxPositionCoordinatesRepository = approxPositionCoordinatesRepository;
        this.approxPositionTimestampRepository = approxPositionTimestampRepository;
        this.userRepository = userRepository;
        this.utilities = utilities;
    }

    //meotodo che aggiunge un nuovo archivio
    //crea una rappresentazione approssimativa dei dati
    @Override
    public List<ResponseContainer> addArchive(String username, List<PositionEntry> entries) {

        List<PositionEntity> entities = new ArrayList<>();

        List<ApproximatedPositionCoordinatesEntity> approximated_coordinates = new ArrayList<ApproximatedPositionCoordinatesEntity>();
        List<ApproximatedPositionTimestampEntity> approximated_timestamp = new ArrayList<>();
        List<ResponseContainer> responseContainerList = new ArrayList<>();


        //converto PositionEntry in PositionEntity
        for (PositionEntry location : entries) {
            GeoJsonPoint locationPoint = new GeoJsonPoint(
                    location.getLongitude(),
                    location.getLatitude());
            entities.add(new PositionEntity(username, locationPoint, location.getTimestamp()));
        }

        //controllo che le posizioni appartengano alla stessa settimana
        //altrimenti ho archivi diversi
        List<List<PositionEntity>> archives_list = utilities.separateArchives(entities);
        System.out.println("Numero archivi settimanali: " + archives_list.size());
        //salvo numero di elementi settimanali dentro l'array perchè poi cambia
        List<Integer> week_num_elem = new ArrayList<>();
        archives_list.forEach(list -> week_num_elem.add(list.size()));


        archives_list.forEach(archive -> System.out.println("Num_elementi: " + archive.size()));
        //System.out.println("1) " + entities.get(0).ge);
        for (List<PositionEntity> arch_list : archives_list) {
            entities = arch_list;
            Archive saved_archive = null;
            List<String> positions_id = new ArrayList<>();
            ResponseContainer responseContainer;
            //validazione

            responseContainer = PositionUtils.addCandidate(entities, username, positionRepository);
            System.out.println("Posizioni non  valide: " + responseContainer.getInvalidList().size());

            if (responseContainer == null) {
                System.out.println("Creo nuovo response container");
                responseContainer = new ResponseContainer();
            }
            //se sono tutte non valide, faccio scattare un'eccezione
            if (responseContainer.getInvalidList().size() == entries.size()) {
                responseContainer.setAllPositionValid(true);
                responseContainerList.add(responseContainer);
                throw new AllInvalidPositionsException(responseContainerList);
            }


            //controllo se divise per settimana il size del singolo archivio diviso per settimana
            //weeks_archive_list = 1 , solo un archivio
            // > 1 , più archvi settimanali
            if (archives_list.size() > 1) {
                System.out.println("CASO SPECIALE: " + archives_list.size());
                //prendo la lista della prima settimana
                int n_elem_first_week = week_num_elem.get(0);
                LinkedList<InvalidPositionEntity> invalid_list = responseContainer.getInvalidList();

                System.out.println("FIRST_SETTIMANA_SIZE: " + n_elem_first_week);
                System.out.println("ARC_POSIZIONI_NON VALIDE: " + invalid_list.size());

                //se tutte le posizioni del primo archvio settimanale sono state rifiutate
                //allora anche le altre lo saranno
                if (n_elem_first_week == invalid_list.size()) {
                    responseContainer.setAllPositionValid(true);
                    responseContainerList.add(responseContainer);
                    throw new AllInvalidPositionsException(responseContainerList);
                }
            }


            //for(List<PositionEntity> arch_list: archives_list){
            try {
                //prendo il timestamp inferiore delle posizioni
                PositionEntity minPosition = entities
                        .stream()
                        .min(Comparator.comparing(PositionEntity::getTimestamp))
                        .orElseThrow(NoSuchElementException::new);

                //Prendo il timestamp maggiore delle posizioni

                PositionEntity maxPosition = entities
                        .stream()
                        .max(Comparator.comparing(PositionEntity::getTimestamp))
                        .orElseThrow(NoSuchElementException::new);

                long minTime = minPosition.getTimestamp();
                long maxTime = maxPosition.getTimestamp();
                long uploadTime = System.currentTimeMillis() / 1000L;

                int num_archives = this.archiveRepository.findAll().size() + 1;
                //creo l'oggetto archivio e  aggiungo i campi
                Archive archive = new Archive(username, minTime, maxTime, uploadTime,  "Archive " + num_archives);
                //setto il prezzo
                archive.setPrice(entities.size() * single_position_price);
                archive.setN_positions(entities.size());
                //salvo archive perchè altirmenti non mi ritorna l'id
                saved_archive = archiveRepository.save(archive);
                //aggiungo archive_id , ho sicuro almeno una pos da salvare, altrimenti sarebbe scattato NoSuchElementException
                responseContainer.getArchiveIdList().add(saved_archive.getId());
                responseContainerList.add(responseContainer);
                //aggiorno l'id_archivio delle posizioni con quelle dell'archivio appena creato
                //Archive saved_archive = archive;
                //temp_archive variabile temp che mi serve per settare id archivio di tutte le posizioni
                Archive temp_archive = saved_archive;
                entities.forEach(p -> p.setArchiveId(temp_archive.getId()));

                //salvo le posizioni sulla repo, altrimenti id=null
                entities = positionRepository.saveAll(entities);

                System.out.println("Num_posizioni_accettate: " + entities.size());
                //riempio lista id_posizioni di appoggio dell'archivio
                entities.forEach(p -> positions_id.add(p.getId()));
                //aggiungo lista id posizioni all'archivio
                archive.setPositionsIdList(positions_id);
                //devo fare update dell'archivio
                archiveRepository.save(archive);
                System.out.println("SIZE_ResponseContainer: " + responseContainerList.size());

                //riempio la lista delle posizioni approssimate per coordinate
                entities.forEach(p -> approximated_coordinates.add(new ApproximatedPositionCoordinatesEntity(p)));

                //riempio la lista delle posizioni approssimate per timestamp
                entities.forEach(p -> approximated_timestamp.add(new ApproximatedPositionTimestampEntity(p)));

                //ordino lista delle posizioni approssimate per coordinate
                Collections.sort(approximated_coordinates, new Comparator<ApproximatedPositionCoordinatesEntity>() {
                    @Override
                    public int compare(ApproximatedPositionCoordinatesEntity o1, ApproximatedPositionCoordinatesEntity o2) {
                        if (o1.getPosition().getX() == o2.getPosition().getX() && o1.getPosition().getY() == o2.getPosition().getY())
                            return 0;
                        return o1.getPosition().getX() < o2.getPosition().getX() && o1.getPosition().getY() < o2.getPosition().getY() ? -1 : 1;
                    }
                });

                //ordina la lista delle posizioni approssimate per timestamp
                Collections.sort(approximated_timestamp, new Comparator<ApproximatedPositionTimestampEntity>() {
                    @Override
                    public int compare(ApproximatedPositionTimestampEntity o1, ApproximatedPositionTimestampEntity o2) {
                        if (o1.getTimestamp().equals(o2.getTimestamp()))
                            return 0;
                        return o1.getTimestamp() < o2.getTimestamp() ? -1 : 1;
                    }
                });

                //elimino i duplicati in base a X, Y e salvo loro id di riferimento alla lista
                List<ApproximatedPositionCoordinatesEntity> noduplicate_coordinates_list;
                noduplicate_coordinates_list = utilities.findDuplicateCoordinates(approximated_coordinates);

                List<ApproximatedPositionTimestampEntity> noduplicate_timestamp_list;
                noduplicate_timestamp_list = utilities.findDuplicateTimestamp(approximated_timestamp);

                //salvo la lista posizioni approssimate per coordinate nel db
                approxPositionCoordinatesRepository.saveAll(noduplicate_coordinates_list);

                //salvo la lista delle posizioni approssimate per timestamp nel db
                approxPositionTimestampRepository.saveAll(noduplicate_timestamp_list);

            } catch (NoSuchElementException ex) {
                //non è stata trovato un timestamp min/max
                //e tutte le posizioni nn sono valide
                //continuo
                continue;
            }
        }

        return responseContainerList;
    }

    @Override
    public void deleteArchive(String archive_id, String username) {
        //1) recuperare l'archivio, con le relative posizioni
        //2) cancellare le posizioni dalla collezione "positions"
        //3) eliminare l'id dalle rappresentazioni approssimate
        //3.1) Se il record rimane con una lista vuota di id, eliminare tutto il record
        //4) cancellare l'archivio dalla collezione "archives"
        Archive saved_archive = archiveRepository.findByIdAndOwner(archive_id, username);
        //se non trovo nessun archivio
        if (saved_archive == null)
            throw new ArchiveNotFoundException(archive_id);

        //2) elimino gli le posizioni scorrendo la lista di Id dell'archivio
        saved_archive.getPositionsIdList().forEach(p -> positionRepository.deleteById(p.toString()));

        //3)//recupero tutte le posizioni approssimate per coord e timestamp
        List<ApproximatedPositionCoordinatesEntity> approx_coordinates = approxPositionCoordinatesRepository.findAll();
        List<ApproximatedPositionTimestampEntity> approx_timestamp = approxPositionTimestampRepository.findAll();

        //elimino ida da posiz approx per x, y
        //Per tutti gli id_posizioni dell'archvio
        List<String> id_to_remove = saved_archive.getPositionsIdList();
        approx_coordinates = utilities.removePositionIdCoord(approx_coordinates, id_to_remove);
        approx_timestamp = utilities.removePositionIdTime(approx_timestamp, id_to_remove);


        //salvo le collezioni approx modificate
        //se contengono ancora delle approx con almeno un id
        if (!approx_coordinates.isEmpty())
            approxPositionCoordinatesRepository.saveAll(approx_coordinates);
        if (!approx_timestamp.isEmpty())
            approxPositionTimestampRepository.saveAll(approx_timestamp);

        //4)cancello record archvio dalla collezione "archives"
        archiveRepository.delete(saved_archive);
    }


    public void deleteAllArchives(String username){
        List<Archive> archiveList = this.archiveRepository.findAllByOwner(username);

        if(archiveList.size() == 0)
            throw new ArchiveNotFoundException(null);

        this.archiveRepository.deleteAllByOwner(username);
    }

    @Override
    public Archive getArchive(String archive_id, String username) {
        //1) recuoero l'archivio
        //2) recupero posizioni archivio dalla tabella posizioni
        //3) riempio lista posizioni dell'archivio
        //4) ritorno archivio

        //1) recupero archvio
        Archive saved_archive = archiveRepository.findByIdAndOwner(archive_id, username);
        //se non trovo nessun archivio
        if (saved_archive == null)
            throw new ArchiveNotFoundException(archive_id);

        //2- 3) recupero posizioni e le metto nell'archivio
        saved_archive.setPositions_list(positionRepository.findAllByArchiveId(archive_id));

        return saved_archive;
    }


    //1-Client invia di default POLIGONO E TIMESTAMP
    //2-User seleziona POLIGONO e TIMESTAMP
    @Override
    public PositionsResponseContainer getApproximatedPositonsByTimestampAndPolygon(List<PositionEntry> polygon_entries, Long minTime, Long maxTime, String subject) {
        //1)recupero posizioni approssimate per timestamp
        // 2)recupero posizioni esatte da "positions" e le converto in ApproximatedPositionCoordinates
        List<PositionEntity> precisePositionsList = null;
        PositionsResponseContainer responseContainer = new PositionsResponseContainer();
        List<List<PositionEntity>> separatedPositionsList;
        List<List<ApproximatedPositionCoordinatesEntity>> responseList = null;
        List<ApproximatedPositionTimestampEntity> timelineList = null;
        //1)

        //converto lista in poligono
        Polygon polygon = utilities.convertToPolygon(polygon_entries);

        //recupero lista posizioni dentro il poligono indicato
        List<ApproximatedPositionCoordinatesEntity> insidePoligonPositionsList = approxPositionCoordinatesRepository.findByPositionWithin(polygon);

        //Recupero posizioni dentro il timestamp indicato
        List<ApproximatedPositionTimestampEntity> timestampPositionsList = approxPositionTimestampRepository.findByTimestampBetween(minTime, maxTime);

        //per ogni id della lista_id di ogni approx_timestamp
        //controllo se id presente in una delle liste_id di ogni approx_coord
        //se si, devo creare una nuova timestampPositionsList con l'oogetto approx, e aggiungo l'id alla lista_id

        //sostituisco con il risultato finale della lista
        timestampPositionsList = utilities.joinTables(timestampPositionsList, insidePoligonPositionsList);

        //trovo tutte le posizioni "precise" aventi id uguale a quello
        //contenuto nella lista di "id" delle poszioni approssimate e setto il subject_id
        precisePositionsList = utilities.findPrecisePositionsById(timestampPositionsList, responseContainer, subject);


        //non ho trovato nessuna posizione
        if (precisePositionsList.isEmpty())
            throw new PositionsNotFoundException(minTime, maxTime);


        //ottengo la lista delle lista separate per utente
        separatedPositionsList = utilities.getSeparatedPositionsBySubject(precisePositionsList);

        //ordino per alias
        separatedPositionsList = sortPositionsByAlias(separatedPositionsList);

        //rimuovo la mia lista, se c'è
        separatedPositionsList = removeMyPositionList(separatedPositionsList);

        if (separatedPositionsList.isEmpty())
            throw new PositionsNotFoundException(minTime, maxTime);

        //converto la lista in ApproxCoordinatesEntity
        responseList = utilities.convertList(separatedPositionsList);

        //creo l'oggetto di risposta da dare al client
        responseContainer.setApproxPositionsList(responseList);

        //aggiungo timelineList
        timelineList = utilities.createTimelineListBySubject(timestampPositionsList, separatedPositionsList);
        responseContainer.setTimelineList(timelineList);
        //setto il numero di utenti a cui appartengono le posizioni
        responseContainer.setN_users(responseContainer.getApproxPositionsList().size());

        //elimino il mio archivio dalla lista
        responseContainer.setArchiveList(removeMyArchive(responseContainer.getArchiveList()));

        //setto il totale degli archivi
        responseContainer.getArchiveList().forEach(archive -> {
            responseContainer.setTotal(responseContainer.getTotal() + archive.getPrice());
            responseContainer.setN_positions(responseContainer.getN_positions() + archive.getN_positions());
        });

        System.out.println("TOTALE POSIZIONI ARCHIVIO: " + responseContainer.getN_positions());

        //ordino timeline per alias
        responseContainer.setTimelineList(sortTimelineForAlias(timelineList));

        // responseContainer.setN_users((int) precisePositionsList.stream().filter(PositionUtils.distinctByKey(b -> b.getSubject())).count());
        return responseContainer;

    }

    public List<List<PositionEntity>> removeMyPositionList(List<List<PositionEntity>> separatedPositionsList) {

        //mi prendo il mio nome utente
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String myUsername = authentication.getName();
        System.out.println("UTENTE_LOGGATO: " + myUsername);
        User user = this.userRepository.findByUsername(myUsername);
        String myId = user.getId();
        System.out.println("MIO_ID: " + myId);

        //per tutte le liste di utenti separati
        for (int i = 0; i < separatedPositionsList.size(); i++) {
            //se dentro una lista trovo il mio id, lo elimino dalla lista e interrompo il ciclo
            List<PositionEntity> user_pos_list = separatedPositionsList.get(i);
            //se la prima posizione della lista ha subject_id = al mio, lo elimino dalla lista delle liste
            if (separatedPositionsList.get(i).get(0).getSubject_id().equals(myId)) {
                separatedPositionsList.remove(i);
                System.out.println("RIMUOVO LISTA USER_ID: " + myId);
            }
        }
        return separatedPositionsList;
    }

    List<Archive> removeMyArchive(List<Archive> archiveList) {
        //mi prendo il mio nome utente
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String myUsername = authentication.getName();

        for (int i = 0; i < archiveList.size(); i++) {
            Archive a = archiveList.get(i);
            if (a.getOwner().equals(myUsername)) {
                System.out.println("RIMUOVO_ARCHIVIO: " + a.getOwner());
                archiveList.remove(i);
            }
        }
        return archiveList;
    }

    @Override
    public PositionsResponseContainer getAppximatedPositionsByTimestampPolygonUsers(List<PositionEntry> polygon_entries, Long minTime, Long maxTime, List<User> users) {
        //1)recupero posizioni approssimate per timestamp
        // 2)recupero posizioni esatte da "positions" e le converto in ApproximatedPositionCoordinates

        //riempo lista con id utenti
        List<String> usersID = new ArrayList<>();
        users.forEach(user -> usersID.add(user.getId()));

        List<PositionEntity> precisePositionsList = null;
        PositionsResponseContainer responseContainer = new PositionsResponseContainer();
        List<List<PositionEntity>> separatedPositionsList;
        List<List<ApproximatedPositionCoordinatesEntity>> responseList = null;
        List<ApproximatedPositionTimestampEntity> timelineList = null;
        //1)

        //converto lista in poligono
        Polygon polygon = utilities.convertToPolygon(polygon_entries);

        //recupero lista posizioni dentro il poligono indicato
        List<ApproximatedPositionCoordinatesEntity> insidePoligonPositionsList = approxPositionCoordinatesRepository.findByPositionWithin(polygon);

        //Recupero posizioni dentro il timestamp indicato
        List<ApproximatedPositionTimestampEntity> timestampPositionsList = approxPositionTimestampRepository.findByTimestampBetween(minTime, maxTime);

        //per ogni id della lista_id di ogni approx_timestamp
        //controllo se id presente in una delle liste_id di ogni approx_coord
        //se si, devo creare una nuova timestampPositionsList con l'oogetto approx, e aggiungo l'id alla lista_id

        //sostituisco con il risultato finale della lista
        timestampPositionsList = utilities.joinTables(timestampPositionsList, insidePoligonPositionsList);

        //trovo tutte le posizioni "precise" aventi id uguale a quello
        //contenuto nella lista di "id" delle poszioni approssimate e setto il subject_id
        precisePositionsList = utilities.findPrecisePositionsBySubjectIdList(timestampPositionsList, responseContainer, usersID);

        //non ho trovato nessuna posizione
        if (precisePositionsList.isEmpty())
            throw new PositionsNotFoundException(minTime, maxTime);


        //ottengo la lista delle lista separate per utente
        separatedPositionsList = utilities.getSeparatedPositionsBySubject(precisePositionsList);

        //ordino per alias
        separatedPositionsList = sortPositionsByAlias(separatedPositionsList);
        //rimuovo la mia lista, se c'è
        separatedPositionsList = removeMyPositionList(separatedPositionsList);

        if (separatedPositionsList.isEmpty())
            throw new PositionsNotFoundException(minTime, maxTime);


        System.out.println("UTENTI_RICHIESTI: " + separatedPositionsList.size());
        //converto la lista in ApproxCoordinatesEntity
        responseList = utilities.convertList(separatedPositionsList);

        //creo l'oggetto di risposta da dare al client
        responseContainer.setApproxPositionsList(responseList);

        //aggiungo timelineList
        timelineList = utilities.createTimelineListBySubject(timestampPositionsList, separatedPositionsList);
        responseContainer.setTimelineList(timelineList);
        //setto il numero di utenti a cui appartengono le posizioni
        responseContainer.setN_users(responseContainer.getApproxPositionsList().size());

        //elimino il mio archivio dalla lista
        responseContainer.setArchiveList(removeMyArchive(responseContainer.getArchiveList()));

        //setto il totale degli archivi
        responseContainer.getArchiveList().forEach(archive -> {
            responseContainer.setTotal(responseContainer.getTotal() + archive.getPrice());
            responseContainer.setN_positions(responseContainer.getN_positions() + archive.getN_positions());
        });

        //ordino timeline per alias
        responseContainer.setTimelineList(sortTimelineForAlias(timelineList));
        // responseContainer.setN_users((int) precisePositionsList.stream().filter(PositionUtils.distinctByKey(b -> b.getSubject())).count());
        System.out.println("ResponseC:    " + responseContainer);
        return responseContainer;
        //
        //
    }


    List<List<PositionEntity>> sortPositionsByAlias(List<List<PositionEntity>> positionList){
            Collections.sort(positionList,
                    (o1, o2) -> o1.get(0).getAlias().compareTo(o2.get(0).getAlias()));

        return positionList;
    }

    List<ApproximatedPositionTimestampEntity> sortTimelineForAlias(List<ApproximatedPositionTimestampEntity> timelineList) {
        Collections.sort(timelineList,
                (o1, o2) -> o1.getAlias().compareTo(o2.getAlias()));
        return timelineList;
    }


    @Override
    public Archive getBoughtArchive(String archive_id, String username) {


        //prendo l'utente che ha acquistato archive_id
        User user = userRepository.findByUsername(username);
        System.out.println("User: " + user);
        //prendo la lista dei suoi archivi acquistati
        List<BoughtArchive> archiveList = user.getBoughtArchiveList();
        //
        BoughtArchive boughtArchive = archiveList.stream()
                .filter(archive -> archive.getArchive_id().equals(archive_id))
                .findAny()
                .orElse(null);

        if (boughtArchive == null)
            throw new ArchiveNotFoundException(archive_id);
        return boughtArchive;
    }

    @Override
    public List<BoughtArchive> getBoughtArchives(String username) {

        //Prendo l'utente che sta facendo richiesta
        User user = userRepository.findByUsername(username);
        System.out.println("User: " + user);
        //prendo la lista dei suoi archivi acquistati
        List<BoughtArchive> boughtArchiveList = user.getBoughtArchiveList();
        if (boughtArchiveList == null)
            throw new ArchiveNotFoundException(null);
        return boughtArchiveList;
    }

    @Override
    public List<Archive> getArchivesInsidePolygon(BuyRequest buyRequest, String subject) {
        //converto entry in un poligono
        //ricavo dalle posizioni esatte la lista di archivi da acquistare
        List<Archive> required_archiveList = buyRequest.getArchiveList();
        List<PositionEntity> positionsList;
        List<Archive> selectedArchiveList = new ArrayList<>();
        //recupero utente che ha fatto la richiesta
        User user = userRepository.findByUsername(subject);

        Polygon polygon = utilities.convertToPolygon(buyRequest.getPolygonPoints());
        positionsList = positionRepository.findByPositionWithin(polygon);

        //se una delle posizioni esatte è dentro un archivio, questo lo metto nella lista aggiornata
        for (int i = 0; i < positionsList.size(); i++) {
            PositionEntity current_pos = positionsList.get(i);

            Archive finded_archive = required_archiveList.stream()
                    .filter(archive -> archive.getId().equals(current_pos.getArchiveId()))
                    .findAny()
                    .orElse(null);

            if (finded_archive == null)
                throw new ArchiveNotFoundException(required_archiveList.toString());

            //ho trovato l'archivio corrispondente, lo metto in lista
            if (!selectedArchiveList.contains(finded_archive)) {
                //controllo se l'archivio è già stato acquistato dall'utente
                BoughtArchive boughtArchive = user.getBoughtArchiveList().stream().filter(a -> a.getArchive_id().equals(finded_archive.getId())).findAny().orElse(null);
                //è già stato acquistato
                if (boughtArchive != null)
                    finded_archive.setJust_bought(true);

                selectedArchiveList.add(finded_archive);
            }

            //se li ho trovati tutti, interrompo ricerca
            if (selectedArchiveList.size() == required_archiveList.size())
                break;
        }

        return selectedArchiveList;
    }

    @Override
    public List<Archive> getArchives(String username) {
        //prendo l'utente che ha acquistato archive_id
        User user = userRepository.findByUsername(username);
        if (user == null)
            throw new UserNotFoundException(username);

        System.out.println("User: " + user);

        List<Archive> loadedArchives = archiveRepository.findAllByOwner(user.getUsername());
        System.out.println("Numero archivi caricati: " + loadedArchives.size());
        if (loadedArchives == null)
            throw new ArchiveNotFoundException(null);
        return loadedArchives;
    }

}
