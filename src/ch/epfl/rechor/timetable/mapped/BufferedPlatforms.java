package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Platforms;

import java.nio.ByteBuffer;
import java.util.List;

/**
 *  @author Valentin Walendy (393413)
 *  @author Ruben Lellouche (400288)
 * BufferedPlatforms est une implémentation de l'interface Platforms qui utilise un StructuredBuffer
 * et une table de chaînes pour gérer les données des plateformes.
 */
public final class BufferedPlatforms implements Platforms {

    private static final int NAME_ID_OFFSET = 0;
    private static final int STATION_ID_OFFSET = 1;

    private final List<String> stringTable;
    private final StructuredBuffer buffer;

    private final static Structure STRUCTURE = new Structure(
            Structure.field(NAME_ID_OFFSET, Structure.FieldType.U16),
            Structure.field(STATION_ID_OFFSET, Structure.FieldType.U16)
    );

    /**
     * Construit une instance de BufferedPlatforms avec la table de chaînes et le ByteBuffer spécifiés.
     *
     * @param stringTable la liste de chaînes utilisée pour résoudre les noms des plateformes.
     * @param buffer      le ByteBuffer contenant les données structurées des plateformes.
     */
    public BufferedPlatforms(List<String> stringTable, ByteBuffer buffer) {
        this.buffer = new StructuredBuffer(STRUCTURE, buffer);
        this.stringTable = stringTable;
    }

    /**
     * Vérifie que l'index donné est dans la plage valide.
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
     * Renvoie le nom de la plateforme associé à l'index spécifié.
     *
     * @param id l'index de la plateforme.
     * @return le nom de la plateforme sous forme de chaîne.
     * @throws IndexOutOfBoundsException si l'index est invalide.
     */
    @Override
    public String name(int id) {
        checkIndex(id);
        return stringTable.get(buffer.getU16(NAME_ID_OFFSET, id));
    }

    /**
     * Renvoie l'identifiant de la station associé à la plateforme spécifiée.
     *
     * @param id l'index de la plateforme.
     * @return l'identifiant de la station sous forme d'entier.
     * @throws IndexOutOfBoundsException si l'index est invalide.
     */
    @Override
    public int stationId(int id) {
        checkIndex(id);
        return buffer.getU16(STATION_ID_OFFSET, id);
    }

    /**
     * Renvoie le nombre total de plateformes contenues dans ce buffer.
     *
     * @return le nombre de plateformes.
     */
    @Override
    public int size() {
        return buffer.size();
    }
}
