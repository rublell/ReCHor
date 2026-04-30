package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.timetable.Connections;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * @author Valentin Walendy (393413)
 *  *  @author Ruben Lellouche (400288)
 * Implémentation de l'interface Connections utilisant des buffers pour stocker les données des connexions.
 */
public final class BufferedConnections implements Connections {

    private static final int DEP_STOP_ID = 0;
    private static final int DEP_MINUTES = 1;
    private static final int ARR_STOP_ID = 2;
    private static final int ARR_MINUTES = 3;
    private static final int TRIP_POS_ID = 4;

    private static final int NEXT_CONNECTION_ID = 0;

    private final StructuredBuffer buffer;
    private final IntBuffer succBuffer;

    private static final Structure STRUCTURE_BUFFER = new Structure(
            Structure.field(DEP_STOP_ID, Structure.FieldType.U16),
            Structure.field(DEP_MINUTES, Structure.FieldType.U16),
            Structure.field(ARR_STOP_ID, Structure.FieldType.U16),
            Structure.field(ARR_MINUTES, Structure.FieldType.U16),
            Structure.field(TRIP_POS_ID, Structure.FieldType.S32)
    );

    private static final Structure STRUCTURE_SUCC = new Structure(
            Structure.field(NEXT_CONNECTION_ID, Structure.FieldType.S32)
    );

    /**
     * Construit un objet BufferedConnections à partir des buffers donnés.
     *
     * @param buffer     Le ByteBuffer contenant les données des connexions.
     * @param succBuffer Le ByteBuffer contenant les ID des connexions suivantes.
     */
    public BufferedConnections(ByteBuffer buffer, ByteBuffer succBuffer) {
        this.buffer = new StructuredBuffer(STRUCTURE_BUFFER, buffer);
        this.succBuffer = succBuffer.asIntBuffer();
    }

    /**
     * Retourne l'identifiant de l'arrêt de départ de la connexion donnée.
     *
     * @param id L'identifiant de la connexion.
     * @return L'identifiant de l'arrêt de départ.
     */
    @Override
    public int depStopId(int id) {
        return buffer.getU16(DEP_STOP_ID, id);
    }

    /**
     * Retourne l'heure de départ en minutes après minuit de la connexion donnée.
     *
     * @param id L'identifiant de la connexion.
     * @return L'heure de départ en minutes.
     */
    @Override
    public int depMins(int id) {
        return buffer.getU16(DEP_MINUTES, id);
    }

    /**
     * Retourne l'identifiant de l'arrêt d'arrivée de la connexion donnée.
     *
     * @param id L'identifiant de la connexion.
     * @return L'identifiant de l'arrêt d'arrivée.
     */
    @Override
    public int arrStopId(int id) {
        return buffer.getU16(ARR_STOP_ID, id);
    }

    /**
     * Retourne l'heure d'arrivée en minutes après minuit de la connexion donnée.
     *
     * @param id L'identifiant de la connexion.
     * @return L'heure d'arrivée en minutes.
     */
    @Override
    public int arrMins(int id) {
        return buffer.getU16(ARR_MINUTES, id);
    }

    /**
     * Retourne l'identifiant du trajet auquel appartient la connexion donnée.
     *
     * @param id L'identifiant de la connexion.
     * @return L'identifiant du trajet.
     */
    @Override
    public int tripId(int id) {
        return Bits32_24_8.unpack24(buffer.getS32(TRIP_POS_ID, id));
    }

    /**
     * Retourne la position de la connexion dans le trajet correspondant.
     *
     * @param id L'identifiant de la connexion.
     * @return La position dans le trajet.
     */
    @Override
    public int tripPos(int id) {
        return Bits32_24_8.unpack8(buffer.getS32(TRIP_POS_ID, id));
    }

    /**
     * Retourne l'identifiant de la connexion suivante dans le trajet.
     *
     * @param id L'identifiant de la connexion.
     * @return L'identifiant de la connexion suivante, ou une valeur spéciale si aucune connexion suivante n'existe.
     */
    @Override
    public int nextConnectionId(int id) {
        return succBuffer.get(id);
    }

    /**
     * Retourne le nombre total de connexions stockées.
     *
     * @return Le nombre total de connexions.
     */
    @Override
    public int size() {
        return buffer.size();
    }
}
