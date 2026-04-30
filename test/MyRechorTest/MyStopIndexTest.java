package MyRechorTest;

import ch.epfl.rechor.StopIndex;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MyStopIndexTest {

    @Test
    void stopsMatching_withAlternateName_LosannaMapsToLausanne() {
        // Noms principaux disponibles
        List<String> names = List.of(
                "Lausanne",
                "Geneva",
                "Bern",
                "Zurich"
        );
        // Map des noms alternatifs vers noms principaux
        Map<String, String> alternates = Map.of(
                "Losanna", "Lausanne",
                "Bärn",     "Bern"
        );

        StopIndex index = new StopIndex(names, alternates);

        // Requête "Losanna" doit retourner Lausanne en première position
        List<String> results = index.stopsMatching("Losanna", 5);

        assertEquals(1, results.size(), "Le nombre de résultats doit être 1");
        assertEquals("Lausanne", results.get(0), "L'alternatif 'Losanna' doit correspondre à 'Lausanne'");
    }

    @Test
    void stopsMatchingAlternateName() {
        List<String> principals = List.of("Lausanne", "Genève", "Berne");
        Map<String, String> alternates = Map.of(
                "Losanna",    "Lausanne",
                "Berne-Ville","Berne"
        );
        StopIndex index = new StopIndex(principals, alternates);

        // “Losanna” doit renvoyer “Lausanne”
        List<String> result1 = index.stopsMatching("Losanna", 5);
        assertEquals(List.of("Lausanne"), result1);

        // “Berne-Ville” doit renvoyer “Berne”
        List<String> result2 = index.stopsMatching("Berne-Ville", 5);
        assertEquals(List.of("Berne"), result2);
    }



    @Test
    void stopsMatchingCaseInsensitive() {
        List<String> principals = List.of("Lausanne", "Genève", "Zurich");
        StopIndex index = new StopIndex(principals, Map.of());

        // la requête “LAU” (toutes majuscules) doit trouver “Lausanne”
        List<String> result = index.stopsMatching("LAU", 5);
        assertEquals(List.of("Lausanne"), result);
    }

    @Test
    void stopsMatchingMaxLimit() {
        List<String> principals = List.of("StopA", "StopB", "StopC", "StopD");
        StopIndex index = new StopIndex(principals, Map.of());

        // “stop” correspond à tous, mais on limite à 2
        List<String> result = index.stopsMatching("stop", 2);
        assertEquals(2, result.size());
        // Les deux résultats doivent provenir de la liste d’origine
        assertTrue(principals.containsAll(result));
    }

    @Test
    void stopsMatchingNoMatch() {
        List<String> principals = List.of("Lausanne", "Genève");
        StopIndex index = new StopIndex(principals, Map.of());

        // “xyz” ne correspond à rien → liste vide
        List<String> result = index.stopsMatching("xyz", 5);
        assertTrue(result.isEmpty());
    }

    @Test
    void stopsMatchingSubqueriesAndAccents() {
        List<String> principals = List.of(
                "Mézières FR, village",
                "Mézières VD, village",
                "Charleville-Mézières",
                "Renens VD"
        );
        StopIndex index = new StopIndex(principals, Map.of());

        List<String> result = index.stopsMatching("mez vil", 10);
        assertEquals(3, result.size(), "Devrait trouver trois arrêts correspondant à 'mez vil'");
        assertEquals("Mézières FR, village", result.get(0));
        assertEquals("Mézières VD, village", result.get(1));
        assertEquals("Charleville-Mézières",    result.get(2));
    }



    @Test
    void stopsMatchingMaxZeroReturnsEmpty() {
        List<String> principals = List.of("A", "B");
        StopIndex index = new StopIndex(principals, Map.of());

        // max = 0 doit renvoyer une liste vide, même si la requête correspond
        List<String> result = index.stopsMatching("A", 0);
        assertTrue(result.isEmpty(), "Avec max=0, on doit obtenir une liste vide");
    }

    @Test
    void stopsMatchingEmptyQueryReturnsEmpty() {
        List<String> principals = List.of("A", "B");
        StopIndex index = new StopIndex(principals, Map.of());

        // requête vide (ou espaces) doit renvoyer une liste vide
        List<String> result1 = index.stopsMatching("", 5);
        List<String> result2 = index.stopsMatching("   ", 5);
        assertTrue(result1.isEmpty(), "Requête vide → liste vide");
        assertTrue(result2.isEmpty(), "Requête espaces → liste vide");
    }

    @Test
    void stopsMatchingAlternateNoDuplicates() {
        List<String> principals = List.of("AA", "BB");
        Map<String, String> alternates = Map.of(
                "aa", "AA"
        );
        StopIndex index = new StopIndex(principals, alternates);

        // l'alternatif “aa” et le principal “AA” doivent apparaître une seule fois
        List<String> result = index.stopsMatching("aa", 5);
        assertEquals(1, result.size(), "Pas de doublon pour principal/alternatif");
        assertEquals("AA", result.get(0));
    }
}
