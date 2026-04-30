package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


/**
 * @author Valentin Walendy (393413)
 * @author Ruben Lellouche (400288)
 * Cette classe représente un voyage constitué de plusieurs étapes.
 * Un voyage est défini par une liste d'étapes (Leg), et chaque étape peut être soit un transport, soit un déplacement à pied.
 */
public record Journey(List<Leg> legs) {

    /**
     * Constructeur de la classe Journey.
     * Vérifie que les conditions suivantes sont remplies :
     * - La liste d'étapes (legs) ne doit pas être vide.
     * - Chaque étape de transport ou de pied doit être valide en termes d'horaires et de transitions.
     *
     * @param legs Liste des étapes du voyage.
     * @throws IllegalArgumentException Si les étapes ne sont pas valides (par exemple, si une étape de transport n'a pas une correspondance correcte avec la suivante).
     */
    public Journey {
        Objects.requireNonNull(legs);
        Preconditions.checkArgument(!legs.isEmpty());
        legs = List.copyOf(legs);


        for (int i = 1; i < legs.size(); i++) {
            Leg previous = legs.get(i - 1);
            Leg current = legs.get(i);

            if (!previous.arrStop().equals(current.depStop())) {
                throw new IllegalArgumentException();
            }

            if (previous.arrTime().isAfter(current.depTime())) {
                throw new IllegalArgumentException();
            }

            if (previous instanceof Leg.Foot && current instanceof Leg.Foot) {
                throw new IllegalArgumentException();
            }
        }
    }

    public interface Leg {

        LocalDateTime depTime();
        LocalDateTime arrTime();
        Stop depStop();
        Stop arrStop();
        List<IntermediateStop> intermediateStops();

        default Duration duration() {
            return Duration.between(depTime(), arrTime());
        }

        /**
         * Cette classe représente un arrêt intermédiaire entre deux étapes de voyage.
         */
        public record IntermediateStop(Stop stop, LocalDateTime arrTime, LocalDateTime depTime) {

            /**
             * Constructeur de la classe IntermediateStop.
             * Vérifie que l'heure de départ n'est pas avant l'heure d'arrivée.
             *
             * @param stop L'arrêt intermédiaire.
             * @param arrTime Heure d'arrivée.
             * @param depTime Heure de départ.
             * @throws IllegalArgumentException Si l'heure de départ est avant l'heure d'arrivée.
             */
            public IntermediateStop {
                Objects.requireNonNull(stop);
                Objects.requireNonNull(arrTime);
                Objects.requireNonNull(depTime);
                Preconditions.checkArgument(!depTime.isBefore(arrTime));
            }
        }

        /**
         * Cette classe représente un trajet en transport (par exemple, en train).
         */
        public record Transport(Stop depStop, LocalDateTime depTime, Stop arrStop, LocalDateTime arrTime, List<IntermediateStop> intermediateStops, Vehicle vehicle, String route, String destination) implements Leg {

            /**
             * Constructeur de la classe Transport.
             * Vérifie que l'heure d'arrivée n'est pas avant l'heure de départ et que tous les champs sont valides.
             *
             * @param depStop L'arrêt de départ.
             * @param depTime L'heure de départ.
             * @param arrStop L'arrêt d'arrivée.
             * @param arrTime L'heure d'arrivée.
             * @param intermediateStops Liste des arrêts intermédiaires.
             * @param vehicle Le véhicule utilisé pour le transport.
             * @param route La route empruntée.
             * @param destination La destination du trajet.
             * @throws IllegalArgumentException Si l'heure d'arrivée est avant l'heure de départ.
             */
            public Transport {
                Objects.requireNonNull(depStop);
                Objects.requireNonNull(depTime);
                Objects.requireNonNull(arrStop);
                Objects.requireNonNull(arrTime);
                Objects.requireNonNull(vehicle);
                Objects.requireNonNull(route);
                Objects.requireNonNull(destination);
                Preconditions.checkArgument(!arrTime.isBefore(depTime));

                intermediateStops = List.copyOf(intermediateStops);
            }
        }

        /**
         * Cette classe représente un trajet à pied entre deux arrêts.
         */
        public record Foot(Stop depStop, LocalDateTime depTime, Stop arrStop, LocalDateTime arrTime) implements Leg {

            /**
             * Constructeur de la classe Foot.
             * Vérifie que l'heure de départ n'est pas après l'heure d'arrivée.
             *
             * @param depStop L'arrêt de départ.
             * @param depTime L'heure de départ.
             * @param arrStop L'arrêt d'arrivée.
             * @param arrTime L'heure d'arrivée.
             * @throws IllegalArgumentException Si l'heure de départ est après l'heure d'arrivée.
             */
            public Foot {
                Objects.requireNonNull(depStop);
                Objects.requireNonNull(arrStop);
                Objects.requireNonNull(depTime);
                Objects.requireNonNull(arrTime);
                Preconditions.checkArgument(!depTime.isAfter(arrTime));
            }

            public List<IntermediateStop> intermediateStops() {
                return List.of();
            }

            /**
             * Détermine si le trajet à pied est un changement (les arrêts de départ et d'arrivée sont les mêmes).
             *
             * @return true si c'est un changement, false sinon.
             */
            public boolean isTransfer() {
                return depStop.name().equals(arrStop.name());
            }
        }
    }

    /**
     * Retourne l'arrêt de départ du voyage, qui est le premier arrêt de la première étape.
     *
     * @return L'arrêt de départ.
     */
    public Stop depStop() {
        return legs.get(0).depStop();
    }

    /**
     * Retourne l'arrêt d'arrivée du voyage, qui est le dernier arrêt de la dernière étape.
     *
     * @return L'arrêt d'arrivée.
     */
    public Stop arrStop() {
        return legs.get(legs.size() - 1).arrStop();
    }

    /**
     * Retourne l'heure de départ du voyage, qui est l'heure de départ de la première étape.
     *
     * @return L'heure de départ.
     */
    public LocalDateTime depTime() {
        return legs.get(0).depTime();
    }

    /**
     * Retourne l'heure d'arrivée du voyage, qui est l'heure d'arrivée de la dernière étape.
     *
     * @return L'heure d'arrivée.
     */
    public LocalDateTime arrTime() {
        return legs.get(legs.size() - 1).arrTime();
    }

    /**
     * Retourne la durée totale du voyage en calculant la différence entre l'heure de départ et l'heure d'arrivée.
     *
     * @return La durée totale du voyage.
     */
    public Duration duration() {
        return Duration.between(depTime(), arrTime());
    }
}
