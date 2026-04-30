package ch.epfl.rechor;

import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Stop;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * * @author Valentin Walendy (393413)
 *  * @author Ruben Lellouche (400288)
 * Classe utilitaire contenant des méthodes de formatage pour afficher des informations liées aux trajets,
 * aux arrêts, aux horaires, et aux durées dans un format adapté au contexte français.
 */
public final class FormatterFr {


    private FormatterFr() {}

    /**
     * Formate une durée en minutes et heures en une chaîne de caractères.
     * Si la durée est inférieure à une heure, elle sera affichée en minutes.
     * Sinon, elle sera affichée en heures et minutes.
     *
     * @param duration La durée à formater.
     * @return La durée formatée sous forme de chaîne.
     */
    public static String formatDuration (Duration duration){
        long minutes= duration.toMinutes();
        long hours= minutes / 60;
        long remainingMinutes= minutes % 60;

        if (hours == 0) {
            return minutes + " min";
        } else {
            return hours + " h " + remainingMinutes + " min";
        }
    }

    /**
     * Formate un objet LocalDateTime en une chaîne de caractères sous le format "hh:mm".
     *
     * @param dateTime L'objet LocalDateTime à formater.
     * @return La chaîne formatée représentant l'heure et les minutes.
     */
    public static String formatTime(LocalDateTime dateTime){
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.HOUR_OF_DAY)
                .appendLiteral("h")
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .toFormatter();

        return formatter.format(dateTime);
    }

    /**
     * Formate le nom de la plateforme d'un arrêt.
     * Si le nom commence par un chiffre, il sera précédé de "voie".
     * Si le nom ne commence pas par un chiffre, il sera précédé de "quai".
     *
     * @param stop L'arrêt dont la plateforme doit être formatée.
     * @return Le nom de la plateforme formatée.
     */
    public static String formatPlatformName(Stop stop){
        if (stop.platformName() == null || stop.platformName().isEmpty()) {
            return "";
        }

        if (Character.isDigit(stop.platformName().charAt(0))) {
            return "voie " + stop.platformName();
        } else {
            return "quai " + stop.platformName();
        }
    }

    /**
     * Formate un trajet à pied (type de leg "Foot") en une chaîne de caractères.
     * La chaîne contient soit "changement", soit "trajet à pied", suivie de la durée en minutes.
     *
     * @param footLeg Le trajet à pied à formater.
     * @return Le trajet à pied formaté.
     */
    public static String formatLeg(Journey.Leg.Foot footLeg){
        long durationMinutes = Duration.between(footLeg.depTime(), footLeg.arrTime()).toMinutes();

        if (footLeg.isTransfer()) {
            return "changement (" + durationMinutes + " min)";
        } else {
            return "trajet à pied (" + durationMinutes + " min)";
        }
    }

    /**
     * Formate un trajet en transport (type de leg "Transport") en une chaîne de caractères.
     * La chaîne contient l'heure de départ, l'arrêt de départ, la plateforme de départ (si disponible),
     * l'heure d'arrivée, l'arrêt d'arrivée, et la plateforme d'arrivée (si disponible).
     *
     * @param leg Le trajet en transport à formater.
     * @return Le trajet en transport formaté.
     */
    public static String formatLeg(Journey.Leg.Transport leg) {
        String depTime = FormatterFr.formatTime(leg.depTime());
        String arrTime = FormatterFr.formatTime(leg.arrTime());

        String depStop = leg.depStop().name();
        String arrStop = leg.arrStop().name();

        String formattedDepPlatform = formatPlatformName(leg.depStop());
        String formattedArrPlatform = formatPlatformName(leg.arrStop());

        StringBuilder depInfo = new StringBuilder();
        depInfo.append(depTime).append(" ").append(depStop);
        if (!formattedDepPlatform.isEmpty()) {
            depInfo.append(" (").append(formattedDepPlatform).append(")");
        }

        StringBuilder arrInfo = new StringBuilder();
        arrInfo.append(" → ").append(arrStop);
        if (!formattedArrPlatform.isEmpty()) {
            arrInfo.append(" (arr. ").append(arrTime).append(" ").append(formattedArrPlatform).append(")");
        } else {
            arrInfo.append(" (arr. ").append(arrTime).append(")");
        }

        return depInfo.toString() + arrInfo.toString();
    }

    /**
     * Formate la ligne et la destination d'un trajet en transport (type de leg "Transport").
     * Retourne une chaîne contenant la ligne suivie de la direction de destination.
     *
     * @param transportLeg Le trajet en transport à formater.
     * @return La ligne et la destination formatées.
     */
    public static String formatRouteDestination(Journey.Leg.Transport transportLeg) {
        String line = transportLeg.route();
        String destination = transportLeg.destination();

        return line + " Direction " + destination;
    }
}
