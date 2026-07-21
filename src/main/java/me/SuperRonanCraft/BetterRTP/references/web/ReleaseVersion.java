package me.SuperRonanCraft.BetterRTP.references.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class ReleaseVersion {

    private ReleaseVersion() {
    }

    static int compare(String left, String right) {
        ParsedVersion leftVersion = parse(left);
        ParsedVersion rightVersion = parse(right);

        int maxParts = Math.max(leftVersion.numbers().size(), rightVersion.numbers().size());
        for (int index = 0; index < maxParts; index++) {
            int comparison = Integer.compare(numberAt(leftVersion.numbers(), index),
                    numberAt(rightVersion.numbers(), index));
            if (comparison != 0) {
                return comparison;
            }
        }

        if (leftVersion.qualifier().isEmpty() && !rightVersion.qualifier().isEmpty()) {
            return 1;
        }
        if (!leftVersion.qualifier().isEmpty() && rightVersion.qualifier().isEmpty()) {
            return -1;
        }
        return leftVersion.qualifier().compareTo(rightVersion.qualifier());
    }

    static String displayName(String version) {
        String value = version == null ? "" : version.trim();
        return value.matches("(?i)^v\\d.*") ? value.substring(1) : value;
    }

    private static ParsedVersion parse(String version) {
        String normalized = displayName(version).toLowerCase(Locale.ROOT);
        String[] versionAndQualifier = normalized.split("-", 2);
        String[] numericParts = versionAndQualifier[0].split("\\.");
        List<Integer> numbers = new ArrayList<>(numericParts.length);
        for (String part : numericParts) {
            if (!part.matches("\\d+")) {
                throw new IllegalArgumentException("Unsupported version: " + version);
            }
            numbers.add(Integer.parseInt(part));
        }
        if (numbers.isEmpty()) {
            throw new IllegalArgumentException("Unsupported version: " + version);
        }
        String qualifier = versionAndQualifier.length == 2 ? versionAndQualifier[1] : "";
        return new ParsedVersion(List.copyOf(numbers), qualifier);
    }

    private static int numberAt(List<Integer> numbers, int index) {
        return index < numbers.size() ? numbers.get(index) : 0;
    }

    private record ParsedVersion(List<Integer> numbers, String qualifier) {
    }
}
