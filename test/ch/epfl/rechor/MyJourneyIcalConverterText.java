package ch.epfl.rechor;

import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.JourneyIcalConverter;
import ch.epfl.rechor.journey.Stop;
import ch.epfl.rechor.journey.Vehicle;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MyJourneyIcalConverterText {

    private static List<Journey.Leg> exampleLegs() {
        var s1 = new Stop("Ecublens VD, EPFL", null, 6.566141, 46.522196);
        var s2 = new Stop("Renens VD, gare", null, 6.578519, 46.537619);
        var s3 = new Stop("Renens VD", "4", 6.578935, 46.537042);
        var s4 = new Stop("Lausanne", "5", 6.629092, 46.516792);
        var s5 = new Stop("Lausanne", "1", 6.629092, 46.516792);
        var s6 = new Stop("Romont FR", "2", 6.911811, 46.693508);

        var d = LocalDate.of(2025, Month.FEBRUARY, 18);
        var l1 = new Journey.Leg.Transport(
                s1,
                d.atTime(16, 13),
                s2,
                d.atTime(16, 19),
                List.of(),
                Vehicle.METRO,
                "m1",
                "Renens VD, gare");

        var l2 = new Journey.Leg.Foot(s2, d.atTime(16, 19), s3, d.atTime(16, 22));

        var l3 = new Journey.Leg.Transport(
                s3,
                d.atTime(16, 26),
                s4,
                d.atTime(16, 33),
                List.of(),
                Vehicle.TRAIN,
                "R4",
                "Bex");

        var l4 = new Journey.Leg.Foot(s4, d.atTime(16, 33), s5, d.atTime(16, 38));

        var l5 = new Journey.Leg.Transport(
                s5,
                d.atTime(16, 40),
                s6,
                d.atTime(17, 13),
                List.of(),
                Vehicle.TRAIN,
                "IR15",
                "Luzern");

        return List.of(l1, l2, l3, l4, l5);

    }



    @Test
    void journeyIcalConverterConvertsJourneyToIcal() {
        Stop startStop = new Stop("Ecublens VD, EPFL", null, 6.62909, 46.51679);
        Stop endStop = new Stop("Gruyères", null, 7.08333, 46.58333);
        LocalDateTime startTime = LocalDateTime.of(2025, 2, 18, 16, 13);
        LocalDateTime endTime = LocalDateTime.of(2025, 2, 18, 17, 57);

        Journey journey = new Journey(List.of(
                new Journey.Leg.Transport(startStop, startTime, endStop, endTime, List.of(), Vehicle.TRAIN, "IR 15", "Lucerne")
        ));

        String ical = JourneyIcalConverter.toIcalendar(journey);
        assertTrue(ical.contains("BEGIN:VCALENDAR"));
        assertTrue(ical.contains("BEGIN:VEVENT"));
        assertTrue(ical.contains("SUMMARY:Ecublens VD, EPFL → Gruyères"));
        assertTrue(ical.contains("END:VEVENT"));
        assertTrue(ical.contains("END:VCALENDAR"));
    }

    @Test
    void journeyIcalConverterHandlesFootLegsCorrectly() {
        Stop startStop = new Stop("Ecublens VD, EPFL", null, 6.62909, 46.51679);
        Stop endStop = new Stop("Renens VD, gare", null, 6.63333, 46.53333);
        LocalDateTime startTime = LocalDateTime.of(2025, 2, 18, 16, 13);
        LocalDateTime endTime = LocalDateTime.of(2025, 2, 18, 16, 19);

        Journey journey = new Journey(List.of(
                new Journey.Leg.Foot(startStop, startTime, endStop, endTime)
        ));

        String ical = JourneyIcalConverter.toIcalendar(journey);
        assertTrue(ical.contains("DESCRIPTION:trajet à pied (6 min)"));
    }


    @Test
    void journeyIcalConverterHandlesMultipleLegsCorrectly() {
        Stop startStop = new Stop("Ecublens VD, EPFL", null, 6.62909, 46.51679);
        Stop midStop = new Stop("Renens VD, gare", null, 6.63333, 46.53333);
        Stop endStop = new Stop("Gruyères", null, 7.08333, 46.58333);
        LocalDateTime startTime = LocalDateTime.of(2025, 2, 18, 16, 13);
        LocalDateTime midTime = LocalDateTime.of(2025, 2, 18, 16, 19);
        LocalDateTime endTime = LocalDateTime.of(2025, 2, 18, 17, 57);

        Journey journey = new Journey(List.of(
                new Journey.Leg.Transport(startStop, startTime, midStop, midTime, List.of(), Vehicle.TRAIN, "IR 15", "Lucerne"),
                new Journey.Leg.Foot(midStop, midTime, endStop, endTime)
        ));

        String ical = JourneyIcalConverter.toIcalendar(journey);
        assertTrue(ical.contains("DESCRIPTION:16h13 Ecublens VD, EPFL → Renens VD, gare (arr. 16h19)"));

    }
}
