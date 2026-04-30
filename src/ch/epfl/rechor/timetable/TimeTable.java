package ch.epfl.rechor.timetable;

import java.time.LocalDate;

/**
 *  * * @author Valentin Walendy (393413)
 *  * * @author Ruben Lellouche (400288)
 * Interface représentant un horaire de transport public.
 */
public interface TimeTable {

    /**
     * Retourne les gares indexées de l'horaire.
     *
     * @return les gares de l'horaire
     */
    Stations stations();

    /**
     * Retourne les noms alternatifs indexés des gares de l'horaire.
     *
     * @return les noms alternatifs des gares
     */
    StationAliases stationAliases();

    /**
     * Retourne les voies/quais indexés de l'horaire.
     *
     * @return les voies et quais de l'horaire
     */
    Platforms platforms();

    /**
     * Retourne les lignes indexées de l'horaire.
     *
     * @return les lignes de transport de l'horaire
     */
    Routes routes();

    /**
     * Retourne les changements indexés de l'horaire.
     *
     * @return les changements de l'horaire
     */
    Transfers transfers();

    /**
     * Retourne les courses indexées actives à la date donnée.
     *
     * @param date la date de référence
     * @return les courses actives ce jour-là
     */
    Trips tripsFor(LocalDate date);

    /**
     * Retourne les liaisons indexées actives à la date donnée.
     *
     * @param date la date de référence
     * @return les liaisons actives ce jour-là
     */
    Connections connectionsFor(LocalDate date);

    /**
     * Retourne vrai si et seulement si l'index d'arrêt donné est un index de gare.
     *
     * @param stopId l'index d'arrêt
     * @return vrai si l'index correspond à une gare
     */
    default boolean isStationId(int stopId){
        return stopId< stations().size();
    }

    /**
     * Retourne vrai si et seulement si l'index d'arrêt donné est un index de voie ou de quai.
     *
     * @param stopId l'index d'arrêt
     * @return vrai si l'index correspond à une voie ou un quai
     */
    default boolean isPlatformId(int stopId){
        return ((stopId >= stations().size()) && (stopId < stations().size()+ platforms().size()));
    }

    /**
     * Retourne l'index de la gare correspondant à l'arrêt d'index donné.
     *
     * @param stopId l'index d'arrêt
     * @return l'index de la gare associée
     */
    default int stationId(int stopId){
        return isStationId(stopId) ? stopId : platforms().stationId(stopId - stations().size());
    }
    /**
     * Retourne le nom de voie ou de quai de l'arrêt d'index donné, ou null si c'est une gare.
     *
     * @param stopId l'index d'arrêt
     * @return le nom de la voie/quai ou null si c'est une gare
     */
    default String platformName(int stopId) {
        return isPlatformId(stopId) ? platforms().name(stopId - stations().size()) : null;
    }

}
