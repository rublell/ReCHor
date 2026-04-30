package ch.epfl.rechor.timetable;
/**
 * * * @author Valentin Walendy (393413)
 *  * * @author Ruben Lellouche (400288)
 * Interface représentant des noms alternatifs de gares indexés.
 */

public interface StationAliases extends Indexed{


    /**
     * Retourne le nom alternatif à l'index donné.
     *
     * @param id l'index du nom alternatif
     * @return le nom alternatif
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    String alias(int id);


    /**
     * Retourne le nom de la gare correspondant au nom alternatif d'index donné.
     *
     * @param id l'index du nom alternatif
     * @return le nom de la gare correspondante
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    String stationName(int id);
}
