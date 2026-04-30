package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.Vehicle;
import javafx.scene.image.Image;

import java.util.EnumMap;
import java.util.Map;

public final class VehicleIcons {
    private static final Map<Vehicle, Image> CACHE =
            new EnumMap<>(Vehicle.class);


    /**  Retourne *toujours la même* icône pour le véhicule donné. */
    public static Image iconFor(Vehicle v) {
        return CACHE.computeIfAbsent(v,
                veh -> new Image(veh.name() + ".png"));
    }
    // on a mit la mthode static pour éviter que son appel soit souligné, bon ??
}
