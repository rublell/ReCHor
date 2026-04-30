package ch.epfl.rechor.journey;

import java.util.List;

/**
 * * @author Valentin Walendy (393413)
 *  * @author Ruben Lellouche (400288)
 * Cette énumération représente les différents types de véhicules utilisés dans un trajet.
 * Chaque type de véhicule correspond à un mode de transport spécifique.
 */

public enum Vehicle {
    TRAM,
    METRO,
    TRAIN,
    BUS,
    FERRY,
    AERIAL_LIFT,
    FUNICULAR;


    /**
     * Liste contenant tous les types de véhicules disponibles.
     * Cette liste contient toutes les valeurs de l'énumération dans l'ordre de leur déclaration.
     */

    public static final List<Vehicle> ALL = List.of(values());
}
