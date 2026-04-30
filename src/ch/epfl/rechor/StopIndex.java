package ch.epfl.rechor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class StopIndex {

    private static final Map<Character, String> ACCENT_EQUIV = Map.of(
            'c', "cç",
            'a', "aáàâä",
            'e', "eéèêë",
            'i', "iíìîï",
            'o', "oóòôö",
            'u', "uúùûü"

    );

    private final List<String> primaryNames;
    private final Map<String, String> nameToPrincipal;

    public StopIndex(List<String> names, Map<String, String> alterantes){
        this.primaryNames = List.copyOf(names);

        Map<String, String> map = new HashMap<>(alterantes);
        for (String name : names) {
            map.putIfAbsent(name, name);
        }
        this.nameToPrincipal = Map.copyOf(map);
    }

    public List<String> stopsMatching(String query, int max) {
        if (max <= 0) return List.of();
        String[] subs = query.trim().split("\\s+");

        List<Pattern> patterns = Arrays.stream(subs)
                .map(this::buildPattern)
                .collect(Collectors.toList());

        int qLen = subs.length; 

        Map<String, Integer> principalScores = nameToPrincipal.entrySet().stream()
                .flatMap(e -> Stream.of(e.getKey()))
                .distinct()
                .map(name -> Map.entry(name, computeScore(name, patterns)))
                .filter(e -> e.getValue() > 0)
                .collect(Collectors.toMap(
                        nameScore -> nameToPrincipal.get(nameScore.getKey()),
                        Map.Entry::getValue,
                        Integer::max
                ));

        return principalScores.entrySet().stream()
                .sorted(Map.Entry.<String,Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(max)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private Pattern buildPattern(String sub) {
        boolean hasUpper = sub.chars().anyMatch(Character::isUpperCase);
        int flags = hasUpper ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        StringBuilder sb = new StringBuilder();
        for (char ch : sub.toCharArray()) {
            char lower = Character.toLowerCase(ch);
            if (ACCENT_EQUIV.containsKey(lower)) {
                sb.append('[').append(ACCENT_EQUIV.get(lower)).append(']');
            } else {
                sb.append(Pattern.quote(String.valueOf(ch)));
            }
        }
        return Pattern.compile(sb.toString(), flags);
    }

    private int computeScore(String name, List<Pattern> patterns) {
        int total = 0;
        int n = name.length();
        for (Pattern p : patterns) {
            Matcher m = p.matcher(name);
            if (!m.find()) return 0;
            int matchLen = m.end() - m.start();
            int base = (matchLen * 100) / n;
            int score = base;
            int start = m.start();
            int end = m.end();
            if (start == 0 || !Character.isLetter(name.charAt(start - 1))) {
                score *= 4;
            }
            if (end == n || !Character.isLetter(name.charAt(end))) {
                score *= 2;
            }
            total += score;
        }
        return total;
    }
}
