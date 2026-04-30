package ch.epfl.rechor;

import ch.epfl.rechor.journey.PackedCriteria;

/**
 * * @author Valentin Walendy (393413)
 * * @author Ruben Lellouche (400288)
 * Classe utilitaire PackedRange permettant de représenter un intervalle [startInclusive, endExclusive)
 * sous la forme d'un entier codé sur 32 bits.
 *
 * Cette classe est non instanciable.
 */
public final class PackedRange {


    private PackedRange() {
    }

    /**
     * Empaquette un intervalle en un entier de 32 bits.
     *
     * @param startInclusive La borne inférieure incluse de l'intervalle (codée sur 24 bits).
     * @param endExclusive   La borne supérieure exclue de l'intervalle.
     * @return Un entier codé sur 32 bits représentant l'intervalle.
     * @throws IllegalArgumentException Si les bornes sont invalides (ordre incorrect ou valeurs hors limites).
     */
    public static int pack(int startInclusive, int endExclusive) {
        if (endExclusive < startInclusive) {

            throw new IllegalArgumentException();
        }
        if (startInclusive < 0 || startInclusive >= (1 << 24)) {

            throw new IllegalArgumentException();
        }

        int length = endExclusive - startInclusive;
        if (length > 255) {
            throw new IllegalArgumentException();
        }

        return Bits32_24_8.pack(startInclusive, length);

    }

    /**
     * Extrait la longueur de l'intervalle empaqueté.
     *
     * @param interval L'entier représentant l'intervalle.
     * @return La longueur de l'intervalle.
     */
    public static int length(int interval) {
        return Bits32_24_8.unpack8(interval);
    }

    /**
     * Extrait la borne inférieure incluse de l'intervalle empaqueté.
     *
     * @param interval L'entier représentant l'intervalle.
     * @return La borne inférieure incluse.
     */
    public static int startInclusive(int interval) {
        return Bits32_24_8.unpack24(interval);
    }

    /**
     * Calcule la borne supérieure exclue de l'intervalle empaqueté.
     *
     * @param interval L'entier représentant l'intervalle.
     * @return La borne supérieure exclue.
     */
    public static int endExclusive(int interval) {
        return startInclusive(interval) + length(interval);
    }
}

