package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.CachedTimeTable;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Trips;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * *  @author Valentin Walendy (393413)
 * *  @author Ruben Lellouche (400288)
 * Représente un profil de trajet basé sur un horaire spécifique et une date donnée.
 * Il contient les informations sur les stations et leurs fronts de Pareto.
 */
public record Profile(
        TimeTable timeTable,
        LocalDate date,
        int arrStationId,
        List<ParetoFront> stationFront){

    /**
     * Constructeur de Profile qui crée une copie immuable de la liste stationFront.
     * @param timeTable L'horaire utilisé.
     * @param date La date du profil.
     * @param arrStationId L'identifiant de la station d'arrivée.
     * @param stationFront La liste des fronts de Pareto des stations.
     */
    public Profile{
        stationFront = List.copyOf(stationFront);
    }

    /**
     * Retourne les connexions disponibles pour la date spécifiée.
     * @return Un objet Connections contenant les trajets possibles.
     */
    public Connections connections(){
        return timeTable.connectionsFor(date);
    }

    /**
     * Retourne les voyages disponibles pour la date spécifiée.
     * @return Un objet Trips contenant les voyages possibles.
     */
    public Trips trips(){
        return timeTable.tripsFor(date);
    }

    /**
     * Récupère le front de Pareto d'une station donnée.
     * @param stationId L'identifiant de la station.
     * @return Le front de Pareto associé à la station.
     */
    public ParetoFront forStation(int stationId){
        return stationFront.get(stationId);
    }

    /**
     * Classe interne permettant la construction progressive d'un objet Profile.
     */
    public static class Builder {

        private final TimeTable timeTable;
        private final LocalDate date;
        private final int arrStationId;

        private final ParetoFront.Builder[] stationFrontBuilders;
        private final ParetoFront.Builder[] tripFrontBuilders;

        /**
         * Constructeur du Builder de Profile.
         * @param timeTable L'horaire utilisé.
         * @param date La date du profil.
         * @param arrStationId L'identifiant de la station d'arrivée.
         */
        public Builder(TimeTable timeTable, LocalDate date, int arrStationId) {
            this.timeTable = timeTable;
            this.date = date;
            this.arrStationId = arrStationId;
            //this.stationFrontBuilders = new ParetoFront.Builder[((FileTimeTable) ((CachedTimeTable) timeTable).underlying).stringTable().size()];
            this.stationFrontBuilders = new ParetoFront.Builder[timeTable.stations().size()];
            this.tripFrontBuilders = new ParetoFront.Builder[timeTable.tripsFor(date).size()];
        }

        /**
         * Récupère le Builder du front de Pareto d'une station donnée.
         * @param stationId L'identifiant de la station.
         * @return Le Builder du front de Pareto correspondant.
         */
        public ParetoFront.Builder forStation(int stationId) {
            return stationFrontBuilders[stationId];
        }

        /**
         * Définit le Builder du front de Pareto pour une station donnée.
         * @param stationId L'identifiant de la station.
         * @param builder Le Builder du front de Pareto.
         */
        public void setForStation(int stationId, ParetoFront.Builder builder) {
            stationFrontBuilders[stationId] = builder;
        }

        /**
         * Récupère le Builder du front de Pareto d'un trajet donné.
         * @param tripId L'identifiant du trajet.
         * @return Le Builder du front de Pareto correspondant.
         */
        public ParetoFront.Builder forTrip(int tripId){
            return tripFrontBuilders[tripId];
        }

        /**
         * Définit le Builder du front de Pareto pour un trajet donné.
         * @param tripId L'identifiant du trajet.
         * @param builder Le Builder du front de Pareto.
         */
        public void setForTrip(int tripId, ParetoFront.Builder builder){
            tripFrontBuilders[tripId] = builder;
        }

        /**
         * Construit un objet Profile en utilisant les données du Builder.
         * @return Une instance de Profile.
         */
        public Profile build(){
            List<ParetoFront> stationFronts = new ArrayList<>(stationFrontBuilders.length);
            for (ParetoFront.Builder builder : stationFrontBuilders){
                if (builder == null){
                    stationFronts.add(ParetoFront.EMPTY);
                } else {
                    stationFronts.add(builder.build());
                }
            }
            return new Profile(timeTable, date, arrStationId, stationFronts);
        }
    }
}