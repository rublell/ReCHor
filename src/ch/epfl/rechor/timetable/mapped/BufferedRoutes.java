package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.journey.Vehicle;
import ch.epfl.rechor.timetable.Routes;

import java.nio.ByteBuffer;
import java.util.List;

/**
 *  *  @author Valentin Walendy (393413)
 *  *  @author Ruben Lellouche (400288)
 * Implémentation de l'interface {@code Routes} utilisant un buffer structuré pour stocker les routes.
 */
public final class BufferedRoutes implements Routes {

    private static final int NAME_ID = 0;
    private static final int KIND_ID = 1;

    private final List<String> stringTable;
    private final StructuredBuffer buffer;

    private static final Structure STRUCTURE = new Structure(
            Structure.field(NAME_ID, Structure.FieldType.U16),
            Structure.field(KIND_ID, Structure.FieldType.U8)
    );

    /**
     * Construit une instance de {@code BufferedRoutes} à partir d'une table de chaînes et d'un buffer de données.
     *
     * @param stringTable La table des chaînes contenant les noms des routes.
     * @param buffer      Le buffer contenant les informations des routes sous forme aplatie.
     */
    public BufferedRoutes(List<String> stringTable, ByteBuffer buffer) {
        this.buffer = new StructuredBuffer(STRUCTURE, buffer);
        this.stringTable = stringTable;
    }

    /**
     * Retourne le type de véhicule associé à une route donnée.
     *
     * @param id L'identifiant de la route.
     * @return Le véhicule associé à cette route.
     */
    @Override
    public Vehicle vehicle(int id) {
        return Vehicle.ALL.get(buffer.getU8(KIND_ID, id));
    }

    /**
     * Retourne le nom de la route d'un identifiant donné.
     *
     * @param id L'identifiant de la route.
     * @return Le nom de la route sous forme de chaîne de caractères.
     */
    @Override
    public String name(int id) {
        return stringTable.get(buffer.getU16(NAME_ID, id));
    }

    /**
     * Retourne le nombre total de routes stockées.
     *
     * @return La taille du buffer, représentant le nombre de routes.
     */
    @Override
    public int size() {
        return buffer.size();
    }
}