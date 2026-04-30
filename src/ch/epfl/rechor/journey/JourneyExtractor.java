package ch.epfl.rechor.journey;

import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.Routes;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * * @author Valentin Walendy (393413)
 * * @author Ruben Lellouche (400288)
 * Classe JourneyExtractor.
 * Extrait les voyages optimaux pour un profil et une gare de départ.
 */
public final class JourneyExtractor {

    /**
     * Constructeur privé empêchant l'instanciation.
     */
    private JourneyExtractor() {
        throw new UnsupportedOperationException();
    }

    /**
     * Extrait et retourne la liste des voyages optimaux triés par heure de départ et d'arrivée.
     *
     * @param profile      le profil de l'itinéraire
     * @param depStationId l'identifiant de la gare de départ
     * @return la liste des voyages optimaux
     */
    public static List<Journey> journeys(Profile profile, int depStationId) {
        List<Journey> journeys = new ArrayList<>();
        ParetoFront front = profile.forStation(depStationId);
        front.forEach(packedCriteria -> {
            List<Journey.Leg> legs = extractLegs(profile, depStationId, packedCriteria);
            journeys.add(new Journey(legs));
        });

        journeys.sort(Comparator
                .comparing(Journey::depTime)
                .thenComparing(Journey::arrTime));

        return journeys;
    }

    /**
     * Extrait les étapes d'un voyage à partir d'un critère initial packagé.
     *
     * @param profile         le profil de l'itinéraire
     * @param depStationId    l'identifiant de la gare de départ
     * @param initialCriteria le critère initial packagé
     * @return la liste des étapes constituant le voyage
     */
    private static List<Journey.Leg> extractLegs(Profile profile, int depStationId, long initialCriteria) {
        List<Journey.Leg> legs = new ArrayList<>();
        TimeTable timeTable = profile.timeTable();
        Connections connections = profile.connections();

        int currentStopId = depStationId;
        int finalArrMins = PackedCriteria.arrMins(initialCriteria);
        int currentArrMins = 0;

        int criterionPayload = PackedCriteria.payload(initialCriteria);
        int connectionId = criterionPayload >> 8;
        int connectionDepStopId = connections.depStopId(connectionId);

        if (timeTable.stationId(connectionDepStopId) != currentStopId) {
            addFootLeg(connections.depMins(connectionId),
                    false,
                    currentStopId,
                    connectionDepStopId,
                    timeTable,
                    profile,
                    legs);
        }

        for (int remainingChanges = PackedCriteria.changes(initialCriteria);
             remainingChanges >= 0; remainingChanges--) {
            ParetoFront pf = profile.forStation(timeTable.stationId(currentStopId));
            long currentCriteria = pf.get(finalArrMins, remainingChanges);

            criterionPayload = PackedCriteria.payload(currentCriteria);
            connectionId = criterionPayload >> 8;
            int skippedStops = criterionPayload & 0xFF;
            connectionDepStopId = connections.depStopId(connectionId);

            if (!legs.isEmpty() && legs.get(legs.size() - 1) instanceof Journey.Leg.Transport) {
                addFootLeg(currentArrMins,
                        true,
                        currentStopId,
                        connectionDepStopId,
                        timeTable,
                        profile,
                        legs);
            }

            connectionId = addTransportLeg(profile, timeTable, connectionId, skippedStops, legs);

            currentStopId = connections.arrStopId(connectionId);
            currentArrMins = connections.arrMins(connectionId);
        }

        if (timeTable.stationId(currentStopId) != profile.arrStationId()) {
            addFootLeg(currentArrMins,
                    true,
                    currentStopId,
                    profile.arrStationId(),
                    timeTable,
                    profile,
                    legs);
        }

        return legs;
    }

    /**
     * Ajoute une étape de transport avec arrêts intermédiaires et la retourne.
     *
     * @param profile      le profil de l'itinéraire
     * @param timeTable    la table des horaires
     * @param connectionId l'identifiant de la connexion courante
     * @param numStops     le nombre d'arrêts intermédiaires
     * @param legs         la liste des étapes du voyage
     * @return le nouvel identifiant de connexion après traitement
     */
    private static int addTransportLeg(
            Profile profile,
            TimeTable timeTable,
            int connectionId,
            int numStops,
            List<Journey.Leg> legs
    ) {
        Connections connections = profile.connections();
        Stations stations = timeTable.stations();
        Routes routes = timeTable.routes();

        int tripIndex = connections.tripId(connectionId);
        int depStopId = connections.depStopId(connectionId);
        int initDepMins = connections.depMins(connectionId);
        List<Journey.Leg.IntermediateStop> intermediateStops = new ArrayList<>();

        for (int i = 0; i < numStops; i++) {
            int intermediateArr = connections.arrMins(connectionId);
            connectionId = connections.nextConnectionId(connectionId);
            intermediateStops.add(new Journey.Leg.IntermediateStop(
                    buildStop(connections.depStopId(connectionId), timeTable, stations),
                    convertTime(intermediateArr, profile.date()),
                    convertTime(connections.depMins(connectionId), profile.date())
            ));
        }

        int finalStopId = connections.arrStopId(connectionId);
        Journey.Leg.Transport transportLeg = new Journey.Leg.Transport(
                buildStop(depStopId, timeTable, stations),
                convertTime(initDepMins, profile.date()),
                buildStop(finalStopId, timeTable, stations),
                convertTime(connections.arrMins(connectionId), profile.date()),
                intermediateStops,
                routes.vehicle(profile.trips().routeId(tripIndex)),
                routes.name(profile.trips().routeId(tripIndex)),
                profile.trips().destination(tripIndex)
        );

        legs.add(transportLeg);
        return connectionId;
    }

    /**
     * Construit et retourne un objet Stop à partir d'un identifiant d'arrêt.
     *
     * @param stopId    l'identifiant de l'arrêt
     * @param timeTable la table des horaires
     * @param stations  les stations disponibles
     * @return l'objet Stop correspondant
     */
    private static Stop buildStop(int stopId, TimeTable timeTable, Stations stations) {
        int stationId = timeTable.stationId(stopId);
        return new Stop(
                stations.name(stationId),
                timeTable.platformName(stopId),
                stations.longitude(stationId),
                stations.latitude(stationId)
        );
    }

    /**
     * Convertit un nombre de minutes en LocalDateTime en utilisant la date fournie.
     *
     * @param mins le nombre de minutes
     * @param date la date à utiliser
     * @return le LocalDateTime correspondant
     */
    private static LocalDateTime convertTime(int mins, LocalDate date) {
        return LocalDateTime.of(date, LocalTime.MIN).plusMinutes(mins);
    }

    /**
     * Ajoute une étape à pied (Foot) au voyage en fonction du temps et des arrêts.
     *
     * @param mins      le temps de référence en minutes
     * @param isDepMins true si le temps est celui de départ, sinon celui d'arrivée
     * @param depStopId l'identifiant de l'arrêt de départ
     * @param arrStopId l'identifiant de l'arrêt d'arrivée
     * @param timeTable la table des horaires
     * @param profile   le profil de l'itinéraire
     * @param legs      la liste des étapes du voyage
     */
    private static void addFootLeg(
            int mins,
            boolean isDepMins,
            int depStopId,
            int arrStopId,
            TimeTable timeTable,
            Profile profile,
            List<Journey.Leg> legs
    ) {
        int depStationId = timeTable.stationId(depStopId);
        int arrStationId = timeTable.stationId(arrStopId);
        int transferView = timeTable.transfers().arrivingAt(arrStationId);
        for (int i = PackedRange.startInclusive(transferView); i < PackedRange.endExclusive(transferView); ++i) {
            if (timeTable.transfers().depStationId(i) == depStationId) {
                int depMins, arrMins;
                if (isDepMins) {
                    depMins = mins;
                    arrMins = mins + timeTable.transfers().minutes(i);
                } else {
                    depMins = mins - timeTable.transfers().minutes(i);
                    arrMins = mins;
                }
                legs.add(new Journey.Leg.Foot(
                        buildStop(depStopId, timeTable, timeTable.stations()),
                        convertTime(depMins, profile.date()),
                        buildStop(arrStopId, timeTable, timeTable.stations()),
                        convertTime(arrMins, profile.date())
                ));
                break;
            }
        }
    }
}