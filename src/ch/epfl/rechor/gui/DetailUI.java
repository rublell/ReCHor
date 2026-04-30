package ch.epfl.rechor.gui;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.journey.*;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record DetailUI(Node rootNode) {

    private record CirclePair(Circle from, Circle to) {}

    public static DetailUI create(ObservableValue<Journey> journeyOV) {
        VBox rootContent = new VBox();
        StackPane stack = new StackPane();
        VBox noJourneyBox = new VBox(new Text("Aucun voyage"));
        noJourneyBox.setId("no-journey");
        stack.getChildren().add(noJourneyBox);

        Pane annotations = new Pane();
        annotations.setId("annotations");
        annotations.setMouseTransparent(true); // Correction ici pour permettre les clics sur les TitledPane

        StepsPane steps = new StepsPane(annotations);
        StackPane stepsStack = new StackPane(steps, annotations);

        HBox buttons = new HBox();
        buttons.setSpacing(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(10));
        buttons.setId("buttons");

        rootContent.getChildren().addAll(stepsStack, buttons);
        stack.getChildren().add(rootContent);

        ScrollPane scroll = new ScrollPane(stack);
        scroll.setId("detail");
        scroll.getStylesheets().add("detail.css");

        journeyOV.addListener((obs, oldJ, newJ) -> refresh(newJ, noJourneyBox, steps, buttons));
        refresh(journeyOV.getValue(), noJourneyBox, steps, buttons);

        return new DetailUI(scroll);
    }

    private static void refresh(Journey j, VBox noJourney, StepsPane stepsPane, HBox buttons) {
        if (j == null) {
            noJourney.setVisible(true);
            stepsPane.clear();
            buttons.getChildren().clear();
        } else {
            noJourney.setVisible(false);
            stepsPane.buildFor(j);
            buttons.getChildren().setAll(
                    createMapButton(j),
                    createCalendarButton(j, stepsPane)
            );
        }
    }

    private static Button createMapButton(Journey j) {
        Button map = new Button("Carte");
        map.setOnAction(e -> openMap(j));
        return map;
    }

    private static Button createCalendarButton(Journey j, Node root) {
        Button ics = new Button("Calendrier");
        ics.setOnAction(e -> exportIcs(j, root));
        return ics;
    }

    private static void openMap(Journey j) {
        try {
            String geoJson = JourneyGeoJsonConverter.toGeoJson(j).toString();
            URI uri = new URI("https", "umap.osm.ch", "/fr/map", "data=" + geoJson, null);
            Desktop.getDesktop().browse(uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void exportIcs(Journey j, Node owner) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Exporter l'événement iCalendar");
        fc.setInitialFileName("voyage_" + j.depTime().toLocalDate().format(DateTimeFormatter.ISO_DATE) + ".ics");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers iCalendar", "*.ics"));
        Path file = Optional.ofNullable(fc.showSaveDialog(owner.getScene().getWindow())).map(File::toPath).orElse(null);
        if (file != null) {
            try {
                Files.writeString(file, JourneyIcalConverter.toIcalendar(j));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static final class StepsPane extends GridPane {
        private final List<CirclePair> links = new ArrayList<>();
        private final Pane annotations;

        StepsPane(Pane annotations) {
            this.annotations = annotations;
            setHgap(5);
            setVgap(3);
            for (int i = 0; i < 4; ++i)
                getColumnConstraints().add(new ColumnConstraints());
            setId("legs");
        }

        void buildFor(Journey j) {
            clear();
            int row = 0;
            for (Journey.Leg leg : j.legs()) {
                row = switch (leg) {
                    case Journey.Leg.Foot f -> addFoot(f, row);
                    case Journey.Leg.Transport t -> addTransport(t, row);
                    default -> throw new IllegalStateException("Unexpected value: " + leg);
                };
            }
        }

        void clear() {
            getChildren().clear();
            annotations.getChildren().clear();
            links.clear();
        }

        private int addFoot(Journey.Leg.Foot f, int row) {
            add(new Text(FormatterFr.formatLeg(f)), 2, row, 2, 1);
            return row + 1;
        }

        private int addTransport(Journey.Leg.Transport t, int row) {
            addText(FormatterFr.formatTime(t.depTime()), 0, row, "departure");
            Circle depC = circle();
            add(depC, 1, row);
            add(new Text(t.depStop().name()), 2, row);
            addText(FormatterFr.formatPlatformName(t.depStop()), 3, row, "departure");

            ImageView icon = new ImageView(VehicleIcons.iconFor(t.vehicle()));
            icon.setFitHeight(31);
            icon.setFitWidth(31);
            setValignment(icon, VPos.TOP);
            add(icon, 0, row + 1);
            add(new Text(FormatterFr.formatRouteDestination(t)), 2, row + 1, 2, 1);

            int extra = 0;
            if (!t.intermediateStops().isEmpty()) {
                GridPane g = new GridPane();
                g.setHgap(5);
                g.setVgap(2);
                g.getStyleClass().add("intermediate-stops");
                int r = 0;
                for (Journey.Leg.IntermediateStop s : t.intermediateStops()) {
                    g.add(new Text(FormatterFr.formatTime(s.arrTime())), 0, r);
                    g.add(new Text(FormatterFr.formatTime(s.depTime())), 1, r);
                    g.add(new Text(s.stop().name()), 2, r);
                    ++r;
                }
                TitledPane tp = new TitledPane(r + " arrêt" + (r > 1 ? "s" : "") + ", " + FormatterFr.formatDuration(t.duration()), g);
                tp.setExpanded(false);
                add(tp, 2, row + 2, 2, 1);
                GridPane.setRowSpan(icon, 2);
                extra = 1;
            }

            int arrRow = row + 2 + extra;
            addText(FormatterFr.formatTime(t.arrTime()), 0, arrRow, null);
            Circle arrC = circle();
            add(arrC, 1, arrRow);
            add(new Text(t.arrStop().name()), 2, arrRow);
            add(new Text(FormatterFr.formatPlatformName(t.arrStop())), 3, arrRow);

            links.add(new CirclePair(depC, arrC));
            return arrRow + 1;
        }

        private static Circle circle() {
            return new Circle(3, Color.BLACK);
        }

        private void addText(String text, int col, int row, String css) {
            Text t = new Text(text);
            if (css != null) t.getStyleClass().add(css);
            add(t, col, row);
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            annotations.getChildren().clear();
            for (CirclePair c : links) {
                double x1 = c.from.getBoundsInParent().getCenterX();
                double y1 = c.from.getBoundsInParent().getCenterY();
                double x2 = c.to.getBoundsInParent().getCenterX();
                double y2 = c.to.getBoundsInParent().getCenterY();
                Line l = new Line(x1, y1, x2, y2);
                l.setStroke(Color.RED);
                l.setStrokeWidth(2);
                annotations.getChildren().add(l);
            }
        }
    }
}
