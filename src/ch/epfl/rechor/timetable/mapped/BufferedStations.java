package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Stations;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * * @author Valentin Walendy (393413)
 * * @author Ruben Lellouche (400288)
 * BufferedStations est une implémentation de l'interface Stations qui utilise un StructuredBuffer
 * et une table de chaînes pour gérer les données des stations, notamment leur nom et leurs coordonnées.
 */
public final class BufferedStations implements Stations {

    private static final int NAME_ID_OFFSET = 0;
    private static final int LON_OFFSET = 1;
    private static final int LAT_OFFSET = 2;

    private static final double COORD_CONVERSION = Math.scalb(360, -32);

    private final List<String> stringTable;
    private final StructuredBuffer buffer;

    private final static Structure STRUCTURE = new Structure(
            Structure.field(NAME_ID_OFFSET, Structure.FieldType.U16),
            Structure.field(LON_OFFSET, Structure.FieldType.S32),
            Structure.field(LAT_OFFSET, Structure.FieldType.S32)
    );

    /**
     * Construit une instance de BufferedStations avec la table de chaînes et le ByteBuffer spécifiés.
     *
     * @param stringTable la liste de chaînes utilisée pour résoudre les noms des stations.
     * @param buffer      le ByteBuffer contenant les données structurées des stations.
     */
    public BufferedStations(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.buffer = new StructuredBuffer(STRUCTURE, buffer);
    }

    /**
     * Vérifie si l'index donné est dans la plage valide.
     *
     * @param id l'index à vérifier.
     * @throws IndexOutOfBoundsException si l'index est négatif ou supérieur ou égal à la taille.
     */


    /**
     * Renvoie le nom de la station associé à l'index spécifié.
     *
     * @param id l'index de la station.
     * @return le nom de la station sous forme de chaîne.
     * @throws IndexOutOfBoundsException si l'index est invalide.
     */
    @Override
    public String name(int id) {
        return stringTable.get(buffer.getU16(NAME_ID_OFFSET, id));
    }

    /**
     * Renvoie la longitude de la station associée à l'index spécifié.
     *
     * @param id l'index de la station.
     * @return la longitude de la station sous forme de double.
     * @throws IndexOutOfBoundsException si l'index est invalide.
     */
    @Override
    public double longitude(int id) {

        return buffer.getS32(LON_OFFSET, id) * COORD_CONVERSION;
    }

    /**
     * Renvoie la latitude de la station associée à l'index spécifié.
     *
     * @param id l'index de la station.
     * @return la latitude de la station sous forme de double.
     * @throws IndexOutOfBoundsException si l'index est invalide.
     */
    @Override
    public double latitude(int id) {

        return buffer.getS32(LAT_OFFSET, id) * COORD_CONVERSION;
    }

    /**
     * Renvoie le nombre total de stations contenues dans ce buffer.
     *
     * @return le nombre de stations.
     */
    @Override
    public int size() {
        return buffer.size();
    }
}
