/*
 * Copyright (c) 2015-2017, Statens vegvesen
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.vegvesen.nvdbapi.client.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import no.vegvesen.nvdbapi.client.model.Geometry;
import no.vegvesen.nvdbapi.client.model.datakatalog.JavaType;
import no.vegvesen.nvdbapi.client.model.roadnet.roadsysref.RoadSysRef;
import no.vegvesen.nvdbapi.client.util.Strings;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

public final class GsonUtil {

    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private GsonUtil() {}

    public static boolean hasNonBlankMember(JsonObject obj, String memberName) {
        return obj.has(memberName) && !Strings.isNullOrEmpty(obj.get(memberName).getAsString());
    }

    public static boolean hasNonNullBlankMember(JsonObject obj, String memberName) {
        return obj.has(memberName) && obj.isJsonNull();
    }

    public static Double parseDoubleMember(JsonObject obj, String path) {
        return getNode(obj, path).map(JsonElement::getAsDouble).orElse(null);
    }

    public static Integer parseIntMember(JsonObject obj, String path) {
        Optional<JsonPrimitive> e = parseNodeToJsonPrimitive(obj, path);
        return e.map(JsonElement::getAsInt).orElse(null);
    }

    public static Long parseLongMember(JsonObject obj, String path) {
        Optional<JsonPrimitive> e = parseNodeToJsonPrimitive(obj, path);
        return e.map(JsonElement::getAsLong).orElse(null);
    }

    public static String parseStringMember(JsonObject obj, String path) {
        Optional<JsonPrimitive> e = getNode(obj, path).map(JsonElement::getAsJsonPrimitive);
        if (e.isPresent() && !e.get().isString()) {
            throw new IllegalArgumentException(path + " did not contain a string.");
        }
        return e.map(JsonElement::getAsString).orElse(null);
    }

    public static <T> Stream<T> parseList(JsonObject obj, String path, Function<JsonElement, T> map){
        Optional<JsonArray> e = getNode(obj, path).map(JsonElement::getAsJsonArray);
        if (e.isPresent() && !e.get().isJsonArray())
            throw new IllegalArgumentException(path + " did not contain an array.");
        return e.map(s -> StreamSupport.stream(s.spliterator(), false).map(map))
                .orElse(Stream.empty());
    }

    public static Optional<String> parseOptionalStringMember(JsonObject obj, String path) {
        return getNode(obj, path)
            .map(JsonElement::getAsJsonPrimitive)
            .filter(JsonPrimitive::isString)
            .map(JsonElement::getAsString);
    }

    public static Boolean parseBooleanMember(JsonObject obj, String path) {
        Optional<JsonPrimitive> e = getNode(obj, path).map(JsonElement::getAsJsonPrimitive);
        if (e.isPresent() && !e.get().isBoolean()) {
            throw new IllegalArgumentException(path + " did not contain a boolean.");
        }
        return e.map(JsonElement::getAsBoolean).orElse(null);
    }

    public static LocalDateTime parseDateTimeMember(JsonObject obj, String path) {
        return Optional.ofNullable(parseStringMember(obj, path))
            .map(v -> v.contains("T") ? LocalDateTime.parse(v) : dateTimeFormatter.parse(v, LocalDateTime::from))
            .orElse(null);
    }

    public static LocalDate parseDateMember(JsonObject obj, String path) {
        return parseDateMember(obj, path, "yyyy-MM-dd");
    }

    public static LocalTime parseTimeMember(JsonObject obj, String path) {
        return Optional.ofNullable(parseStringMember(obj, path))
            .map(v -> LocalTime.parse(v, timeFormatter)).orElse(null);
    }

    public static LocalDate parseDateMember(JsonObject obj, String path, String pattern) {
        return Optional.ofNullable(parseStringMember(obj, path))
            .map(s -> LocalDate.parse(s, DateTimeFormatter.ofPattern(pattern)))
            .orElse(null);
    }

    public static List<String> parseStringListMember(JsonObject obj, String path) {
        return getNode(obj, path)
            .map(JsonElement::getAsJsonArray)
            .map(a -> StreamSupport.stream(a.spliterator(), false)
                .map(JsonElement::getAsString)
                .collect(Collectors.toList()))
            .orElse(Collections.emptyList());

    }

    public static List<Integer> parseIntListMember(JsonObject obj, String path) {
        return getNode(obj, path)
            .map(JsonElement::getAsJsonArray)
            .map(a -> StreamSupport.stream(a.spliterator(), false)
                .map(JsonElement::getAsInt)
                .collect(Collectors.toList()))
            .orElse(Collections.emptyList());

    }

    public static List<Long> parseLongListMember(JsonObject obj, String path) {
        return getNode(obj, path)
            .map(JsonElement::getAsJsonArray)
            .map(a -> StreamSupport.stream(a.spliterator(), false)
                .map(JsonElement::getAsLong)
                .collect(Collectors.toList()))
            .orElse(null);

    }

    public static Object parseAttributeValue(JsonObject obj, String path, JavaType datatype) {
        Optional<JsonElement> node = getNode(obj, path);
        if (!node.isPresent()) {
            return null;
        }

        JsonElement e = node.get();
        if (!e.isJsonPrimitive()) {
            throw new RuntimeException(path + "is not a value node.");
        }

        switch (datatype) {
            case NUMBER:
                return e.getAsNumber();
            case BOOLEAN:
                return e.getAsBoolean();
            default:
                return e.getAsString();
        }
    }

    public static Optional<JsonElement> getNode(JsonObject start, String path) {
        Optional<JsonElement> temp = Optional.ofNullable(start);

        String[] elements = path.split("\\.");
        for (String p : elements) {
            if (!temp.isPresent()) {
                return temp;
            }

            if (!temp.get().isJsonObject()) {
                throw new RuntimeException("Node " + p + "is not an object!");
            }


            temp = temp.map(JsonElement::getAsJsonObject)
                .map(o -> o.get(p))
                .filter(o -> !o.isJsonNull());
        }
        return temp;
    }

    public static Optional<JsonArray> getArray(JsonObject node, String path) {
        return getNode(node, path).map(JsonElement::getAsJsonArray);
    }

    public static <T> List<T> parseArray(JsonObject obj, String path, Function<JsonObject, T> parser) {
        return getArray(obj, path)
            .map(array ->
                StreamSupport.stream(array.spliterator(), false)
                    .map(JsonElement::getAsJsonObject)
                    .map(parser)
                    .collect(toList()))
            .orElseGet(Collections::emptyList);
    }

    public static Geometry parseGeometryMember(JsonObject obj, String path) {
        if (obj.has(path)) {
            return GeometryParser.parse(obj.getAsJsonObject(path));
        } else {
            return null;
        }
    }

    public static RoadSysRef parseRoadSysRefMember(JsonObject obj, String path) {
        if (obj.has(path) && obj.getAsJsonObject(path).size() > 0) {
            return RoadSysRefParser.parse(obj.getAsJsonObject(path));
        } else {
            return null;
        }
    }

    public static <T> Function<JsonObject, T> rt(Function<JsonObject, T> f) {
        return obj -> {
            try {
                return f.apply(obj);
            } catch (Exception e) {
                throw new RuntimeException("Error processing \n" + obj, e);
            }
        };
    }

    private static Optional<JsonPrimitive> parseNodeToJsonPrimitive(JsonObject obj, String path) {
        Optional<JsonPrimitive> jsonElement = getNode(obj, path).map(JsonElement::getAsJsonPrimitive);
        if (jsonElement.isPresent() && !jsonElement.get().isNumber()) {
            throw new IllegalArgumentException(path + " did not contain a number.");
        }
        return jsonElement;
    }
}
