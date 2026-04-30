package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.timetable.Transfers;
import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

/**
 * *  @author Valentin Walendy (393413)
 * *  @author Ruben Lellouche (400288)
 * Représente une collection de transferts entre stations sous forme de buffer structuré.
 * Cette implémentation permet un accès rapide aux informations de transfert en utilisant des indices pré-calculés.
 */
public final class BufferedTransfers implements Transfers {

    private static final int DEP_STATION_ID = 0;
    private static final int ARR_STATION_ID = 1;
    private static final int TRANSFER_MINUTES = 2;

    private final StructuredBuffer buffer;
    private final int[] arrivingAtTable;

    private static final Structure STRUCTURE = new Structure(
            Structure.field(DEP_STATION_ID, Structure.FieldType.U16),
            Structure.field(ARR_STATION_ID, Structure.FieldType.U16),
            Structure.field(TRANSFER_MINUTES, Structure.FieldType.U8)
    );

    /**
     * Construit un objet BufferedTransfers à partir d'un ByteBuffer contenant les données des transferts.
     * Il pré-calcule également un tableau permettant un accès rapide aux stations d'arrivée.
     *
     * @param buffer Le ByteBuffer contenant les informations des transferts.
     */
    public BufferedTransfers(ByteBuffer buffer) {
        this.buffer = new StructuredBuffer(STRUCTURE, buffer);

        int n = this.buffer.size();

                if (n == 0) {
                    this.arrivingAtTable = new int[0];
                } else {
                    int maxArrId = -1;
                    for (int i = 0; i < n; i++) {
                        int arrId = this.buffer.getU16(ARR_STATION_ID, i);
                        if (arrId > maxArrId) {
                            maxArrId = arrId;
                         }
                    }
                    this.arrivingAtTable = new int[maxArrId + 1];

                    int i = 0;
                    while (i < n) {
                        int currentArr = this.buffer.getU16(ARR_STATION_ID, i);
                        int start = i;
                        while (i < n && this.buffer.getU16(ARR_STATION_ID, i) == currentArr) {
                            i++;
                        }
                        int end = i;

                        int packedInterval = PackedRange.pack(start, end);
                        arrivingAtTable[currentArr] = packedInterval;
                    }
                }
    }

    /**
     * Retourne l'identifiant de la station de départ pour un transfert donné.
     *
     * @param id L'identifiant du transfert.
     * @return L'identifiant de la station de départ.
     */
    @Override
    public int depStationId(int id) {
        return buffer.getU16(DEP_STATION_ID, id);
    }


    /**
     * Retourne le temps de transfert en minutes pour un transfert donné.
     *
     * @param id L'identifiant du transfert.
     * @return La durée du transfert en minutes.
     */
    @Override
    public int minutes(int id) {
        return buffer.getU8(TRANSFER_MINUTES, id);
    }

    /**
     * Retourne un intervalle packé des transferts arrivant à une station donnée.
     *
     * @param stationId L'identifiant de la station d'arrivée.
     * @return Un entier représentant l'intervalle des transferts (startInclusive, endExclusive).
     */
    @Override
    public int arrivingAt(int stationId) {
        return arrivingAtTable[stationId];
    }

    /**
     * Retourne le temps de transfert entre une station de départ et une station d'arrivée données.
     * Si aucun transfert correspondant n'est trouvé, une exception est levée.
     *
     * @param depStationId L'identifiant de la station de départ.
     * @param arrStationId L'identifiant de la station d'arrivée.
     * @return La durée du transfert en minutes.
     * @throws NoSuchElementException Si aucun transfert entre ces deux stations n'existe.
     */
    @Override
    public int minutesBetween(int depStationId, int arrStationId) {
        int arr = arrivingAt(arrStationId);

        for (int i = PackedRange.startInclusive(arr); i < PackedRange.endExclusive(arr); i++) {
            if (depStationId(i) == depStationId) {
                return minutes(i);
            }
        }
        throw new NoSuchElementException();
    }

    /**
     * Retourne la taille du buffer, c'est-à-dire le nombre total de transferts stockés.
     *
     * @return Le nombre de transferts dans le buffer.
     */
    @Override
    public int size() {
        return buffer.size();
    }
}