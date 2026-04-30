package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Trips;

import java.nio.ByteBuffer;
import java.util.List;

/**
 *  * * @author Valentin Walendy (393413)
 *  * * @author Ruben Lellouche (400288)
 * Implémentation de l'interface Trips utilisant un buffer structuré.
 */
public final class BufferedTrips implements Trips {

    private static final int ROUTE_ID = 0;
    private static final int DESTINATION_ID = 1;

    private final List<String> stringTable;
    private final StructuredBuffer buffer;

    private static final Structure STRUCTURE = new Structure(
            Structure.field(ROUTE_ID, Structure.FieldType.U16),
            Structure.field(DESTINATION_ID, Structure.FieldType.U16)
    );

    /**
     * Construit une instance de BufferedTrips avec une table de chaînes et un buffer de données.
     *
     * @param stringTable La liste des noms de destinations.
     * @param buffer      Le buffer contenant les données des trajets.
     */
    public BufferedTrips(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.buffer = new StructuredBuffer(STRUCTURE, buffer);
    }

    /**
     * Retourne l'identifiant de la route associée au trajet d'index donné.
     *
     * @param id L'index du trajet.
     * @return L'identifiant de la route.
     */
    @Override
    public int routeId(int id) {
        return buffer.getU16(ROUTE_ID, id);
    }

    /**
     * Retourne le nom de la destination du trajet d'index donné.
     *
     * @param id L'index du trajet.
     * @return Le nom de la destination.
     */
    @Override
    public String destination(int id) {
        int index = buffer.getU16(DESTINATION_ID, id);
        return stringTable.get(index);
    }

    /**
     * Retourne la taille du buffer, correspondant au nombre de trajets stockés.
     *
     * @return La taille du buffer.
     */
    @Override
    public int size() {
        return buffer.size();
    }
}
