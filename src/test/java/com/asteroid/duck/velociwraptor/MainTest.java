package com.asteroid.duck.velociwraptor;

import org.apache.commons.io.FileUtils;
import org.hamcrest.collection.IsArrayContaining;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.hamcrest.io.FileMatchers.*;

public class MainTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    public void runMain(String ... args) {
        Main.main(args);
    }

    @Test
    public void remoteZipTest() throws IOException {
        File output = temporaryFolder.newFolder("remote-zip-test");
        runMain("-q",
                "-i", "velociwraptor-template",
                "-z", "https://github.com/duckAsteroid/velociwraptor/archive/template.zip",
                "-o", output.getAbsolutePath());


        assertFolderContains(output, true, "injected.txt", "test.txt", "test2.txt");
        assertFolderContains(output, false, "abc", "sub");

        File injected = new File(output, "injected.txt");
        String content = FileUtils.readFileToString(injected, StandardCharsets.UTF_8);
        assertThat(content, containsString("Brick"));
    }

    private void assertFolderContains(File folder, boolean areFiles, String ... fileNames) {
        FileFilter filter = areFiles ? File::isFile : File::isDirectory;
        List<File> children = Arrays.asList(folder.listFiles(filter));
        assertEquals(fileNames.length, children.size());
        children.stream().forEach(file -> {
            assertThat(fileNames, new IsArrayContaining<>(equalToIgnoringCase(file.getName())));
            if (areFiles) {
                assertThat(file, aFileWithSize(greaterThan(0L)));
            }
        });
    }

    @Test
    public void githubTest() {
        runMain("-q",  "-g", "duckAsteroid/velociwraptor/template");
    }
}