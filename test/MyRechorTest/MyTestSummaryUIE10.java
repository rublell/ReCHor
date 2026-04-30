package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.*;
import ch.epfl.rechor.timetable.CachedTimeTable;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;

public final class MyTestSummaryUIE10 extends Application {

    private static int stationId(Stations stations, String name) {
        for (int i = 0; i < stations.size(); i++) {
            if (stations.name(i).equals(name)) return i;
        }
        throw new IllegalArgumentException("Station inconnue: " + name);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger la timetable
        TimeTable tt = new CachedTimeTable(
                FileTimeTable.in(Path.of("timetables/timetable-05-12"))
        );
        Stations stations = tt.stations();

        // Date et identifiants de gares
        LocalDate date = LocalDate.of(2025, Month.MAY, 16);
        int depStationId = stationId(stations, "Joeuf");
        int arrStationId = stationId(stations, "Bulle, Pierre-Alex");

        // Calcul du profil
        Router router = new Router(tt);
        Profile profile = router.profile(date, arrStationId);

        // Extraction des voyages
        List<Journey> journeys = JourneyExtractor.journeys(profile, depStationId);
        ObservableValue<List<Journey>> journeysO = new SimpleObjectProperty<>(journeys);
        ObservableValue<LocalTime> depTimeO = new SimpleObjectProperty<>(LocalTime.of(16, 0));

        // Création de la vue SummaryUI
        SummaryUI summaryUI = SummaryUI.create(journeysO, depTimeO);
        Pane root = new BorderPane(summaryUI.rootNode());

        // Création de la scène
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Test SummaryUI");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
