package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import ch.epfl.rechor.journey.*;
import ch.epfl.rechor.timetable.*;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public final class Main extends Application {

    private ObservableValue<List<Journey>> journeysO;

    @Override
    public void start(Stage stage) throws Exception {
        // 1. Chargement des données horaires
        TimeTable tt = new CachedTimeTable(FileTimeTable.in(Path.of("timetables/timetable-05-05")));

        // 2. Création du StopIndex avec noms principaux + alias
        Stations stations = tt.stations();
        StationAliases aliases = tt.stationAliases();

        Set<String> allNames = new HashSet<>();
        List<String> primaryNames = new ArrayList<>();
        Map<String, String> alternateNames = new HashMap<>();

        for (int i = 0; i < stations.size(); i++) {
            String stationName = stations.name(i);
            primaryNames.add(stationName);
            allNames.add(stationName);
        }

        for (int i = 0; i < aliases.size(); i++) {
            String alias = aliases.alias(i);
            String stationName = aliases.stationName(i);
            if (!allNames.contains(alias)) {
                alternateNames.put(alias, stationName);
            }
        }

        StopIndex stopIndex = new StopIndex(primaryNames, alternateNames);

        // 3. Création de l'interface de requête
        QueryUI queryUI = QueryUI.create(stopIndex);

        // 4. Propriété contenant le profil mis en cache
        ObjectProperty<Profile> profileCache = new SimpleObjectProperty<>();

        // 5. Création de la valeur observable des voyages
        journeysO = Bindings.createObjectBinding(() -> {
            String dep = queryUI.depStopO().getValue();
            String arr = queryUI.arrStopO().getValue();
            LocalDate date = queryUI.dateO().getValue();

            if (dep.isEmpty() || arr.isEmpty()) {
                return List.of();
            }

            int depId = stationIdFromName(stations, dep);
            int arrId = stationIdFromName(stations, arr);
            if (depId == -1 || arrId == -1) return List.of();

            Profile profile = profileCache.get();
            if (profile == null || !profile.date().equals(date) || profile.arrStationId() != arrId) {
                profile = new Router(tt).profile(date, arrId);
                profileCache.set(profile);
            }

            return JourneyExtractor.journeys(profile, depId);
        }, queryUI.depStopO(), queryUI.arrStopO(), queryUI.dateO());

        // 6. Création des interfaces Summary et Detail
        ObservableValue<LocalTime> timeO = queryUI.timeO();
        SummaryUI summaryUI = SummaryUI.create(journeysO, timeO);
        DetailUI detailUI = DetailUI.create(summaryUI.selectedJourneyO());

        // 7. Organisation de l’interface
        SplitPane centerPane = new SplitPane(summaryUI.rootNode(), detailUI.rootNode());
        BorderPane root = new BorderPane(centerPane);
        root.setTop(queryUI.rootNode());

        Scene scene = new Scene(root, 1000, 700);
        stage.setScene(scene);
        stage.setTitle("ReCHor");
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();

        // 8. Focus initial sur le champ de départ
        Platform.runLater(() -> scene.lookup("#depStop").requestFocus());
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static int stationIdFromName(Stations stations, String name) {
        for (int i = 0; i < stations.size(); i++) {
            if (stations.name(i).equals(name)) return i;
        }
        return -1;
    }
}
