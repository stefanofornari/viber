package ste.ai.viber.tools;

import dev.langchain4j.exception.ToolExecutionException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileSystemToolsTest {

    @Test
    void readFile_returns_content(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, "hello world");

        FileSystemTools tools = new FileSystemTools(tempDir.toString());
        String content = tools.readFile("test.txt");

        then(content).isEqualTo("hello world");
    }

    @Test
    void readFile_rejects_path_outside_basedir(@TempDir Path tempDir) throws Exception {
        FileSystemTools tools = new FileSystemTools(tempDir.toString());

        thenThrownBy(() -> tools.readFile("/etc/passwd"))
            .isInstanceOf(ToolExecutionException.class);
    }

    @Test
    void createFile_creates_file_in_basedir(@TempDir Path tempDir) throws Exception {
        FileSystemTools tools = new FileSystemTools(tempDir.toString());
        String result = tools.createFile("newfile.txt", "content");

        then(result).isEqualTo("File created");
        then(Files.readString(tempDir.resolve("newfile.txt"))).isEqualTo("content");
    }

    @Test
    void createFile_rejects_existing_path(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("existing.txt");
        Files.writeString(file, "exists");

        FileSystemTools tools = new FileSystemTools(tempDir.toString());

        thenThrownBy(() -> tools.createFile("existing.txt", "content"))
            .isInstanceOf(ToolExecutionException.class);
    }

    @Test
    void deleteFile_removes_file(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("todelete.txt");
        Files.writeString(file, "delete me");

        FileSystemTools tools = new FileSystemTools(tempDir.toString());
        String result = tools.deleteFile("todelete.txt");

        then(result).isEqualTo("File deleted");
        then(Files.exists(file)).isFalse();
    }

    @Test
    void listFilesInDirectory_lists_contents(@TempDir Path tempDir) throws Exception {
        Files.writeString(tempDir.resolve("a.txt"), "a");
        Files.createDirectory(tempDir.resolve("subdir"));

        FileSystemTools tools = new FileSystemTools(tempDir.toString());
        String listing = tools.listFilesInDirectory(".");

        then(listing).contains("a.txt");
        then(listing).contains("subdir/");
    }

    @Test
    void basedir_is_absolute_real_path(@TempDir Path tempDir) throws Exception {
        FileSystemTools tools = new FileSystemTools(tempDir.toString());

        then(tools.basedir()).isEqualTo(tempDir.toAbsolutePath().toString());
    }
}
