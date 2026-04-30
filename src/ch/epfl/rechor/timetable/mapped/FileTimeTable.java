package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.journey.ParetoFront;
import ch.epfl.rechor.timetable.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.List;

public record FileTimeTable(
        Path directory,
        List<String> stringTable,
        Stations stations,
        StationAliases stationAliases,
        Platforms platforms,
        Routes routes,
        Transfers transfers
) implements TimeTable {


    /**
     * *  @author Valentin Walendy (393413)
     * *  @author Ruben Lellouche (400288)
     *
     * Crée un FileTimeTable à partir d'un répertoire contenant les fichiers d'horaires.
     * Cette méthode lit la table des chaînes depuis "strings.txt" (encodage ISO-8859-1)
     * et mappe en mémoire les fichiers binaires pour les stations, alias, plateformes, routes et transferts.
     *
     * @param directory Le chemin du répertoire contenant les fichiers d'horaires.
     * @return Une instance de FileTimeTable.
     * @throws IOException En cas d'erreur d'E/S.
     */
    public static TimeTable in(Path directory) throws IOException {
        try {
            Path stringsPath = directory.resolve("strings.txt");
            List<String> strings = List.copyOf(Files.readAllLines(stringsPath, StandardCharsets.ISO_8859_1));

            ByteBuffer stationsBuffer = mapFile(directory.resolve("stations.bin"));
            Stations stations = new BufferedStations(strings, stationsBuffer);

            ByteBuffer aliasesBuffer = mapFile(directory.resolve("station-aliases.bin"));
            StationAliases stationAliases = new BufferedStationAliases(strings, aliasesBuffer);

            ByteBuffer platformsBuffer = mapFile(directory.resolve("platforms.bin"));
            Platforms platforms = new BufferedPlatforms(strings, platformsBuffer);

            ByteBuffer routesBuffer = mapFile(directory.resolve("routes.bin"));
            Routes routes = new BufferedRoutes(strings, routesBuffer);

            ByteBuffer transfersBuffer = mapFile(directory.resolve("transfers.bin"));
            Transfers transfers = new BufferedTransfers(transfersBuffer);

            return new FileTimeTable(directory, strings, stations, stationAliases, platforms, routes, transfers);
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    /**
     * Mappe en mémoire un fichier en mode lecture seule.
     *
     * @param filePath Le chemin du fichier à mapper.
     * @return Un ByteBuffer contenant les données du fichier.
     * @throws IOException En cas d'erreur d'accès.
     */
    private static ByteBuffer mapFile(Path filePath) throws IOException {
        try (FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.READ)) {
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
        }
    }

    @Override
    public Stations stations() {
        return stations;
    }

    @Override
    public StationAliases stationAliases() {
        return stationAliases;
    }

    @Override
    public Platforms platforms() {
        return platforms;
    }

    @Override
    public Routes routes() {
        return routes;
    }

    @Override
    public Transfers transfers() {
        return transfers;
    }

    /**
     * Retourne les courses pour la date donnée.
     * Le fichier des courses est attendu dans le sous-dossier correspondant à la date.
     *
     * Par exemple, pour la date 2025-03-18, il cherche le fichier "2025-03-18/trips.bin".
     *
     * @param date La date des courses.
     * @return Une instance de Trips.
     */
    @Override
    public Trips tripsFor(LocalDate date) {
        try {
            Path tripsPath = directory.resolve(date.toString()).resolve("trips.bin");
            ByteBuffer tripsBuffer = mapFile(tripsPath);
            return new BufferedTrips(stringTable, tripsBuffer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Retourne les connexions pour la date donnée.
     * Les fichiers des connexions sont attendus dans le sous-dossier correspondant à la date.
     * Par exemple, pour la date 2025-03-18, il cherche "2025-03-18/connections.bin" et "2025-03-18/connections-succ.bin".
     *
     * @param date La date des connexions.
     * @return Une instance de Connections.
     */
    @Override
    public Connections connectionsFor(LocalDate date) {
        try {
            Path connectionsPath = directory.resolve(date.toString()).resolve("connections.bin");
            Path connectionsSuccPath = directory.resolve(date.toString()).resolve("connections-succ.bin");
            ByteBuffer connectionsBuffer = mapFile(connectionsPath);
            ByteBuffer connectionsSuccBuffer = mapFile(connectionsSuccPath);
            return new BufferedConnections(connectionsBuffer, connectionsSuccBuffer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
