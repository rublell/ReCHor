package MyRechorTest.journey;
import ch.epfl.rechor.gui.DetailUI;
import ch.epfl.rechor.timetable.CachedTimeTable;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.JourneyExtractor;
import ch.epfl.rechor.journey.Profile;
import ch.epfl.rechor.journey.Router;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;


public class JavaFxTestE9 extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    static int stationId(Stations stations, String stationName) {
        for (int i = 0, n = stations.size(); i < n; i++) {
            if (stations.name(i).equals(stationName)) {
                return i;
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        TimeTable timeTable = new CachedTimeTable(
                FileTimeTable.in(Path.of("/Users/valentinwalendy/Downloads/rechor 2/timetables/timetable-05-12")));
        Stations stations = timeTable.stations();
        LocalDate date = LocalDate.of(2025, Month.MAY, 16);
        int depStationId = stationId(stations, "Joeuf");
        int arrStationId = stationId(stations, "Bulle, Pierre-Alex");
        Router router = new Router(timeTable);
        Profile profile = router.profile(date, arrStationId);
        Journey journey = JourneyExtractor
                .journeys(profile, depStationId)
                .get(6);

        ObservableValue<Journey> journeyO =
                new SimpleObjectProperty<>(journey);
        DetailUI detailUI = DetailUI.create(journeyO);
        Pane root = new BorderPane(detailUI.rootNode());

        primaryStage.setScene(new Scene(root));
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }
}