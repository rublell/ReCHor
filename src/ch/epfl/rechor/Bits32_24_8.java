package ch.epfl.rechor;

/**
 * * @author Valentin Walendy (393413)
 *  * @author Ruben Lellouche (400288)
 * Classe utilitaire pour encoder et extraire des valeurs de 24 bits et 8 bits dans un entier 32 bits.
 */
public final class Bits32_24_8 {

    /**
     * Constructeur privé pour empêcher l'instanciation.
     */
    private Bits32_24_8() {
    }

    /**
     * Encode une valeur de 24 bits et une valeur de 8 bits dans un entier 32 bits.
     *
     * @param bits24 Valeur sur 24 bits (0 à 2^24 - 1).
     * @param bits8  Valeur sur 8 bits (0 à 255).
     * @return Un entier 32 bits combinant les deux valeurs.
     * @throws IllegalArgumentException Si {@code bits24} ou {@code bits8} dépasse leur plage.
     */
    public static int pack(int bits24, int bits8) {
        Preconditions.checkArgument((bits24>>24)==0);
        Preconditions.checkArgument((bits8>>8)==0);
        return (bits24 << 8) | (bits8 & 0xFF);
    }


    /**
     * Extrait la partie 24 bits d'un entier 32 bits.
     */
    public static int unpack24(int bits32) {
        return (bits32 >>> 8);
    }

    /**
     * Extrait la partie 8 bits d'un entier 32 bits.
     */
    public static int unpack8(int bits32) {
        return bits32 & 0xFF;
    }
}


