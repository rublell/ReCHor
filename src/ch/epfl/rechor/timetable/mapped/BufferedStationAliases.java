package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.StationAliases;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * * @author Valentin Walendy (393413)
 * * @author Ruben Lellouche (400288)
 * BufferedStationAliases est une implémentation de l'interface StationAliases qui utilise un StructuredBuffer
 * et une table de chaînes pour associer des alias de station à leurs noms correspondants.
 */
public final class BufferedStationAliases implements StationAliases {

    private final List<String> stringTable;
    private final StructuredBuffer buffer;

    public static final int ALIAS_FIELD_INDEX = 0;
    public static final int STATION_NAME_FIELD_INDEX = 1;

    public static final Structure STRUCTURE = new Structure(
            Structure.field(ALIAS_FIELD_INDEX, Structure.FieldType.U16),
            Structure.field(STATION_NAME_FIELD_INDEX, Structure.FieldType.U16)
    );

    /**
     * Construit une instance de BufferedStationAliases avec la table de chaînes et le ByteBuffer spécifiés.
     *
     * @param stringTable la liste de chaînes utilisée pour résoudre les alias et les noms de station.
     * @param buffer      le ByteBuffer contenant les données structurées des alias de station.
     */
    public BufferedStationAliases(List<String> stringTable, ByteBuffer buffer) {
        this.buffer = new StructuredBuffer(STRUCTURE, buffer);
        this.stringTable = stringTable;
    }

    /**
     * Vérifie si l'index donné est dans la plage valide.
     *
     * @param id l'index à vérifier.
     * @throws IndexOutOfBoundsException si l'index est négatif ou supérieur ou égal à la taille.
     */
    private void checkIndex(int id) {
        if (id < 0 || id >= size()) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Renvoie l'alias associé à l'index spécifié.
     *
     * @param id l'index de l'alias.
     * @return l'alias sous forme de chaîne.
     * @throws IndexOutOfBoundsException si l'index est invalide.
     */
    @Override
    public String alias(int id) {
        checkIndex(id);
        int index = buffer.getU16(ALIAS_FIELD_INDEX, id);
        return stringTable.get(index);
    }

    /**
     * Renvoie le nom de station associé à l'alias spécifié.
     *
     * @param id l'index de l'alias de station.
     * @return le nom de la station sous forme de chaîne.
     * @throws IndexOutOfBoundsException si l'index est invalide.
     */
    @Override
    public String stationName(int id) {
        checkIndex(id);
        int index = buffer.getU16(STATION_NAME_FIELD_INDEX, id);
        return stringTable.get(index);
    }

    /**
     * Renvoie le nombre total d'alias de station contenus dans ce buffer.
     *
     * @return le nombre d'alias de station.
     */
    @Override
    public int size() {
        return buffer.size();
    }
}
