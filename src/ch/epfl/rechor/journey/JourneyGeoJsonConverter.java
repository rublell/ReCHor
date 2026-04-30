package ch.epfl.rechor.journey;

import ch.epfl.rechor.Json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class JourneyGeoJsonConverter {
    private JourneyGeoJsonConverter() {}

    public static String toGeoJson(Journey journey) {
        List<Json> coordinates = new ArrayList<>();
        double[] last = new double[]{Double.NaN, Double.NaN};
        boolean first = true;

        for (Journey.Leg leg : journey.legs()) {
            if (first) {
                addStop(coordinates, leg.depStop(), last);
                first = false;
            }
            for (Journey.Leg.IntermediateStop istop : leg.intermediateStops()) {
                addStop(coordinates, istop.stop(), last);
            }
            addStop(coordinates, leg.arrStop(), last);
        }

        Json geo = new Json.JObject(Map.of(
                "type", new Json.JString("LineString"),
                "coordinates", new Json.JArray(coordinates)
        ));
        return geo.toString();
    }

    private static void addStop(List<Json> coords, Stop stop, double[] last) {
        double lon = Math.round(stop.longitude() * 1e5) / 1e5;
        double lat = Math.round(stop.latitude() * 1e5) / 1e5;
        if (Double.isNaN(last[0]) || lon != last[0] || Double.isNaN(last[1]) || lat != last[1]) {
            coords.add(new Json.JArray(List.of(
                    new Json.JNumber(lon),
                    new Json.JNumber(lat)
            )));
            last[0] = lon;
            last[1] = lat;
        }
    }
}
