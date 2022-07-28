package com.gruppo3.ai.lab3.utils;

import com.gruppo3.ai.lab3.model.*;
import com.gruppo3.ai.lab3.repository.*;
import org.joda.time.DateTime;
import org.joda.time.Weeks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.data.geo.Polygon;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class PositionUtils {

    private final UserRepository userRepository;
    private final PositionRepository positionRepository;
    private final ApproximatedPositionCoordinatesRepository approxPositionCoordinatesRepository;
    private final ApproximatedPositionTimestampRepository approxPositionTimestampRepository;
    private final ArchiveRepository archiveRepository;

    @Autowired
    public PositionUtils(UserRepository userRepository,
                         PositionRepository positionRepository,
                         ApproximatedPositionCoordinatesRepository approxPositionCoordinatesRepository,
                         ApproximatedPositionTimestampRepository approxPositionTimestampRepository,
                         ArchiveRepository archiveRepository) {
        this.userRepository = userRepository;
        this.positionRepository = positionRepository;
        this.approxPositionCoordinatesRepository = approxPositionCoordinatesRepository;
        this.approxPositionTimestampRepository = approxPositionTimestampRepository;
        this.archiveRepository = archiveRepository;
    }


    public static ResponseContainer addCandidate(List<PositionEntity> entities,
                                                 String username,
                                                 PositionRepository positionRepository) {

        LinkedList<PositionEntity> validPositionList = new LinkedList<>();
        ResponseContainer responseContainer = new ResponseContainer();
        boolean positionValid;

        Iterator<PositionEntity> iter = entities.iterator();
        try {
            while (iter.hasNext()) {
                PositionEntity position = iter.next();
                positionValid = validateCandidatePosition(position, username, positionRepository, validPositionList, responseContainer);
                if (positionValid) {
                    validPositionList.add(position);

                } else {
                    iter.remove();
                }
            }
        } catch (ConcurrentModificationException ex) {
            throw new ConcurrentModificationException(ex);
        }

        return responseContainer;
    }


    private static boolean validateCandidatePosition(PositionEntity position, String username, PositionRepository positionRepository, LinkedList<PositionEntity> validPositionList, ResponseContainer responseContainer) {
        PositionErrors positionErrors = new PositionErrors();

        try {
            if (position.getposition().getY() < -90L || position.getposition().getY() > 90L) {
                positionErrors.setError(true);
                positionErrors.setLatitude(true);
            }
            // acquisisco longitudine e faccio verifica
            if (position.getposition().getX() < -180L || position.getposition().getX() > 180L) {
                positionErrors.setError(true);
                positionErrors.setLongitude(true);
            }

            PositionEntity lastPosition;

            List<PositionEntity> positions = new ArrayList<>();
            if (!validPositionList.isEmpty()) {
                // la lista delle posizioni dell'utente non è vuota
                // prendi l'ultima posizione dalla lista
                lastPosition = validPositionList.getLast();
            } else {

                positions = positionRepository.findAllBySubjectOrderByTimestamp(username);
                System.out.println("Username: " + username);
                //System.out.println("SIZE_POSITIONS: " + positions.size());
                if (positions.isEmpty())
                    return true;
                else {
                    int size = positions.size();
                    lastPosition = positions.get(size - 1);
                    System.out.println("LAST POSITION - TIMESTAMP: " + lastPosition.getTimestamp());
                }
            }
            if (lastPosition != null) {
                long lastTimeStamp = lastPosition.getTimestamp();
                // verifica di coerenza cronologica
                if (lastTimeStamp >= position.getTimestamp()) {
                    positionErrors.setError(true);
                    positionErrors.setTimeStamp(true);
                }
                // verifico che la velocità sia < di 100 m/s
                double distance = GeoFunction.distance(position.getposition().getY(), position.getposition().getX(), lastPosition.getposition().getY(), lastPosition.getposition().getX()) * 1000;
                double intervalTime = position.getTimestamp() - lastTimeStamp;
                double speed = distance / intervalTime;

                if (speed >= 100D) {
                    positionErrors.setError(true);
                    positionErrors.setSpeed(true);
                }
            }
            if (positionErrors.isError()) {
                throw new PositionNotValidException();
            }
        } catch (PositionNotValidException e) {
            System.out.println("Posizione non valida");
            createResponse(position, positionErrors.getErrors(), responseContainer);
            return false;
        }
        return true;
    }

    private static void createResponse(PositionEntity pos, String reason, ResponseContainer responseContainer) {

        InvalidPositionEntity invalidPosition = new InvalidPositionEntity(pos, reason);
        responseContainer.getInvalidList().add(invalidPosition);
        responseContainer.setAllPositionValid(false);
    }

    /*prendo il massimo timestamp della lista delle posizioni*/
    private PositionEntity getMaxPosition(List<PositionEntity> positions) {
        long max = 0;
        PositionEntity maxPosition = null;
        for (PositionEntity pos : positions) {
            if (pos.getTimestamp() > max) {
                max = pos.getTimestamp();
                maxPosition = pos;
            }
        }
        return maxPosition;
    }

    //usato per contare numero utenti dele
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    //metodo per dividere le posizioni approssimate in liste separate per subject_id
    public List<ApproximatedPositionTimestampEntity> createTimelineListBySubject(List<ApproximatedPositionTimestampEntity> approx_list, List<List<PositionEntity>> separatedPositionsList) {
        //scorro la lista delle posizioni divise per utente
        //controllo se ogni id della posizione esatta è presente in una delle liste di id delle posizioni approssimate
        //in caso positivo, aggiungo la posizioni approssimata dentro una lista di posizioni approssimate
        //setto subject_id
        //devo permettere la presenza di duplicati
        //alla fine magari la divido per subject_id
        List<ApproximatedPositionTimestampEntity> timelineList = new ArrayList<>();

        for (int i = 0; i < separatedPositionsList.size(); i++) {
            //prendo la singola lista di posizioni per subject
            List<PositionEntity> list = separatedPositionsList.get(i);
            //Per ogni posizioni esatta della lista
            for (int m = 0; m < list.size(); m++) {
                PositionEntity current_pos = list.get(m);

                for (int j = 0; j < approx_list.size(); j++) {
                    ApproximatedPositionTimestampEntity current_approx = approx_list.get(j);
                    List<String> idList = current_approx.getPrecise_positions_list();
                    //scorro tutta la lista di id
                    String current_id = idList.stream()
                            .filter(id -> id.equals(current_pos.getId()))
                            .findAny()
                            .orElse(null);

                    //se ho trovato una corrispondenza
                    if (current_id != null) {
                        //setto il nome utente della posizione approssimata perchè corrisponde l'id
                        ApproximatedPositionTimestampEntity approx_copy = new ApproximatedPositionTimestampEntity();
                        approx_copy.setTimestamp(current_approx.getTimestamp());
                        approx_copy.setSubject_id(current_pos.getSubject_id());
                        approx_copy.setAlias(current_pos.getAlias());
                        //e la salvo dentro una lista
                        timelineList.add(approx_copy);
                    }
                }
            }
        }
        return timelineList;
    }

    //trovo le posizioni precise e imposto a quelle approssimate il subject_id
    public List<PositionEntity> findPrecisePositionsById(List<ApproximatedPositionTimestampEntity> timestampPositionsList, PositionsResponseContainer responseContainer, String subject) {
        List<PositionEntity> precisePositionsList = new ArrayList<>();
        for (int i = 0; i < timestampPositionsList.size(); i++) {
            ApproximatedPositionTimestampEntity current_approx = timestampPositionsList.get(i);

            for (int j = 0; j < current_approx.getPrecise_positions_list().size(); j++) {
                String approx_id = current_approx.getPrecise_positions_list().get(j);
                PositionEntity accurate_pos = positionRepository.findById(approx_id).get();

                if (subject.equals("all")) {
                    if (accurate_pos.getId() != null) {
                        //imposto subject_id nella ApproximatedPositionTimestamp
                        String id = userRepository.findByUsername(accurate_pos.getSubject()).getId();
                        current_approx.setSubject_id(id);
                        //se archive_id non è già contenuto
                        //riempio la lista di con gli id_degli archivi da mettere nella risposta(response container)
                        Archive archive = archiveRepository.findById(accurate_pos.getArchiveId()).get();

                        //controllo che non sia già presente in lista
                        Archive arch = null;
                        if (!responseContainer.getArchiveList().isEmpty()) {
                            arch = responseContainer.getArchiveList()
                                    .stream()
                                    .filter(arc -> arc.getId().equals(archive.getId()))
                                    .findAny()
                                    .orElse(null);
                        }

                        //non è ancora in lista
                        if (arch == null) {
                            archive.setPositionsIdList(null);
                            responseContainer.getArchiveList().add(archive);
                        }
                        precisePositionsList.add(accurate_pos);
                    }
                }
//                } else {
//                    System.out.println("Caso con subject: " + subject);
//                    if (accurate_pos.getId() != null && accurate_pos.getSubject().equals(subject)) {
//                        //imposto subject_id nella ApproximatedPositionTimestamp
//                        String id = userRepository.findByUsername(accurate_pos.getSubject()).getId();
//                        current_approx.setSubject_id(id);
//                        //se archive_id non è già contenuto
//                        //riempio la lista di con gli id_degli archivi da mettere nella risposta(response container)
//                        Archive archive = archiveRepository.findById(accurate_pos.getArchiveId()).get();
//
//                        //controllo che non sia già presente in lista
//                        Archive arch = null;
//                        if (!responseContainer.getArchiveList().isEmpty()) {
//                            arch = responseContainer.getArchiveList()
//                                    .stream()
//                                    .filter(arc -> arc.getId().equals(archive.getId()))
//                                    .findAny()
//                                    .orElse(null);
//                        }
//
//                        //non è ancora in lista
//                        if (arch == null) {
//                            archive.setPositionsIdList(null);
//                            responseContainer.getArchiveList().add(archive);
//                        }
//                        precisePositionsList.add(accurate_pos);
//                    }
//                }
            }
        }
        return precisePositionsList;
    }

    //trovo le posizioni precise e imposto a quelle approssimate il subject_id
    public List<PositionEntity> findPrecisePositionsBySubjectIdList(List<ApproximatedPositionTimestampEntity> timestampPositionsList, PositionsResponseContainer responseContainer, List<String> usersID) {
        List<PositionEntity> precisePositionsList = new ArrayList<>();

        //per tutti gli Id degli utenti ricevuti
        for( String subject_id: usersID) {
            for (int i = 0; i < timestampPositionsList.size(); i++) {
                ApproximatedPositionTimestampEntity current_approx = timestampPositionsList.get(i);

                for (int j = 0; j < current_approx.getPrecise_positions_list().size(); j++) {
                    String approx_id = current_approx.getPrecise_positions_list().get(j);
                    PositionEntity accurate_pos = positionRepository.findById(approx_id).get();

                        System.out.println("Caso con subject: " + subject_id);
                        //Recupero utente specificato nel campo della posizioni esatta
                        User user_accurate_pos = userRepository.findByUsername(accurate_pos.getSubject());
                        //se l'id corrisponde a quello in arrivo dal client
                        if (accurate_pos.getId() != null && user_accurate_pos.getId().equals(subject_id)) {
                            //imposto subject_id nella ApproximatedPositionTimestamp
                            String id = userRepository.findByUsername(accurate_pos.getSubject()).getId();
                            current_approx.setSubject_id(id);
                            //se archive_id non è già contenuto
                            //riempio la lista di con gli id_degli archivi da mettere nella risposta(response container)
                            Archive archive = archiveRepository.findById(accurate_pos.getArchiveId()).get();

                            //controllo che non sia già presente in lista
                            Archive arch = null;
                            if (!responseContainer.getArchiveList().isEmpty()) {
                                arch = responseContainer.getArchiveList()
                                        .stream()
                                        .filter(arc -> arc.getId().equals(archive.getId()))
                                        .findAny()
                                        .orElse(null);
                            }

                            //non è ancora in lista
                            if (arch == null) {
                                archive.setPositionsIdList(null);
                                responseContainer.getArchiveList().add(archive);
                            }
                            precisePositionsList.add(accurate_pos);
                        }

                }
            }
        }
        System.out.println("Numero posizioni: " + precisePositionsList.size());
        return precisePositionsList;
    }


    //conveto List<List<PositionEntity>> in List<List<ApproxCoordEntity>>
    public List<List<ApproximatedPositionCoordinatesEntity>> convertList(List<List<PositionEntity>> separatedPositionsList) {
        List<List<ApproximatedPositionCoordinatesEntity>> responseList = new ArrayList<>();

        for (int i = 0; i < separatedPositionsList.size(); i++) {
            List<PositionEntity> list = separatedPositionsList.get(i);
            List<ApproximatedPositionCoordinatesEntity> approx_list = new ArrayList<>();
            for (int j = 0; j < list.size(); j++) {
                PositionEntity pos = list.get(j);
                approx_list.add(new ApproximatedPositionCoordinatesEntity(pos));
            }
            responseList.add(approx_list);
        }
        return responseList;
    }

    //metodo usato per dividere le posizioni accurate in una lista di liste di posizioni divise per utente
    public List<List<PositionEntity>> getSeparatedPositionsBySubject(List<PositionEntity> precisePositionsList) {
        Map map = new HashMap();
        for (Iterator iter = precisePositionsList.iterator(); iter.hasNext(); ) {
            PositionEntity pos = (PositionEntity) iter.next();
            //setto subject_id di tutte le posizioni
            User user = userRepository.findByUsername(pos.getSubject());
            pos.setSubject_id(user.getId());
            pos.setAlias(user.getAlias());
            List list = (List) map.get(pos.getSubject());
            if (list == null) {
                list = new ArrayList();
                map.put(pos.getSubject(), list);
            }
            list.add(pos);
        }
        List<List<PositionEntity>> listOfPositionsList = new ArrayList<>(map.values());
        return listOfPositionsList;
    }

    //ritorno una lista di liste di posizioni
    public List<List<PositionEntity>> separateArchives(List<PositionEntity> entities) {
        List<List<PositionEntity>> separated_archives = new ArrayList<>();
        List<PositionEntity> duplicated_list = entities;
        List<PositionEntity> deleted_elements_index;

        for (int i = 0; i < entities.size(); i++) {
            //lista dove salvo indice elemto cancelaltto e già controllato
            deleted_elements_index = new ArrayList<>();
            //per ogni posizione istanzio una sua possibile collezione della stessa settimana
            PositionEntity current_pos = entities.get(i);
            List<PositionEntity> separated_positions_list = new ArrayList<>();
            DateTime current_date = new DateTime(current_pos.getTimestamp() * 1000L);
            System.out.println("Current_date: " + current_date);

            for (int j = 0; j < duplicated_list.size(); j++) {
                PositionEntity compare_pos = duplicated_list.get(j);
                DateTime compare_date = new DateTime(compare_pos.getTimestamp() * 1000L);
                System.out.println("Compare_date: " + compare_date);
                //int weeks = weeks_between_two_dates(current_date, compare_date);
                int weeks = Weeks.weeksBetween(current_date, compare_date).getWeeks();
                System.out.println("Diff_Settimana: " + weeks);
                //appartengono alla stessa settimana
                if (weeks == 0) {
                    //aggiungo alla stessa collezione
                    separated_positions_list.add(compare_pos);
                    //la elimino tra quelle scorribili nel ciclo j
                    duplicated_list.remove(compare_pos);
                    //salvo nell'array l'indice dell'elemento trovato ed eliminato dalla lista duplicati
                    deleted_elements_index.add(compare_pos);
                    //lista scorre in avanti, scendo di un posto
                    j--;
                }
            }
            //aggiungo la collezione_settimanale alla lista di archivi
            //se ho trovato delle posizioni della stessa settimana
            if (!separated_positions_list.isEmpty())
                separated_archives.add(separated_positions_list);
            //la lista scorre in avanti dopo l'eliminazione
            //vado avanti per il numero di elementi controllati
            //elimino elementi dalla lista
            deleted_elements_index.forEach(pos -> entities.remove(pos));
            //entities.remove(current_pos);
            //scorre la in avanti, quindi torno indietro di una posizione
            System.out.println("i: " + i);
            //-1 perchè altrimenti il ciclo non continua
            i = -1;
        }
        System.out.println("ARCHIVI_SEPARATI: " + separated_archives.size());
        return separated_archives;
    }



    //meotodo per eliminare id da posiz approx (x,y)
    public List<ApproximatedPositionCoordinatesEntity> removePositionIdCoord(List<ApproximatedPositionCoordinatesEntity> approx_coordinates_list, List<String> id_to_remove) {
        for (int i = 0; i < id_to_remove.size(); i++) {
            String current_id = id_to_remove.get(i);
            //Per tutte le posiz approssimate della lista approx_coordinates_list
            for (int j = 0; j < approx_coordinates_list.size(); j++) {
                ApproximatedPositionCoordinatesEntity current_approx = approx_coordinates_list.get(j);
                List<String> id_list = current_approx.getPrecise_positions_list();
                String pos_id = id_list
                        .stream()
                        .filter(id -> id.equals(current_id))
                        .findAny()
                        .orElse(null);
                //se ho trovato un id, lo rimuovo dalla lista
                //interrompo la ricerca sulla lista di questa misura approx
                if (pos_id != null)
                    id_list.remove(pos_id);

                //se la lista rimane vuota, elimino tutta la approx dalla lista e dal db
                if (id_list.isEmpty()) {
                    approx_coordinates_list.remove(current_approx);
                    approxPositionCoordinatesRepository.deleteById(current_approx.getId());
                }
            }
        }
        return approx_coordinates_list;
    }

    //meotodo per eliminare id da posiz approx (x,y)
    public List<ApproximatedPositionTimestampEntity> removePositionIdTime(List<ApproximatedPositionTimestampEntity> approx_timestamp_list, List<String> id_to_remove) {
        for (int i = 0; i < id_to_remove.size(); i++) {
            String current_id = id_to_remove.get(i);

            //Per tutte le posiz approssimate della lista approx_coordinates
            for (int j = 0; j < approx_timestamp_list.size(); j++) {
                ApproximatedPositionTimestampEntity current_approx = approx_timestamp_list.get(j);
                List<String> id_list = current_approx.getPrecise_positions_list();
                String pos_id = id_list
                        .stream()
                        .filter(id -> id.equals(current_id))
                        .findAny()
                        .orElse(null);
                //se ho trovato un id, lo rimuovo dalla lista
                //interrompo la ricerca sulla lista di questa misura approx
                if (pos_id != null)
                    id_list.remove(pos_id);

                //se la lista rimane vuota, elimino tutta la approx dalla lista
                //e dal db
                if (id_list.isEmpty()) {
                    approx_timestamp_list.remove(current_approx);
                    approxPositionTimestampRepository.deleteById(current_approx.getId());
                }
            }
        }
        return approx_timestamp_list;
    }

    //metodo per la ricerca di duplicati
    public List<ApproximatedPositionCoordinatesEntity> findDuplicateCoordinates(List<ApproximatedPositionCoordinatesEntity> new_approx_coordinatesList) {

        //considero anche quelli già salvati nel db ( se ce n'è qualcuno)
        //lista di coordinate già esistenti nel db
        List<ApproximatedPositionCoordinatesEntity> old_approx_coordinatesList = approxPositionCoordinatesRepository.findAll();
        List<ApproximatedPositionCoordinatesEntity> noDuplicateList = new ArrayList<>();
        List<ApproximatedPositionCoordinatesEntity> all_approx_coordinatesList = new ArrayList<>();
        //se non è vuota
        //creo una nuova lista, prima metto le approx vecchie, poi le nuove
        if (!old_approx_coordinatesList.isEmpty())
            all_approx_coordinatesList = Stream.concat(old_approx_coordinatesList.stream(), new_approx_coordinatesList.stream()).distinct().collect(Collectors.toList());
        else
            //altrimenti soltanto quelle nuove
            all_approx_coordinatesList.addAll(new_approx_coordinatesList);

        for (int i = 0; i < all_approx_coordinatesList.size(); i++) {
            ApproximatedPositionCoordinatesEntity just_inList = null;
            ApproximatedPositionCoordinatesEntity current_approx = all_approx_coordinatesList.get(i);
            GeoJsonPoint current_position = current_approx.getPosition();

            //la eseguo soltanto se è già piena
            //controllo che tale approx non sia già salvata nella lista da ritornare
            //se ho in lista un oggetto con già queste coordinate
            if (!noDuplicateList.isEmpty())
                just_inList = noDuplicateList
                        .stream()
                        .filter(position -> position.getPosition().getX() == current_position.getX() && position.getPosition().getY() == current_position.getY())
                        .findAny()
                        .orElse(null);

            //se è la prima posizione avente queste X,Y
            // per una ogni posizione "i", scoorro tutte le restanti "j" con stesse cooridnate
            if (just_inList == null) {
                for (int j = 0; j < all_approx_coordinatesList.size(); j++) {
                    ApproximatedPositionCoordinatesEntity compare_approx = all_approx_coordinatesList.get(j);
                    if (current_position.getX() == compare_approx.getPosition().getX()
                            && current_position.getY() == compare_approx.getPosition().getY()) {
                        //ho trovato un duplicato
                        //se non in lista
                        //aggiungo id della posizione precisa del duplicato nella lista di quello approssimato
                        if (!current_approx.getPrecise_positions_list().contains(compare_approx.getMy_precise_position_id()))
                            current_approx.getPrecise_positions_list().add(compare_approx.getMy_precise_position_id());
                    }
                }
                //inserisco soltanto l'oggetto nn duplicato
                noDuplicateList.add(current_approx);
            }
        }
        return noDuplicateList;
    }

    //metodo per la ricerca dei duplicati
    public List<ApproximatedPositionTimestampEntity> findDuplicateTimestamp(List<ApproximatedPositionTimestampEntity> new_approx_timestampList) {
        List<ApproximatedPositionTimestampEntity> all_approx_timestampList = new ArrayList<>();
        List<ApproximatedPositionTimestampEntity> noDuplicateList = new ArrayList<>();
        List<ApproximatedPositionTimestampEntity> old_approx_timestampList = approxPositionTimestampRepository.findAll();

        //se non è vuota
        //creo una nuova lista, prima metto le approx vecchie, poi le nuove
        if (!old_approx_timestampList.isEmpty())
            all_approx_timestampList = Stream.concat(old_approx_timestampList.stream(), new_approx_timestampList.stream()).distinct().collect(Collectors.toList());
        else
            //altrimenti soltanto quelle nuove
            all_approx_timestampList.addAll(new_approx_timestampList);

        for (int i = 0; i < all_approx_timestampList.size(); i++) {
            ApproximatedPositionTimestampEntity just_inList = null;
            ApproximatedPositionTimestampEntity current_approx = all_approx_timestampList.get(i);

            //la eseguo soltanto se è già piena
            //controllo che tale approx non sia già salvata nella lista da ritornare
            //se ho in lista un oggetto con già queste coordinate
            if (!noDuplicateList.isEmpty())
                just_inList = noDuplicateList
                        .stream()
                        .filter(position -> position.getTimestamp().equals(current_approx.getTimestamp()))
                        .findAny()
                        .orElse(null);

            //se è la prima posizione avente queste X,Y
            // per una ogni posizione "i", scoorro tutte le restanti "j" con stesse cooridnate
            if (just_inList == null) {
                for (int j = 0; j < all_approx_timestampList.size(); j++) {
                    ApproximatedPositionTimestampEntity compare_approx = all_approx_timestampList.get(j);
                    if (current_approx.getTimestamp().equals(compare_approx.getTimestamp())) {
                        //ho trovato un duplicato
                        //se non in lista
                        //aggiungo id della posizione precisa del duplicato nella lista di quello approssimato
                        if (!current_approx.getPrecise_positions_list().contains(compare_approx.getMy_precise_position_id()))
                            current_approx.getPrecise_positions_list().add(compare_approx.getMy_precise_position_id());
                    }
                }
                //inserisco soltanto l'oggetto nn duplicato
                noDuplicateList.add(current_approx);
            }
        }
        return noDuplicateList;
    }


    //metodo per convertire PositionEntry in poligono
    public Polygon convertToPolygon(List<PositionEntry> polygon_entries) {
        List<Point> points = new ArrayList<>();
        for (PositionEntry location : polygon_entries) {
            Point locationPoint = new Point(
                    location.getLongitude(),
                    location.getLatitude());
            points.add(locationPoint);
        }
        Polygon polygon = new Polygon(points);
        return polygon;
    }

    public List<ApproximatedPositionTimestampEntity> joinTables(List<ApproximatedPositionTimestampEntity> approxTimestampList, List<ApproximatedPositionCoordinatesEntity> insidePolygonList) {
        List<ApproximatedPositionTimestampEntity> copyTimeStampPositionList = new ArrayList<>();
        //adesso faccio il join degli id presenti tra i due e prendo la posizione approxTimestamp

        for (int i = 0; i < approxTimestampList.size(); i++) {
            ApproximatedPositionTimestampEntity timestamp_approx = approxTimestampList.get(i);
            ApproximatedPositionTimestampEntity modified_approx = new ApproximatedPositionTimestampEntity();
            List<String> id_list = timestamp_approx.getPrecise_positions_list();

            //scorro tutti gli id della approx_timestamp corrente
            for (int j = 0; j < id_list.size(); j++) {
                String current_id = id_list.get(j);

                //lo confronto con tutte le liste di id di tutte le posizioni approssimate del poligono
                for (int m = 0; m < insidePolygonList.size(); m++) {
                    ApproximatedPositionCoordinatesEntity coord_approx = insidePolygonList.get(m);
                    List<String> id_list2 = coord_approx.getPrecise_positions_list();

                    String finded_id = id_list2.stream().filter(id -> id.equals(current_id)).findAny().orElse(null);

                    if (finded_id != null) {
                        //creo nuovo oggetto ApproxTimestamp

                        modified_approx.setId(timestamp_approx.getId());
                        modified_approx.setTimestamp(timestamp_approx.getTimestamp());
                        modified_approx.getPrecise_positions_list().add(finded_id);
                        //aggiungo approx_timestamp però soltanto con id_posizioni trovate

                        break;
                    }
                }
            }
            //quando ho finito la ricerca di tutti gli di della approx_timestamp corrente
            //la metto in lista
            copyTimeStampPositionList.add(modified_approx);
        }
        return copyTimeStampPositionList;
    }

}
