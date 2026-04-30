package ch.epfl.rechor.timetable;

/**
 *  * * @author Valentin Walendy (393413)
 *  * * @author Ruben Lellouche (400288)
 */
public interface Stations extends Indexed{

    /**
     * Retourne le nom de la gare à l'index donné.
     *
     * @param id l'index de la gare
     * @return le nom de la gare
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    String name (int id);

    /**
     * Retourne la longitude de la gare à l'index donné.
     *
     * @param id l'index de la gare
     * @return la longitude en degrés
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    double longitude(int id);


    /**
     * Retourne la latitude de la gare à l'index donné.
     *
     * @param id l'index de la gare
     * @return la latitude en degrés
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    double latitude(int id);
}
