package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Popup;

public record StopField(TextField textField, ObservableValue<String> stopO) {

    public static StopField create(StopIndex stopIndex) {
        TextField textField = new TextField();
        //j suis pas sur
        textField.setId("depStop");

        SimpleStringProperty selectedStop = new SimpleStringProperty("");

        ObservableList<String> results = FXCollections.observableArrayList();
        ListView<String> resultList = new ListView<>(results);
        resultList.setMaxHeight(240);
        resultList.setFocusTraversable(false);

        Popup popup = new Popup();
        popup.setHideOnEscape(false);
        popup.getContent().add(resultList);

        // Flèches ↑ ↓ pour naviguer dans la liste
        textField.addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.DOWN) {
                resultList.getSelectionModel().selectNext();
                resultList.scrollTo(resultList.getSelectionModel().getSelectedIndex());
                e.consume();
            } else if (e.getCode() == KeyCode.UP) {
                resultList.getSelectionModel().selectPrevious();
                resultList.scrollTo(resultList.getSelectionModel().getSelectedIndex());
                e.consume();
            }
        });

        // Clic avec la souris sur un résultat
        resultList.setOnMouseClicked(e -> {
            String selected = resultList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                textField.setText(selected);
                selectedStop.set(selected);
                popup.hide();
                textField.getParent().requestFocus(); // Pour déclencher perte de focus
            }
        });

        // Listener de texte (mise à jour des résultats)
        final var textListener = (javafx.beans.value.ChangeListener<String>) (obs, oldText, newText) -> {
            results.setAll(stopIndex.stopsMatching(newText, 30));
            if (!results.isEmpty()) {
                resultList.getSelectionModel().selectFirst();
            }
        };

        // Listener de position (pour popup sous le champ)
        final var boundsListener = (javafx.beans.value.ChangeListener<Bounds>) (obs, oldBounds, newBounds) -> {
            Bounds screenBounds = textField.localToScreen(newBounds);
            if (screenBounds != null) {
                popup.setAnchorX(screenBounds.getMinX());
                popup.setAnchorY(screenBounds.getMaxY());
            }
        };

        // Focus listener
        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                results.setAll(stopIndex.stopsMatching(textField.getText(), 30));
                if (!results.isEmpty()) {
                    resultList.getSelectionModel().selectFirst();
                }
                popup.show(textField.getScene().getWindow());
                textField.textProperty().addListener(textListener);
                textField.boundsInLocalProperty().addListener(boundsListener);
            } else {
                popup.hide();
                textField.textProperty().removeListener(textListener);
                textField.boundsInLocalProperty().removeListener(boundsListener);

                String selected = resultList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    textField.setText(selected);
                    selectedStop.set(selected);
                } else {
                    textField.setText("");
                    selectedStop.set("");
                }
            }
        });

        return new StopField(textField, selectedStop);
    }

    public void setTo(String stopName) {
        textField.setText(stopName);
    }
}
