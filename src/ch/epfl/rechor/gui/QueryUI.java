package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.util.converter.LocalTimeStringConverter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public record QueryUI(Node rootNode,
                      ObservableValue<String> depStopO,
                      ObservableValue<String> arrStopO,
                      ObservableValue<LocalDate> dateO,
                      ObservableValue<LocalTime> timeO) {

    public static QueryUI create(StopIndex index) {
        // Création des StopFields
        StopField depField = StopField.create(index);
        StopField arrField = StopField.create(index);

        depField.textField().setId("depStop");

        // Définir les invites (prompts)
        depField.textField().setPromptText("Nom de l'arrêt de départ");
        arrField.textField().setPromptText("Nom de l'arrêt d'arrivée");

        // Bouton d'échange
        Button swapButton = new Button("⟲");

        swapButton.setOnAction(e -> {
            String tmp = depField.stopO().getValue();
            depField.setTo(arrField.stopO().getValue());
            arrField.setTo(tmp);
        });

        // DatePicker initialisé à la date actuelle
        DatePicker datePicker = new DatePicker(LocalDate.now());

        // TextField + TextFormatter pour l’heure
        TextField timeField = new TextField();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTimeStringConverter converter =
                new LocalTimeStringConverter(formatter, DateTimeFormatter.ofPattern("H:mm"));
        TextFormatter<LocalTime> timeFormatter = new TextFormatter<>(converter, LocalTime.now());
        timeField.setTextFormatter(timeFormatter);

        // Organisation de l'interface
        HBox stopsBox = new HBox(10,
                new Label("Départ\u202f:"), depField.textField(),
                swapButton,
                new Label("Arrivée\u202f:"), arrField.textField());
        HBox dateTimeBox = new HBox(10,
                new Label("Date\u202f:"), datePicker,
                new Label("Heure\u202f:"), timeField);

        VBox root = new VBox(10, stopsBox, dateTimeBox);
        root.setPadding(new Insets(10));
        root.getStylesheets().add("query.css");

        return new QueryUI(root,
                depField.stopO(),
                arrField.stopO(),
                datePicker.valueProperty(),
                timeFormatter.valueProperty());
    }
}
