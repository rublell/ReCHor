package ch.epfl.rechor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * * @author Valentin Walendy (393413)
 *  * @author Ruben Lellouche (400288)
 * Constructeur de fichiers iCalendar (ICS) permettant d'ajouter des composants et des champs.
 */
public final class IcalBuilder {

    /** Types de composants iCalendar pris en charge. */
    public enum Component {
        VCALENDAR, VEVENT;
    }

    /** Champs iCalendar utilisables. */
    public enum Name {
        BEGIN, END, PRODID, VERSION, UID, DTSTAMP, DTSTART, DTEND, SUMMARY, DESCRIPTION;
    }

    private final StringBuilder icalBuilder;
    private final List<Component> components;

    /** Initialise un constructeur iCalendar vide. */
    public IcalBuilder() {
        this.icalBuilder = new StringBuilder();
        this.components = new ArrayList<>();
    }

    /**
     * Ajoute un champ iCalendar.
     *
     * @param name  Nom du champ.
     * @param value Valeur du champ.
     * @return L'instance actuelle d'{@code IcalBuilder}.
     * @throws NullPointerException Si {@code name} ou {@code value} est {@code null}.
     */
    public IcalBuilder add(Name name, String value) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);

        String line = name + ":" + value;
        int maxChar = 75;

        if (line.length() <= maxChar) {
            icalBuilder.append(line).append("\r\n");
        } else {
            icalBuilder.append(line, 0, maxChar).append("\r\n");
            int pos = maxChar;

            while (pos < line.length()) {
                int end = Math.min(pos + maxChar-1, line.length());
                icalBuilder.append(" ").append(line, pos, end).append("\r\n");
                pos = end;
            }
        }
        return this;
    }

    /**
     * Ajoute un champ iCalendar avec une date formatée en ISO.
     *
     * @param name     Nom du champ.
     * @param dateTime Date et heure au format {@code LocalDateTime}.
     * @return L'instance actuelle d'{@code IcalBuilder}.
     */
    public IcalBuilder add(Name name, LocalDateTime dateTime) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR, 4)
                .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                .appendValue(ChronoField.DAY_OF_MONTH, 2)
                .appendLiteral('T')
                .appendValue(ChronoField.HOUR_OF_DAY, 2)
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .appendValue(java.time.temporal.ChronoField.SECOND_OF_MINUTE, 2)
                .toFormatter();
        return add(name, formatter.format(dateTime));
    }

    /**
     * Démarre un composant iCalendar.
     *
     * @param component Composant à débuter.
     * @return L'instance actuelle d'{@code IcalBuilder}.
     */
    public IcalBuilder begin(Component component) {
        icalBuilder.append(Name.BEGIN).append(":").append(component).append("\r\n");
        components.add(component);
        return this;
    }

    /**
     * Termine le dernier composant iCalendar ouvert.
     *
     * @return L'instance actuelle d'{@code IcalBuilder}.
     * @throws IllegalArgumentException Si aucun composant n'est ouvert.
     */
    public IcalBuilder end() {
        if (components.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Component lastComponent = components.remove(components.size() - 1);
        icalBuilder.append(Name.END).append(":").append(lastComponent).append("\r\n");
        return this;
    }

    /**
     * Construit et retourne la chaîne iCalendar générée.
     *
     * @return La chaîne iCalendar.
     * @throws IllegalArgumentException Si des composants n'ont pas été fermés.
     */
    public String build() {
        if (!components.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return icalBuilder.toString();
    }

}
