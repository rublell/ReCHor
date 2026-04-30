package ch.epfl.rechor.timetable;

/**
 *  * * @author Valentin Walendy (393413)
 *  * * @author Ruben Lellouche (400288)
 * Interface représentant des courses de transport public indexées.
 */
public interface Trips extends Indexed{


    /**
     * Retourne l'index de la ligne à laquelle appartient la course d'index donné.
     *
     * @param id l'index de la course
     * @return l'index de la ligne correspondante
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int routeId (int id);

    /**
     * Retourne le nom de la destination finale de la course d'index donné.
     *
     * @param id l'index de la course
     * @return le nom de la destination finale
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    String destination (int id);
}
