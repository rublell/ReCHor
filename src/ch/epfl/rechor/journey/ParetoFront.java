package ch.epfl.rechor.journey;

import ch.epfl.rechor.FormatterFr;

import java.time.Duration;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.LongConsumer;

/**
 *  * @author Valentin Walendy (393413)
 *  * @author Ruben Lellouche (400288)
 * Classe représentant une frontière Pareto constituée de tuples empaquetés.
 * Chaque tuple est encodé dans un long et représente des critères d'optimisation.
 */
public class ParetoFront {

    /** Une frontière Pareto vide. */
    public static final ParetoFront EMPTY = new ParetoFront(new long[0]);
    private final long[] criteria;

    /**
     * Constructeur privé.
     *
     * @param criteria le tableau de tuples représentant la frontière Pareto.
     */
   private ParetoFront(long[] criteria) {
        this.criteria = criteria;
    }

    /**
     * Retourne le nombre de tuples présents dans la frontière.
     *
     * @return la taille de la frontière.
     */
    public int size() {
        return criteria.length;
    }

    /**
     * Retourne le tuple dont les heures d'arrivée et le nombre de changements correspondent
     * aux valeurs spécifiées.
     *
     * @param arrMins l'heure d'arrivée attendue.
     * @param changes le nombre de changements attendu.
     * @return le tuple correspondant.
     * @throws NoSuchElementException si aucun tuple ne correspond.
     */
    public long get(int arrMins, int changes) {
        for (int i = 0; i < criteria.length; i++) {
            long crit = criteria[i];
            if (PackedCriteria.arrMins(crit) == arrMins && PackedCriteria.changes(crit) == changes) {
                return crit;
            }
        }
        throw new NoSuchElementException();
    }

    /**
     * Exécute l'action spécifiée pour chaque tuple de la frontière.
     *
     * @param action l'action à appliquer sur chaque tuple.
     * @throws NullPointerException si l'action est nulle.
     */
    public void forEach(LongConsumer action) {
        for (long criterion : criteria) {
            action.accept(criterion);
        }
    }

    /**
     * Retourne une représentation textuelle de la frontière Pareto.
     * Chaque tuple est affiché avec ses heures de départ (si présentes), d'arrivée et le nombre de changements.
     *
     * @return une chaîne décrivant la frontière.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (long criterion : criteria) {
            if (PackedCriteria.hasDepMins(criterion)) {
                int depMins = PackedCriteria.depMins(criterion);
                Duration depDuration = Duration.ofMinutes(depMins);
                sb.append("dep: ").append(FormatterFr.formatDuration(depDuration));
                sb.append(", ");
            }
            int arrMins = PackedCriteria.arrMins(criterion);
            Duration arrduration = Duration.ofMinutes(arrMins);
            sb.append("arr : ").append(FormatterFr.formatDuration(arrduration));
            sb.append(", ");
            sb.append("Changes : ").append(PackedCriteria.changes(criterion));
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Classe Builder pour construire dynamiquement une frontière Pareto.
     */
    public static class Builder {

        private long[] frontiere;
        private int size;
        private static final int INITIAL_CAPACITY = 2;

        /**
         * Construit un Builder avec une frontière vide.
         */
        public Builder() {
            this.frontiere = new long[INITIAL_CAPACITY];
            this.size = 0;
        }

        /**
         * Constructeur de copie.
         *
         * @param that le Builder à copier.
         */
        public Builder(Builder that) {
            this.frontiere = Arrays.copyOf(that.frontiere, that.frontiere.length);
            this.size = that.size;
        }

        /**
         * Indique si la frontière en cours de construction est vide.
         *
         * @return true si la frontière est vide, false sinon.
         */
        public boolean isEmpty() {
            return size == 0;
        }

        /**
         * Vide la frontière en cours de construction.
         *
         * @return le Builder courant, pour le chaînage.
         */
        public Builder clear() {
            size = 0;
            return this;
        }

        /**
         * Ajoute un tuple empaqueté à la frontière, en maintenant l'ordre lexicographique
         * et en supprimant les éléments dominés.
         *
         * @param packedTuple le tuple empaqueté à ajouter.
         * @return le Builder courant, pour le chaînage.
         */
        public Builder add(long packedTuple) {

            boolean doitEtreVrai = true;
            int destPos = 0;
            for (int i = 0; i < size; i++) {
                if (packedTuple > frontiere[i]) {
                    if (PackedCriteria.dominatesOrIsEqual(frontiere[i], packedTuple)) {
                        doitEtreVrai = false;
                        break;
                    }
                    destPos = i + 1;
                } else {
                    if (PackedCriteria.dominatesOrIsEqual(packedTuple, frontiere[i])) {

                        if (i<size) {
                            System.arraycopy(frontiere, i + 1, frontiere, i, size - i - 1);
                        }

                        size--;
                        i--;
                    }
                }
            }

            if (doitEtreVrai) {
                if (size == frontiere.length) {
                    frontiere = Arrays.copyOf(frontiere, frontiere.length * 2);
                }

                if (size != 0) {
                    System.arraycopy(frontiere, destPos, frontiere, destPos + 1, size - destPos );
                }

                frontiere[destPos] = packedTuple;
                size++;
            }

            return this;
        }

        /**
         * Empaquette les paramètres donnés en un tuple et l'ajoute à la frontière.
         *
         * @param arrMins l'heure d'arrivée.
         * @param changes le nombre de changements.
         * @param payload le champ additionnel.
         * @return le Builder courant, pour le chaînage.
         */
        public Builder add(int arrMins, int changes, int payload) {
            long packedTuples = PackedCriteria.pack(arrMins, changes, payload);
            return add(packedTuples);
        }

        /**
         * Ajoute tous les tuples présents dans un autre Builder à celui-ci.
         *
         * @param that le Builder dont les tuples seront ajoutés.
         * @return le Builder courant, pour le chaînage.
         */
        public Builder addAll(Builder that) {
            for (int i = 0; i < that.size; i++) {
                this.add(that.frontiere[i]);
            }
            return this;
        }

        /**
         * Vérifie si le Builder courant domine complètement un autre Builder
         * en fixant l'heure de départ des tuples de l'autre Builder à depMins.
         *
         * @param that le Builder dont tous les tuples doivent être dominés.
         * @param depMins la valeur forcée de l'heure de départ.
         * @return true si tous les tuples de l'autre Builder, après ajustement, sont dominés par au moins un tuple du Builder courant.
         * @throws IllegalArgumentException dans certains cas d'incohérence entre heures de départ.
         */
        public boolean fullyDominates(Builder that, int depMins) {
            boolean thisHasNoDep = true;
            for (int i = 0; i < this.size; i++) {
                if (PackedCriteria.hasDepMins(this.frontiere[i])) {
                    thisHasNoDep = false;
                    break;
                }
            }
            boolean thatHasNoDep = true;
            for (int i = 0; i < that.size; i++) {
                if (PackedCriteria.hasDepMins(that.frontiere[i])) {
                    thatHasNoDep = false;
                    break;
                }
            }
            if (thisHasNoDep && thatHasNoDep) {
                if (depMins == 0) {
                    throw new IllegalArgumentException();
                }
                for (int i = 0; i < that.size; i++) {
                    long tupleThat = that.frontiere[i];
                    boolean isDominated = false;
                    for (int j = 0; j < this.size; j++) {
                        long tupleThis = this.frontiere[j];
                        if (PackedCriteria.arrMins(tupleThis) <= PackedCriteria.arrMins(tupleThat)
                                && PackedCriteria.changes(tupleThis) <= PackedCriteria.changes(tupleThat)) {
                            isDominated = true;
                            break;
                        }
                    }
                    if (!isDominated) {
                        return false;
                    }
                }
                return true;
            } else {
                for (int i = 0; i < that.size; i++) {
                    long tupleThat = that.frontiere[i];
                    long modifiedTupleThat = PackedCriteria.withDepMins(tupleThat, depMins);
                    boolean isDominated = false;
                    for (int j = 0; j < this.size; j++) {
                        long tupleThis = this.frontiere[j];
                        long modifiedTupleThis = PackedCriteria.hasDepMins(tupleThis)
                                ? tupleThis
                                : PackedCriteria.withDepMins(tupleThis, depMins);
                        if (PackedCriteria.dominatesOrIsEqual(modifiedTupleThis, modifiedTupleThat)) {
                            isDominated = true;
                            break;
                        }
                    }
                    if (!isDominated) {
                        return false;
                    }
                }
                return true;
            }
        }

        /**
         * Exécute l'action spécifiée pour chaque tuple de la frontière en cours de construction.
         *
         * @param action l'action à appliquer sur chaque tuple.
         * @throws NullPointerException si l'action est nulle.
         */
        public void forEach(LongConsumer action) {
            Objects.requireNonNull(action);
            for (int i = 0; i < size; i++) {
                action.accept(frontiere[i]);
            }
        }

        /**
         * Construit une instance immuable de ParetoFront à partir de la frontière en cours de construction.
         *
         * @return une instance de ParetoFront contenant les tuples actuellement stockés.
         */
        public ParetoFront build() {
            return new ParetoFront(Arrays.copyOf(frontiere, size));
        }

        /**
         * Retourne une représentation textuelle de la frontière en cours de construction.
         *
         * @return une chaîne décrivant la frontière construite.
         */
        public String toString() {
            return this.build().toString();
        }
    }
}
