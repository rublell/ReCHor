package ch.epfl.rechor.timetable;

import java.util.NoSuchElementException;

/**
 *  * * @author Valentin Walendy (393413)
 *  * * @author Ruben Lellouche (400288)
 * Interface représentant des changements indexés entre gares.
 */
public interface Transfers extends Indexed{

    /**
     * Retourne l'index de la gare de départ du changement d'index donné.
     *
     * @param id l'index du changement
     * @return l'index de la gare de départ
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int depStationId(int id);

    /**
     * Retourne la durée, en minutes, du changement d'index donné.
     *
     * @param id l'index du changement
     * @return la durée du changement en minutes
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int minutes(int id);

    /**
     * Retourne l'intervalle empaqueté des index des changements dont la gare d'arrivée est celle d'index donné.
     *
     * @param stationId l'index de la gare d'arrivée
     * @return l'intervalle empaqueté des index des changements
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int arrivingAt(int stationId);

    /**
     * Retourne la durée, en minutes, du changement entre les deux gares d'index donnés.
     *
     * @param depStationId l'index de la gare de départ
     * @param arrStationId l'index de la gare d'arrivée
     * @return la durée du changement en minutes
     * @throws IndexOutOfBoundsException si l'un des index est invalide
     * @throws NoSuchElementException si aucun changement n'est possible entre ces deux gares
     */
    int minutesBetween(int depStationId, int arrStationId);
}
