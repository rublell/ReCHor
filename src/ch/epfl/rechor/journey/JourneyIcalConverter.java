package ch.epfl.rechor.journey;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.IcalBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * * @author Valentin Walendy (393413)
 * @author Ruben Lellouche (400288)
 * Convertit un trajet {@link Journey} en un fichier iCalendar (ICS).
 */
public final class JourneyIcalConverter {
    /**
     * Constructeur privé pour empêcher l'instanciation.
     */
    private JourneyIcalConverter() {

    }


    public static String toIcalendar(Journey journey) {
        Objects.requireNonNull(journey);

        IcalBuilder builder = new IcalBuilder();


        builder.begin(IcalBuilder.Component.VCALENDAR)
                .add(IcalBuilder.Name.VERSION, "2.0")
                .add(IcalBuilder.Name.PRODID, "ReCHor");

        builder.begin(IcalBuilder.Component.VEVENT)
                .add(IcalBuilder.Name.UID, UUID.randomUUID().toString())
                .add(IcalBuilder.Name.DTSTAMP, LocalDateTime.now())
                .add(IcalBuilder.Name.DTSTART, journey.depTime())
                .add(IcalBuilder.Name.DTEND, journey.arrTime());

        String summary = journey.depStop().name() + " → " + journey.arrStop().name();
        builder.add(IcalBuilder.Name.SUMMARY, summary);

        StringJoiner j = new StringJoiner("\\n");
        for (Journey.Leg leg : journey.legs()) {
            String description = switch (leg) {
                case Journey.Leg.Foot foot -> FormatterFr.formatLeg(foot);
                case Journey.Leg.Transport transport -> FormatterFr.formatLeg(transport);
                default -> throw new IllegalStateException();
            };
            j.add(description);
        }
        builder.add(IcalBuilder.Name.DESCRIPTION, j.toString());

        builder.end()
                .end();

        return builder.build();

    }
}
