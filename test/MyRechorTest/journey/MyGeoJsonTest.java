package MyRechorTest.journey;

import ch.epfl.rechor.Json;
import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.JourneyGeoJsonConverter;
import ch.epfl.rechor.journey.Stop;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MyGeoJsonTest {
    public static void main(String[] args) {
        // Définition des arrêts pour quatre grandes villes suisses différentes
        Stop basel      = new Stop("Basel",     "",  7.588576, 47.559599);
        Stop lucerne    = new Stop("Lucerne",   "",  8.309307, 47.050168);
        Stop lugano     = new Stop("Lugano",    "",  8.951058, 46.003708);
        Stop stGallen   = new Stop("St. Gallen","",  9.376717, 47.423556);

        // Création des étapes du voyage
        Journey.Leg leg1 = new Journey.Leg() {
            @Override public LocalDateTime depTime()              { return LocalDateTime.of(2025, 5, 2, 8, 0);  }
            @Override public LocalDateTime arrTime()              { return LocalDateTime.of(2025, 5, 2, 9, 30); }
            @Override public Stop          depStop()              { return basel;                          }
            @Override public Stop          arrStop()              { return lucerne;                        }
            @Override public List<IntermediateStop> intermediateStops() { return List.of();                        }
        };

        Journey.Leg leg2 = new Journey.Leg() {
            @Override public LocalDateTime depTime()              { return LocalDateTime.of(2025, 5, 2, 9, 45); }
            @Override public LocalDateTime arrTime()              { return LocalDateTime.of(2025, 5, 2, 11, 0); }
            @Override public Stop          depStop()              { return lucerne;                        }
            @Override public Stop          arrStop()              { return lugano;                         }
            @Override public List<IntermediateStop> intermediateStops() { return List.of();                        }
        };

        Journey.Leg leg3 = new Journey.Leg() {
            @Override public LocalDateTime depTime()              { return LocalDateTime.of(2025, 5, 2, 11, 15);}
            @Override public LocalDateTime arrTime()              { return LocalDateTime.of(2025, 5, 2, 13, 30);}
            @Override public Stop          depStop()              { return lugano;                         }
            @Override public Stop          arrStop()              { return stGallen;                       }
            @Override public List<IntermediateStop> intermediateStops() { return List.of();                        }
        };

        // Construction et conversion du voyage
        Journey journey = new Journey(List.of(leg1, leg2, leg3));
        String geoJson = JourneyGeoJsonConverter.toGeoJson(journey);

        // Affichage du GeoJSON généré
        System.out.println(geoJson);
    }

    @Test
    void jsonPrimitivesToString() {
        // JString
        assertEquals("\"hello\"", new Json.JString("hello").toString());
        // JNumber
        assertEquals("123.0", new Json.JNumber(123).toString());
        assertEquals("123.456", new Json.JNumber(123.456).toString());
    }

    @Test
    void jsonCompositeToString() {
        // JArray
        Json.JArray arr = new Json.JArray(List.of(new Json.JNumber(1), new Json.JString("a")));
        assertEquals("[1.0,\"a\"]", arr.toString());
        // JObject
        Json.JObject obj = new Json.JObject(Map.of("key", new Json.JNumber(2)));
        assertEquals("{\"key\":2.0}", obj.toString());
    }

}
