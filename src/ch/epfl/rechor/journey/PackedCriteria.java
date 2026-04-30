package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

/**
 * *  @author Valentin Walendy (393413)
 * *  @author Ruben Lellouche (400288)
 * Classe utilitaire pour encoder et manipuler un critère de trajet sous forme d'un ⁠ long ⁠.
 */
public final class PackedCriteria {
    private static final int TIME_FIELD_BITS = 12;
    private static final int CHANGES_BITS = 7;
    private static final int PAYLOAD_FIELD_BITS = 32;
    private static final int CHANGES_SHIFT = PAYLOAD_FIELD_BITS;
    private static final int ARRIVAL_TIME_SHIFT = CHANGES_BITS + PAYLOAD_FIELD_BITS;
    private static final long DEPARTURE_TIME_SHIFT = TIME_FIELD_BITS + CHANGES_BITS + PAYLOAD_FIELD_BITS;
    private static final long TIME_MASK = 0xFFFL;
    private static final long CHANGE_COUNT_MASK = 0b1111111L;
    private static final long PAYLOAD_FIELD_MASK = 0xFFFFFFFFL;
    private static final long DEPARTURE_TIME_MASK = 1L;
    private static final int TIME_ORIGIN_OFFSET = -240;
    private static final int COMPLEMENT = 4095;
    private static final int MAX_TIME_OFFSET = 3119;


    /**
     * Private constructor to prevent instantiation.
     */
    private PackedCriteria() {}

    /**
     * Packs optimization criteria into a 64-bit long (without departure time).
     *
     * @param arrMins The arrival time in minutes since midnight.
     * @param changes The number of changes (max 127).
     * @param payload The payload (32-bit).
     * @return A packed long representing the criteria.
     * @throws IllegalArgumentException If the arrival time is out of range or changes exceed 7
     *                                  bits.
     */
    public static long pack(int arrMins, int changes, int payload) {
        Preconditions.checkArgument(arrMins >= TIME_ORIGIN_OFFSET && arrMins <= MAX_TIME_OFFSET + TIME_ORIGIN_OFFSET);
        Preconditions.checkArgument(changes >>> CHANGES_BITS == 0);

        return (((long) arrMins - TIME_ORIGIN_OFFSET) << ARRIVAL_TIME_SHIFT) |
                (((long) changes) << CHANGES_SHIFT) | (Integer.toUnsignedLong(payload));
    }


    public static boolean hasDepMins(long criteria) {
        return (criteria >>> DEPARTURE_TIME_SHIFT) != 0;
    }


    public static int depMins(long criteria) {
        if (!hasDepMins(criteria)) throw new IllegalArgumentException();
        return COMPLEMENT - ((int) (criteria >>> DEPARTURE_TIME_SHIFT)) + TIME_ORIGIN_OFFSET;

    }


    public static int arrMins(long criteria) {

        return (int) ((criteria >>> ARRIVAL_TIME_SHIFT) & TIME_MASK) + TIME_ORIGIN_OFFSET;
    }


    public static int changes(long criteria) {
        return (int) ((criteria >>> PAYLOAD_FIELD_BITS) & CHANGE_COUNT_MASK);
    }

    /**
     * Extrait le champ additionnel (payload).
     */
    public static int payload(long criteria) {
        return (int) (criteria & PAYLOAD_FIELD_MASK);
    }

    /**
     * Checks if one set of criteria dominates or is equal to another.
     *
     * @param criteria1 First criteria.
     * @param criteria2 Second criteria.
     * @return True if {@code criteria1} dominates or is equal to {@code criteria2}.
     * @throws IllegalArgumentException If one has a departure time and the other does not.
     */
    /**
     * Vérifie si un critère domine ou est égal à un autre.
     *
     * @throws IllegalArgumentException Si un critère a une heure de départ et pas l'autre.
     */
    public static boolean dominatesOrIsEqual(long criteria1, long criteria2) {
        if (hasDepMins(criteria1) && hasDepMins(criteria2)) {
            return arrMins(criteria1) <= arrMins(criteria2) &&
                    depMins(criteria1) >= depMins(criteria2) &&
                    changes(criteria1) <= changes(criteria2);
        } else if (!hasDepMins(criteria1) && !hasDepMins(criteria2)) {
            return arrMins(criteria1) <= arrMins(criteria2) &&
                    changes(criteria1) <= changes(criteria2);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Supprime l'heure de départ.
     */
    public static long withoutDepMins(long criteria) {
        return criteria & ~(TIME_MASK << DEPARTURE_TIME_SHIFT);
    }

    /**
     * Ajoute/modifie l'heure de départ.
     *
     * @throws IllegalArgumentException Si hors limites.
     */
    public static long withDepMins(long criteria, int depMins) {
        depMins = COMPLEMENT - (depMins - TIME_ORIGIN_OFFSET);
        return criteria | (((long) depMins) << DEPARTURE_TIME_SHIFT);

    }

    /**
     * Ajoute un changement.
     *
     * @throws IllegalArgumentException Si dépasse 127.
     */
    public static long withAdditionalChange(long criteria) {

        return criteria + (DEPARTURE_TIME_MASK << CHANGES_SHIFT);
    }

    /**
     * Modifie le champ additionnel (payload).
     */
    public static long withPayload(long criteria, int payload) {
        return (criteria & ~PAYLOAD_FIELD_MASK) | Integer.toUnsignedLong(payload);
    }
}