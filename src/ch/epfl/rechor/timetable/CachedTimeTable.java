package ch.epfl.rechor.timetable;

import java.time.LocalDate;
import java.util.Objects;

/**
 *  @author Valentin Walendy (393413)
 *  @author Ruben Lellouche (400288)
 *
 *  Classe publique implémentant TimeTable avec cache pour les données dépendantes de la date.
 */
public final class CachedTimeTable implements TimeTable {
    //public final TimeTable underlying;
//a prendre en compte que en asistana ce matin c'etait en public
       private final TimeTable underlying;
    private LocalDate cachedDate;
    private Trips cachedTrips;
    private Connections cachedConnections;

    /**
     * Construit un CachedTimeTable déléguant à l'horaire donné.
     * @param underlying l'horaire sous-jacent à mettre en cache
     * @throws NullPointerException si underlying est null
     */
    public CachedTimeTable(TimeTable underlying) {
        this.underlying = Objects.requireNonNull(underlying);
        this.cachedDate = null;
        this.cachedTrips = null;
        this.cachedConnections = null;
    }

    @Override
    public Stations stations() {
        return underlying.stations();
    }

    @Override
    public StationAliases stationAliases() {
        return underlying.stationAliases();
    }

    @Override
    public Platforms platforms() {
        return underlying.platforms();
    }

    @Override
    public Routes routes() {
        return underlying.routes();
    }

    @Override
    public Transfers transfers() {
        return underlying.transfers();
    }

    @Override
    public Trips tripsFor(LocalDate date) {
        if (cachedDate == null || !cachedDate.equals(date) || cachedTrips == null) {
            cachedTrips = underlying.tripsFor(date);
            cachedDate = date;
        }
        return cachedTrips;
    }

    @Override
    public Connections connectionsFor(LocalDate date) {
        if (cachedDate == null || !cachedDate.equals(date) || cachedConnections == null) {
            cachedConnections = underlying.connectionsFor(date);
            cachedDate = date;
        }
        return cachedConnections;
    }

    // je ne pense pas que ce soit necessaire ici de les remettre

    //@Override
    //    public boolean isStationId(int stopId) {
    //        return underlying.isStationId(stopId);
    //    }
    //
    //    @Override
    //    public boolean isPlatformId(int stopId) {
    //        return underlying.isPlatformId(stopId);
    //    }
    //
    //    @Override
    //    public int stationId(int stopId) {
    //        return underlying.stationId(stopId);
    //    }
    //
    //    @Override
    //    public String platformName(int stopId) {
    //        return underlying.platformName(stopId);
    //    }
}
