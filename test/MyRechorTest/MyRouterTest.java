package MyRechorTest;

import ch.epfl.rechor.journey.*;
import ch.epfl.rechor.timetable.CachedTimeTable;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.NoSuchElementException;

public class MyRouterTest {
    private static int stationId(Stations stations, String name) {
        for (var i = 0; i < stations.size(); i += 1)
            if (stations.name(i).equals(name)) return i;
        throw new NoSuchElementException();
    }

    @Test
    public void profileExtractionTest () throws IOException {
        long tStart = System.nanoTime();

        TimeTable timeTable =
                new CachedTimeTable(FileTimeTable.in(Path.of("timetables/timetable-05-12")));
        Stations stations = timeTable.stations();
        LocalDate date = LocalDate.of(2025, Month.MAY, 15);
        int depStationId = stationId(stations, "Joeuf");
        int arrStationId = stationId(stations, "Bulle, Pierre-Alex");
        Router router = new Router(timeTable);
        Profile profile = router.profile(date, arrStationId);
        Journey journey = JourneyExtractor
                .journeys(profile, depStationId)
                .get(32);
        System.out.println(JourneyIcalConverter.toIcalendar(journey));

        double elapsed = (System.nanoTime() - tStart) * 1e-9;
        System.out.printf("Temps écoulé : %.3f s%n", elapsed);
    }
}
