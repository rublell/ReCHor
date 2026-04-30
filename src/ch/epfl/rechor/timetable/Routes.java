package ch.epfl.rechor.timetable;

import ch.epfl.rechor.journey.Vehicle;


/**
 *  * * @author Valentin Walendy (393413)
 *  * * @author Ruben Lellouche (400288)
 * Interface représentant des lignes de transport public indexées.
 */
public interface Routes extends Indexed{

    /**
     * Retourne le type de véhicule desservant la ligne d'index donné.
     *
     * @param id l'index de la ligne
     * @return le type de véhicule desservant la ligne
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    Vehicle vehicle (int id);


    /**
     * Retourne le nom de la ligne d'index donné.
     *
     * @param id l'index de la ligne
     * @return le nom de la ligne (p. ex. IR 15)
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    String name (int id);
}
