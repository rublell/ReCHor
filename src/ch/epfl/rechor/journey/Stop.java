package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

import java.util.Objects;

/**
 * * @author Valentin Walendy (393413)
 *  * @author Ruben Lellouche (400288)
 * Cette classe représente un arrêt de transport (par exemple, une gare ou un arrêt de bus).
 * Un arrêt est défini par un nom, un nom de plateforme, une longitude et une latitude.
 */
public record Stop(String name, String platformName, double longitude, double latitude) {

    /**
     * Constructeur de la classe Stop.
     * Vérifie que le nom de l'arrêt est non nul et que les coordonnées géographiques sont valides.
     * - La longitude doit être comprise entre -180 et 180 degrés.
     * - La latitude doit être comprise entre -90 et 90 degrés.
     *
     * @param name Nom de l'arrêt (par exemple, "Paris Gare du Nord").
     * @param platformName Nom de la plateforme ou du quai (peut être nul).
     * @param longitude La longitude de l'arrêt (comprise entre -180 et 180).
     * @param latitude La latitude de l'arrêt (comprise entre -90 et 90).
     * @throws NullPointerException Si le nom de l'arrêt est nul.
     * @throws IllegalArgumentException Si la longitude ou la latitude sont en dehors des plages autorisées.
     */
    public Stop {

        Objects.requireNonNull(name);
        Objects.requireNonNull(longitude);
        Objects.requireNonNull(latitude);



        Preconditions.checkArgument(longitude <= 180.0 && longitude >= -180.0);

        Preconditions.checkArgument(latitude <= 90.0 && latitude >= -90.0);
    }
}
