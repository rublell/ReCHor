package ch.epfl.rechor.timetable.mapped;

import java.util.Objects;

/**
 * * @author Valentin Walendy (393413)
 * * @author Ruben Lellouche (400288)
 * Structure représente une structure de données définissant le format des champs
 * dans un buffer structuré.
 */
public class Structure {

    /**
     * Enumération des types de champs supportés.
     */
    public enum FieldType {
        U8, U16, S32;
    }

    /**
     * Représente un champ de la structure avec son index et son type.
     */
    public record Field(int index, FieldType type) {
        /**
         * Constructeur du record Field qui vérifie que le type n'est pas nul.
         *
         * @param index l'index du champ.
         * @param type  le type du champ.
         * @throws NullPointerException si le type est nul.
         */
        public Field {
            Objects.requireNonNull(type);
        }
    }

    /**
     * Crée et renvoie un nouveau champ avec l'index et le type spécifiés.
     *
     * @param index l'index du champ.
     * @param type  le type du champ.
     * @return une nouvelle instance de Field.
     */
    public static Field field(int index, FieldType type) {
        return new Field(index, type);
    }

    private final Field[] fields;

    /**
     * Construit une Structure à partir d'une séquence de champs.
     * Chaque champ doit avoir un index correspondant à sa position dans le tableau.
     *
     * @param fields la séquence de champs définissant la structure.
     * @throws IllegalArgumentException si un champ n'a pas l'index attendu.
     */
    public Structure(Field... fields) {
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].index() != i) {
                throw new IllegalArgumentException();
            }
        }
        this.fields = fields;
    }

    /**
     * Calcule et renvoie la taille totale en octets de la structure.
     *
     * @return la taille totale en octets.
     */
    public int totalSize() {
        int size = 0;
        for (Field f : fields) {
            switch (f.type()) {
                case U8:
                    size += 1;
                    break;
                case U16:
                    size += 2;
                    break;
                case S32:
                    size += 4;
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
        return size;
    }

    /**
     * Calcule et renvoie l'offset (décalage en octets) d'un champ pour un élément donné.
     *
     * @param fieldIndex   l'index du champ dans la structure.
     * @param elementIndex l'index de l'élément dans le buffer.
     * @return l'offset en octets du champ pour l'élément spécifié.
     * @throws IndexOutOfBoundsException si l'index du champ ou l'index de l'élément est invalide.
     */
    public int offset(int fieldIndex, int elementIndex) {
        if (fieldIndex < 0 || fieldIndex >= fields.length) {
            throw new IndexOutOfBoundsException();
        }
        if (elementIndex < 0) {
            throw new IndexOutOfBoundsException();
        }
        int fieldOffset = 0;
        for (int i = 0; i < fieldIndex; i++) {
            switch (fields[i].type()) {
                case U8:
                    fieldOffset += 1;
                    break;
                case U16:
                    fieldOffset += 2;
                    break;
                case S32:
                    fieldOffset += 4;
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
        return elementIndex * totalSize() + fieldOffset;
    }
}