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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.BDDAssertions.then;

class ProjectInfoTest {

    @TempDir
    Path tmp;

    @Test
    void name_is_the_first_level_1_heading_of_overview() throws IOException {
        final Path dir = projectDir("acme", "# Viber\n\nSome text\n");

        then(new ProjectInfo(dir).name()).isEqualTo("Viber");
    }

    @Test
    void working_directory_is_the_absolute_path_of_the_given_directory() throws IOException {
        final Path dir = projectDir("acme", "# Viber\n");

        then(new ProjectInfo(dir).workingDirectory()).isEqualTo(dir.toAbsolutePath());
        then(new ProjectInfo(dir).workingDirectory().isAbsolute()).isTrue();
    }

    @Test
    void first_level_1_heading_wins() throws IOException {
        final Path dir = projectDir("acme", "## Intro\n\n# Alpha\n\ntext\n\n# Beta\n");

        then(new ProjectInfo(dir).name()).isEqualTo("Alpha");
    }

    @Test
    void heading_text_is_trimmed() throws IOException {
        final Path dir = projectDir("acme", "#   Viber   \n");

        then(new ProjectInfo(dir).name()).isEqualTo("Viber");
    }

    @Test
    void missing_overview_falls_back_to_the_folder_name() throws IOException {
        final Path dir = Files.createDirectories(tmp.resolve("acme"));

        then(new ProjectInfo(dir).name()).isEqualTo("acme");
    }

    @Test
    void unreadable_overview_falls_back_to_the_folder_name() throws IOException {
        final Path dir = Files.createDirectories(tmp.resolve("acme"));
        // a directory in place of the file makes reading it fail
        Files.createDirectories(dir.resolve("uzz/project/overview.md"));

        then(new ProjectInfo(dir).name()).isEqualTo("acme");
    }

    @Test
    void overview_with_invalid_encoding_falls_back_to_the_folder_name() throws IOException {
        final Path dir = Files.createDirectories(tmp.resolve("acme"));
        Files.createDirectories(dir.resolve("uzz/project"));
        Files.write(dir.resolve("uzz/project/overview.md"), new byte[] {'#', ' ', (byte) 0xC3, (byte) 0x28});

        then(new ProjectInfo(dir).name()).isEqualTo("acme");
    }

    @Test
    void overview_without_a_level_1_heading_falls_back_to_the_folder_name() throws IOException {
        final Path dir = projectDir("acme", "just text\n## Second level\n#NoSpace\n");

        then(new ProjectInfo(dir).name()).isEqualTo("acme");
    }

    @Test
    void blank_level_1_heading_falls_back_to_the_folder_name() throws IOException {
        final Path dir = projectDir("acme", "text\n# \n");

        then(new ProjectInfo(dir).name()).isEqualTo("acme");
    }

    @Test
    void blank_level_1_heading_is_skipped_in_favour_of_the_next_one() throws IOException {
        final Path dir = projectDir("acme", "# \n\n# Viber\n");

        then(new ProjectInfo(dir).name()).isEqualTo("Viber");
    }

    @Test
    void root_directory_falls_back_to_its_path() {
        final Path root = tmp.getRoot();

        then(new ProjectInfo(root).name()).isEqualTo(root.toString());
    }

    private Path projectDir(final String folder, final String overview) throws IOException {
        final Path dir = Files.createDirectories(tmp.resolve(folder));
        final Path project = Files.createDirectories(dir.resolve("uzz/project"));
        Files.writeString(project.resolve("overview.md"), overview);
        return dir;
    }
}
