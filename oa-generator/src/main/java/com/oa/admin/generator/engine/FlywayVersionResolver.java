package com.oa.admin.generator.engine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlywayVersionResolver {

    private static final Pattern VERSION_PATTERN = Pattern.compile("^V(\\d+)__.*\\.sql$");

    public String nextVersion(Path migrationDir) throws IOException {
        int maxVersion = 0;
        if (Files.isDirectory(migrationDir)) {
            try (var stream = Files.list(migrationDir)) {
                Optional<Integer> max = stream
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .map(VERSION_PATTERN::matcher)
                        .filter(Matcher::matches)
                        .map(m -> Integer.parseInt(m.group(1)))
                        .max(Integer::compareTo);
                if (max.isPresent()) {
                    maxVersion = max.get();
                }
            }
        }
        return "V" + (maxVersion + 1);
    }
}
