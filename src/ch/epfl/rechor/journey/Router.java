package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.Transfers;
import ch.epfl.rechor.timetable.TimeTable;

import java.time.LocalDate;
import java.util.*;

/**
 * Router : implémentation “de base” du CSA avec payload
 * (payload = (indexPremièreConnexion << 8) | nombreArrêtsSautés).
 */
public record Router(TimeTable timeTable) {

    /** Construit le profil pour la date et la gare d’arrivée données. */
    public Profile profile(LocalDate date, int destStationId) {
        Connections conns   = timeTable.connectionsFor(date);
        Transfers   transf  = timeTable.transfers();

        /* -------- 1. pré‑calcul des temps de marche vers la destination -------- */
        Map<Integer,Integer> walk = new HashMap<>();
        int view = transf.arrivingAt(destStationId);
        for (int i = PackedRange.startInclusive(view); i < PackedRange.endExclusive(view); ++i) {
            walk.put(timeTable.stationId(transf.depStationId(i)),
                    transf.minutes(i));
        }

        /* -------- 2. structures CSA -------- */
        Profile.Builder profile_builder = new Profile.Builder(timeTable, date, destStationId);

        /* -------- 3. balayage des connexions (ordre départ décroissant) -------- */
        for (int i = 0; i < conns.size(); ++i) {
            ParetoFront.Builder connections_builder = new ParetoFront.Builder();

            final int firstConn = i;                 // pour le payload
            int depStop   = conns.depStopId(i);
            int arrStop   = conns.arrStopId(i);
            int depSt     = timeTable.stationId(depStop);
            int arrSt     = timeTable.stationId(arrStop);
            int arrTime   = conns.arrMins(i);
            int depTime = conns.depMins(i);
            int tripId    = conns.tripId(i);

            /* -- Option 1 : on descend et on termine à pied -- */
            Integer w = walk.get(arrSt);
            if (w != null)
                connections_builder.add(arrTime + w, 0, firstConn);


            if (profile_builder.forTrip(tripId) != null)
                connections_builder.addAll(profile_builder.forTrip(tripId));


            int arrSt1 = timeTable.stationId(arrStop);
            ParetoFront.Builder stFront = profile_builder.forStation(arrSt1);
            if (stFront != null) {
                stFront.build().forEach(crit -> {
                    if (PackedCriteria.depMins(crit) >= arrTime) {
                        connections_builder.add(
                                PackedCriteria.pack(
                                        PackedCriteria.arrMins(crit),
                                        PackedCriteria.changes(crit) + 1,
                                        firstConn));
                    }
                });
            }

            if (connections_builder.isEmpty()) continue;

            if (profile_builder.forTrip(tripId) == null) {
                profile_builder.setForTrip(tripId, new ParetoFront.Builder(connections_builder));
            } else {
                profile_builder.forTrip(tripId).addAll(connections_builder);
            }

            if (profile_builder.forStation(depSt) != null && profile_builder.forStation(depSt).fullyDominates(connections_builder, depTime))
                continue;

            int transfers = transf.arrivingAt(depSt);
            for (int tId = PackedRange.startInclusive(transfers); tId < PackedRange.endExclusive(transfers); tId++) {
                int depTime_transf = depTime - transf.minutes(tId);
                int depStId_transf = transf.depStationId(tId);
                if (profile_builder.forStation(depStId_transf) == null)
                {
                    profile_builder.setForStation(depStId_transf, new ParetoFront.Builder());
                }
                connections_builder.forEach(crit -> {
                    int posPayload   = conns.tripPos(PackedCriteria.payload(crit));
                    int posFirstConn = conns.tripPos(firstConn);
                    int stops        = posPayload - posFirstConn;
                    if (stops < 0) stops = 0;
                    if (stops > 0xFF) stops = 0xFF;
                    profile_builder.forStation(depStId_transf).add(
                            PackedCriteria.withPayload(
                                    PackedCriteria.withDepMins(crit, depTime_transf),
                                    Bits32_24_8.pack(firstConn, stops)
                            ));
                });
            }

        }
        return profile_builder.build();
    }
}