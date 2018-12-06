package com.asteroid.duck.velociwraptor;

import org.apache.commons.io.FileUtils;
import org.hamcrest.Matcher;
import org.hamcrest.collection.IsArrayContaining;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.io.FileMatchers.aFileWithSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class AssertFile {
    public static void assertFolderContains(File folder, boolean areFiles, boolean strict, String ... fileNames) {
        FileFilter filter = areFiles ? File::isFile : File::isDirectory;
        List<File> children = Arrays.asList(folder.listFiles(filter));
        assertThat(children.size(), strict ? equalTo(fileNames.length) : greaterThanOrEqualTo(fileNames.length));
        List<String> childNames = children.stream().map(File::getName).collect(Collectors.toList());

        Matcher<String>[] matchers = Arrays.asList(fileNames).stream().map(name -> equalToIgnoringCase(name)).collect(Collectors.toList()).toArray(new Matcher[fileNames.length]);
        assertThat(childNames, hasItems(matchers));
    }
    public static void assertStandardTemplateApplied(File folder) throws IOException {
        assertStandardTemplateApplied(folder, true);
    }

    public static void assertStandardTemplateApplied(File folder, boolean strict) throws IOException {
        assertFolderContains(folder, true, strict,"injected.txt", "test.txt", "test2.txt");
        assertFolderContains(folder, false, strict, "abc", "sub");

        File injected = new File(folder, "injected.txt");
        String content = FileUtils.readFileToString(injected, StandardCharsets.UTF_8);
        assertThat(content, containsString("Brick"));
    }
}
