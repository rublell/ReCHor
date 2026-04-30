package ch.epfl.rechor.timetable;

/**
 *  * * @author Valentin Walendy (393413)
 *  * * @author Ruben Lellouche (400288)
 * Interface représentant des liaisons indexées dans un réseau de transport public.
 * Les liaisons sont ordonnées par heure de départ décroissante.
 */
public interface Connections extends Indexed{
    /**
     * Retourne l'index de l'arrêt de départ de la liaison d'index donné.
     *
     * @param id l'index de la liaison
     * @return l'index de l'arrêt de départ
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int depStopId(int id);

    /**
     * Retourne l'heure de départ de la liaison d'index donné, en minutes après minuit.
     *
     * @param id l'index de la liaison
     * @return l'heure de départ en minutes après minuit
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int depMins(int id);

    /**
     * Retourne l'index de l'arrêt d'arrivée de la liaison d'index donné.
     *
     * @param id l'index de la liaison
     * @return l'index de l'arrêt d'arrivée
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int arrStopId(int id);

    /**
     * Retourne l'heure d'arrivée de la liaison d'index donné, en minutes après minuit.
     *
     * @param id l'index de la liaison
     * @return l'heure d'arrivée en minutes après minuit
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int arrMins(int id);

    /**
     * Retourne l'index de la course à laquelle appartient la liaison d'index donné.
     *
     * @param id l'index de la liaison
     * @return l'index de la course correspondante
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int tripId(int id);

    /**
     * Retourne la position de la liaison d'index donné dans la course à laquelle elle appartient.
     * La première liaison d'une course a l'index 0.
     *
     * @param id l'index de la liaison
     * @return la position de la liaison dans la course
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int tripPos(int id);

    /**
     * Retourne l'index de la liaison suivante dans la course à laquelle appartient celle d'index donné.
     * Si la liaison est la dernière de la course, retourne l'index de la première liaison de la course.
     *
     * @param id l'index de la liaison
     * @return l'index de la liaison suivante
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int nextConnectionId(int id);
}
