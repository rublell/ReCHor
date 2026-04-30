package ch.epfl.rechor.gui;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.journey.Journey;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record SummaryUI (Node rootNode, ObservableValue<Journey> selectedJourneyO) {
    // Configuration Constants
    private static final String STYLE_SHEET = "summary.css";

    // Layout Constants
    private static final int SPACING = 6;
    private static final int MAIN_PADDING = 10;
    private static final int CELL_PADDING = 6;
    private static final int LINE_MARGIN = 10;
    private static final int CIRCLE_RADIUS = 3;

    private static class JourneyCell extends ListCell<Journey> {
        private final BorderPane mainContainer;

        private final HBox routeHBox;
        private final ImageView vehicleImageView;
        private final Text routeText;

        private final Pane transferPane;

        private final Text depTime;

        private final Text arrTime;

        private final HBox durationHBox;
        private final Text durationText;

        public JourneyCell () {

            vehicleImageView = new ImageView();
            vehicleImageView.setFitWidth(20);
            vehicleImageView.setFitHeight(20);

            routeText = new Text();

            routeHBox = new HBox(vehicleImageView, routeText);
            routeHBox.getStyleClass().add("route");

            depTime = new Text();
            depTime.getStyleClass().add("departure");

            arrTime = new Text();

            durationText = new Text();
            durationHBox = new HBox(durationText);
            durationHBox.getStyleClass().add("duration");

            transferPane = new Pane () {
                @Override
                protected void layoutChildren() {
                    super.layoutChildren();

                    // Calculate vertical center of the pane
                    double centerY = getHeight() / 2.0;

                    // Create the journey line with margins
                    double lineStartX = LINE_MARGIN;
                    double lineEndX = getWidth() - LINE_MARGIN;

                    // Position the circles
                    for (Node n : getChildren()) {
                        if (n instanceof Circle c) {
                            // Get the relative X position (0.0 to 1.0)
                            Double relativeX = (Double) c.getUserData();

                            if (relativeX == null) return;

                            // Map to the actual pixel position, respecting margins
                            double actualX = lineStartX + relativeX * (lineEndX - lineStartX);

                            // Update the circle position
                            c.setCenterX(actualX);
                            c.setCenterY(centerY);
                        } else if (n instanceof Line l) {
                            l.setStartX(lineStartX);
                            l.setStartY(centerY);
                            l.setEndX(lineEndX);
                            l.setEndY(centerY);
                        }
                    }

                }
            };

            transferPane.setPrefHeight(0);
            transferPane.setPrefWidth(0);

            mainContainer = new BorderPane();
            mainContainer.setTop(routeHBox);
            mainContainer.setRight(arrTime);
            mainContainer.setBottom(durationHBox);
            mainContainer.setLeft(depTime);
            mainContainer.setCenter(transferPane);
            mainContainer.setPadding(new Insets(CELL_PADDING));
            mainContainer.getStyleClass().add("journey");
        }

        @Override
        protected void updateItem(Journey journey, boolean empty) {
            super.updateItem(journey, empty);

            if (empty || journey == null) {
                setGraphic(null);
                setText(null);
            } else {
                transferPane.getChildren().clear();

                Line journeyLine = new Line();
                transferPane.getChildren().add(journeyLine);

                Circle depCircle = new Circle(CIRCLE_RADIUS);
                Circle arrCircle = new Circle(CIRCLE_RADIUS);

                depCircle.getStyleClass().add("dep-arr");
                arrCircle.getStyleClass().add("dep-arr");

                // Relative positions of the circles
                depCircle.setUserData(0d);
                arrCircle.setUserData(1d);


                transferPane.getChildren().add(depCircle);
                transferPane.getChildren().add(arrCircle);

                Journey.Leg.Transport firstTransportLeg = null;
                for (Journey.Leg leg: journey.legs()) {
                    if (Objects.requireNonNull(leg) instanceof Journey.Leg.Transport t && firstTransportLeg == null) {
                        firstTransportLeg = t;
                    } else if (Objects.requireNonNull(leg) instanceof Journey.Leg.Foot f) {
                        Double relativePosition = (double) Duration.between(journey.depTime(), f.depTime())
                                .toNanos() / journey.duration().toNanos();
                        Circle transferCircle = new Circle(CIRCLE_RADIUS);
                        transferCircle.getStyleClass().add("transfer");
                        transferCircle.setUserData(relativePosition);
                        transferPane.getChildren().add(transferCircle);
                    }
                }

                if (firstTransportLeg != null) {
                    routeText.setText(FormatterFr.formatRouteDestination(firstTransportLeg));
                    vehicleImageView.setImage(VehicleIcons.iconFor(firstTransportLeg.vehicle()));
                }

                depTime.setText(FormatterFr.formatTime(journey.depTime()));
                arrTime.setText(FormatterFr.formatTime(journey.arrTime()));

                durationText.setText(FormatterFr.formatDuration(journey.duration()));

                setGraphic(mainContainer);
            }
        }
    }

    public static SummaryUI create (ObservableValue<List<Journey>> journeys, ObservableValue<LocalTime> desiredTime) {
        ListView<Journey> rootView = createRootListView(journeys, desiredTime);
        rootView.setCellFactory(p -> new JourneyCell());

        ObjectProperty<Journey> selectedJourneyProperty = new SimpleObjectProperty<>();
        // Bind the selected journey property to the ListView selection
        rootView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedJourneyProperty.set(newVal);
        });

        return new SummaryUI(rootView, selectedJourneyProperty);
    }

    private static void selectFromDesiredTime(ListView<Journey> rootView,
                                              List<Journey> journeys,
                                              LocalTime desiredTime) {
        // Détermine l’index du trajet à sélectionner
        int idxToSelect = -1;
        for (int i = 0; i < journeys.size(); i++) {
            if (journeys.get(i).depTime().toLocalTime().isAfter(desiredTime)) {
                idxToSelect = i;
                break;
            }
        }
        // Si aucun voyage n'est après l'heure désirée, on prend le dernier
        if (idxToSelect == -1 && !journeys.isEmpty()) {
            idxToSelect = journeys.size() - 1;
        }

        if (idxToSelect >= 0) {
            // Récupère et sélectionne le meilleur trajet
            Journey selectedJourney = journeys.get(idxToSelect);
            rootView.getSelectionModel().select(selectedJourney);
            // Assure que le trajet sélectionné est rendu visible dans la liste
            Platform.runLater(() -> rootView.scrollTo(selectedJourney));
        }
    }



    private static ListView<Journey> createRootListView (ObservableValue<List<Journey>> journeys, ObservableValue<LocalTime> desiredTime) {
        ObservableList<Journey> journeyItems = FXCollections.observableArrayList();

        // Add values from the given journeys
        ListView<Journey> rootView = new ListView<Journey>(journeyItems);
        if (journeys.getValue() != null) {
            journeyItems.addAll(journeys.getValue());
        }

        // Select the right journey using the desired time
        selectFromDesiredTime(rootView, journeys.getValue(), desiredTime.getValue());

        // Listen to potential journey changes
        journeys.addListener((o, oV, nV) -> {
            journeyItems.clear();
            journeyItems.addAll(nV);
            selectFromDesiredTime(rootView, nV, desiredTime.getValue());
        });

        // Listen to potential desired time changes
        desiredTime.addListener((o, oV, nV) -> {
            selectFromDesiredTime(rootView, journeys.getValue(), nV);
        });

        rootView.getStylesheets().add(STYLE_SHEET);
        rootView.setId("detail");
        return rootView;
    }

}