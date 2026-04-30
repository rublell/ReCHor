package ch.epfl.rechor.timetable;

/**
 *  * * @author Valentin Walendy (393413)
 *  * * @author Ruben Lellouche (400288)
 * Interface représentant des voies/quais indexés.
 */
public interface Platforms extends Indexed{


    /**
     * Retourne le nom de la voie ou du quai à l'index donné.
     *
     * @param id l'index de la voie/quai
     * @return le nom de la voie/quai (peut être vide)
     * @throws IndexOutOfBoundsException si l'index est invalide
     */

    String name(int id);


    /**
     * Retourne l'index de la gare à laquelle appartient la voie ou le quai.
     *
     * @param id l'index de la voie/quai
     * @return l'index de la gare correspondante
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int stationId(int id);
}
