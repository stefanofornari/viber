/*
 * Copyright 2026 the original author or authors from the Viber project
 * (https://stefanofornari.github.io/viber).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ste.ai.viber.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * Describes the project Viber is operating on: the working directory and the
 * project name. The name is the first non-blank level-1 heading of
 * {@code uzz/project/overview.md} in the working directory; when that file is
 * missing, unreadable or has no such heading, the name of the working
 * directory's folder is used instead. Both values are read once, when the
 * instance is created.
 */
public class ProjectInfo {

    /** Location of the project overview, relative to the working directory. */
    public static final String OVERVIEW = "uzz/project/overview.md";

    private static final String HEADING_PREFIX = "# ";

    private final Path workingDirectory;
    private final String name;

    /**
     * Creates the description of the project located in the given directory.
     *
     * @param workingDirectory the project working directory; never null
     */
    public ProjectInfo(final Path workingDirectory) {
        Objects.requireNonNull(workingDirectory, "workingDirectory");
        this.workingDirectory = workingDirectory.toAbsolutePath().normalize();
        this.name = headingOf(this.workingDirectory.resolve(OVERVIEW))
            .orElseGet(this::folderName);
    }

    /**
     * @return the absolute path of the project working directory
     */
    public Path workingDirectory() {
        return workingDirectory;
    }

    /**
     * @return the project name, never blank
     */
    public String name() {
        return name;
    }

    private Optional<String> headingOf(final Path overview) {
        try {
            return Files.readAllLines(overview).stream()
                .filter(line -> line.startsWith(HEADING_PREFIX))
                .map(line -> line.substring(HEADING_PREFIX.length()).strip())
                .filter(title -> !title.isEmpty())
                .findFirst();
        } catch (IOException | SecurityException x) {
            // a missing or unreadable overview is not an error
            return Optional.empty();
        }
    }

    private String folderName() {
        final Path folder = workingDirectory.getFileName();
        return (folder == null) ? workingDirectory.toString() : folder.toString();
    }
}
