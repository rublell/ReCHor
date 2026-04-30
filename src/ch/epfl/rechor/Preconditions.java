package ch.epfl.rechor;

/**
 * * @author Valentin Walendy (393413)
 *  * @author Ruben Lellouche (400288)
 * Classe utilitaire contenant des méthodes de validation des préconditions.
 * Cette classe permet de vérifier si des conditions sont remplies avant d'exécuter une opération.
 * Si la condition donnée n'est pas remplie, une exception {@link IllegalArgumentException} est levée.
 */
public final class Preconditions {


    private Preconditions() {
    }

    /**
     * Vérifie si l'argument donné est vrai.
     * Si l'argument est faux, une exception {@link IllegalArgumentException} est levée.
     *
     * @param shouldBeTrue Le booléen à vérifier.
     * @throws IllegalArgumentException Si l'argument est faux.
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }
}
