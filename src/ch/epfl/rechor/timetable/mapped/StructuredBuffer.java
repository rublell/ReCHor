package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Preconditions;

import java.nio.ByteBuffer;

/**
 * * @author Valentin Walendy (393413)
 * * @author Ruben Lellouche (400288)
 * StructuredBuffer gère un ByteBuffer structuré selon un format défini par une instance de Structure.
 * Il permet d'accéder aux données du buffer en fonction des types de champs définis.
 */
public class StructuredBuffer {

    private final Structure structure;
    private final ByteBuffer buffer;

    /**
     * Construit un StructuredBuffer avec la structure et le ByteBuffer spécifiés.
     * Vérifie que la capacité du buffer est un multiple de la taille totale de la structure.
     *
     * @param structure la structure définissant le format des données.
     * @param buffer    le ByteBuffer contenant les données.
     * @throws IllegalArgumentException si la capacité du buffer n'est pas un multiple de la taille totale de la structure.
     */
    public StructuredBuffer(Structure structure, ByteBuffer buffer) {
        Preconditions.checkArgument(buffer.capacity() % structure.totalSize() == 0);
        this.structure = structure;
        this.buffer = buffer;
    }

    /**
     * Renvoie le nombre d'éléments stockés dans le buffer.
     *
     * @return le nombre d'éléments.
     */
    public int size() {
        return buffer.capacity() / structure.totalSize();
    }

    /**
     * Lit et renvoie une valeur non signée sur 8 bits (U8) à l'index spécifié.
     *
     * @param fieldIndex   l'index du champ dans la structure.
     * @param elementIndex l'index de l'élément dans le buffer.
     * @return la valeur non signée sur 8 bits.
     */
    public int getU8(int fieldIndex, int elementIndex) {
        int offset = structure.offset(fieldIndex, elementIndex);
        return Byte.toUnsignedInt(buffer.get(offset));
    }

    /**
     * Lit et renvoie une valeur non signée sur 16 bits (U16) à l'index spécifié.
     *
     * @param fieldIndex   l'index du champ dans la structure.
     * @param elementIndex l'index de l'élément dans le buffer.
     * @return la valeur non signée sur 16 bits.
     */
    public int getU16(int fieldIndex, int elementIndex) {
        int offset = structure.offset(fieldIndex, elementIndex);
        return Short.toUnsignedInt(buffer.getShort(offset));
    }

    /**
     * Lit et renvoie une valeur signée sur 32 bits (S32) à l'index spécifié.
     *
     * @param fieldIndex   l'index du champ dans la structure.
     * @param elementIndex l'index de l'élément dans le buffer.
     * @return la valeur signée sur 32 bits.
     */
    public int getS32(int fieldIndex, int elementIndex) {
        int offset = structure.offset(fieldIndex, elementIndex);
        return buffer.getInt(offset);
    }
}
