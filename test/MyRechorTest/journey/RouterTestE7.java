package MyRechorTest.journey;

import ch.epfl.rechor.journey.*;
import ch.epfl.rechor.timetable.CachedTimeTable;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;

public class RouterTestE7 {
    /**
     * Parcourt la liste des gares et renvoie l’indice dont le nom correspond.
     * Lance IllegalArgumentException si la gare n’est pas trouvée.
     */
    static int stationId(Stations stations, String stationName) {
        for (int i = 0; i < stations.size(); i++) {
            if (stations.name(i).equals(stationName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Gare inconnue: " + stationName);
    }

    public static void main(String[] args) throws IOException {
        long tStart = System.nanoTime();

        // On charge la TimeTable et on y ajoute un cache pour accélérer les accès
        TimeTable timeTable =
                new CachedTimeTable(ch.epfl.rechor.timetable.mapped.FileTimeTable.in(Path.of("timetables/timetable-03-31")));
        Stations stations = timeTable.stations();

        // Date du 1er avril 2025
        LocalDate date = LocalDate.of(2025, Month.APRIL, 1);

        // Recherche des indices des gares de départ et d'arrivée
        int depStationId = stationId(stations, "Ecublens VD, EPFL");
        int arrStationId = stationId(stations, "Gruyères");

        // Construction du routeur et du profil jusqu'à la gare d'arrivée
        Router router = new Router(timeTable);
        Profile profile = router.profile(date, arrStationId);

        // Extraction du 33e trajet (index 32) depuis Ecublens VD, EPFL
        Journey journey = JourneyExtractor
                .journeys(profile, depStationId)
                .get(10);

        // Conversion en iCalendar et affichage
        System.out.println(JourneyIcalConverter.toIcalendar(journey));

        // Mesure et affichage du temps écoulé
        double elapsed = (System.nanoTime() - tStart) * 1e-9;
        System.out.printf("Temps écoulé : %.3f s%n", elapsed);
    }
}
