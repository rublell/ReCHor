package ch.epfl.rechor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public sealed interface Json permits Json.JArray, Json.JObject, Json.JString, Json.JNumber {

    record JArray(List<Json> values) implements Json {
        @Override
        public String toString() {
            return values.stream()
                    .map(Json::toString)
                    .collect(Collectors.joining(",", "[", "]"));
        }
    }

    record JObject(Map<String, Json> members) implements Json {
        @Override
        public String toString() {
            return members.entrySet().stream()
                    .map(e -> '"' + e.getKey() + '"' + ":" + e.getValue().toString())
                    .collect(Collectors.joining(",", "{", "}"));
        }
    }

    record JString(String value) implements Json {
        @Override
        public String toString() {
            return '"' + value + '"';
        }
    }

    record JNumber(double value) implements Json {
        @Override
        public String toString() {
            return Double.toString(value);
        }
    }
}
