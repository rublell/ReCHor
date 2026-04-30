package ch.epfl.rechor.app;

import ch.epfl.rechor.journey.Router;
import ch.epfl.rechor.journey.Profile;
import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.JourneyExtractor;
import ch.epfl.rechor.journey.JourneyIcalConverter;
import ch.epfl.rechor.timetable.CachedTimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

public class TestIcal {
    static int stationId(Stations stations, String name) {
        for (int i = 0; i < stations.size(); i++) {
            if (stations.name(i).equals(name)) return i;
        }
        throw new IllegalArgumentException("Station inconnue: " + name);
    }

    public static void main(String[] args) throws IOException {
        long t0 = System.nanoTime();

        // 1) Charge la timetable depuis ton dossier « timetable »
        TimeTable tt = new CachedTimeTable(
                FileTimeTable.in(Path.of("timetables/timetable-03-17"))
        );

        // 2) Récupère tes stations et choisis date / départ / arrivée
        Stations stations = tt.stations();
        LocalDate date     = LocalDate.of(2025, Month.MARCH, 18);
        int depId = stationId(stations, "Ecublens VD, EPFL");
        int arrId = stationId(stations, "Gruyères");
        System.out.println("arrId = " + arrId+ ", stations.size() = " + stations.size());


        // 3) Calcule le profil et extrait le 33e voyage (index 32)
        Router router   = new Router(tt);
        Profile profile = router.profile(date, arrId);
        Journey journey = JourneyExtractor.journeys(profile, depId).get(32);

        // 4) Affiche l’iCal et le temps de calcul
        System.out.println(JourneyIcalConverter.toIcalendar(journey));
        double secs = (System.nanoTime() - t0) * 1e-9;
        System.out.printf("Temps: %.2f s%n", secs);
    }
}
