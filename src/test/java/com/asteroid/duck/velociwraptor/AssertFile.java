package com.asteroid.duck.velociwraptor;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.*;

public class AssertFile {
    public static void assertFolderDoesNotContain(File folder, String ... fileNames) throws IOException {
        List<File> children = Arrays.asList(folder.listFiles());
        List<String> childNames = children.stream().map(File::getName).map(String::toLowerCase).collect(Collectors.toList());
        for(String bannedName : fileNames) {
            assertFalse(folder.getAbsolutePath()+" should not contain " +
                            bannedName.toLowerCase()+", and yet does.",
                    childNames.contains(bannedName.toLowerCase()));
        }
    }
    public static void assertFolderContains(File folder, boolean areFiles, boolean strict, String ... fileNames) throws IOException {
        FileFilter filter = areFiles ? File::isFile : File::isDirectory;
        List<File> children = Arrays.asList(folder.listFiles(filter));
        if (strict) {
            assertEquals(fileNames.length, children.size());
        }
        else {
            assertTrue(children.size() >= fileNames.length); // there may be more than was specified
        }
        List<String> childNames = children.stream().map(File::getName).map(String::toLowerCase).collect(Collectors.toList());

        for(String fileName : fileNames) {
            assertTrue("Cannot find "+fileName+" in "+
                    childNames.stream().collect(Collectors.joining(";")),
                    childNames.contains(fileName.toLowerCase()));
            File check = new File(folder, fileName);
            assertTrue(check.exists());
            if (areFiles) {
                String content = FileUtils.readFileToString(check, StandardCharsets.UTF_8);
                assertTrue(check.getAbsolutePath() + " is empty", content.length() > 0L);
            }
        }
    }

    public static void assertStandardTemplateApplied(File folder) throws IOException {
        assertStandardTemplateApplied(folder, true);
    }

    public static void assertStandardTemplateApplied(File folder, boolean strict) throws IOException {
        assertFolderContains(folder, true, strict,"injected.txt", "test.txt", "test2.txt", "conditional.txt");
        assertFolderContains(folder, false, strict, "abc", "sub");
        assertFolderDoesNotContain(folder, "should-not-appear.txt", "negative.txt");
        File injected = new File(folder, "injected.txt");
        String content = FileUtils.readFileToString(injected, StandardCharsets.UTF_8);
        assertThat(content, containsString("Brick"));
    }
}
