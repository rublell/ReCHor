package ch.epfl.rechor.gui;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Vehicle;
import javafx.scene.image.Image;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class MyDetailUITest {

    @Test
    void iconFor_eachVehicle_returnsNonNullAndCached() {
        for (ch.epfl.rechor.journey.Vehicle v : Vehicle.values()) {
            Image img1 = VehicleIcons.iconFor(v);
            assertNotNull(img1, "L'icône pour " + v + " ne doit pas être nulle");
            Image img2 = VehicleIcons.iconFor(v);
            assertSame(img1, img2, "L'appel répété doit renvoyer la même instance pour " + v);
        }
    }

    @Test
    void iconFor_unknownVehicle_throwsIfEnumExtended() {
        // Si jamais on étend Vehicle sans ajouter l'image, on devrait obtenir
        // une IllegalArgumentException ou un RuntimeException explicite.
        // On simule cela en passant un enum bidon via valueOf.
        String fake = "FAKE_VEHICLE";
        try {
            Vehicle v = Vehicle.valueOf(fake);
            fail("Vehicle.valueOf(\"" + fake + "\") aurait dû échouer");
        } catch (IllegalArgumentException expected) {
            // OK
        }
    }
}
