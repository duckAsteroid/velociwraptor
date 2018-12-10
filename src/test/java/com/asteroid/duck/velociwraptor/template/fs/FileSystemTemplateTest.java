package com.asteroid.duck.velociwraptor.template.fs;

import com.asteroid.duck.velociwraptor.Session;
import com.asteroid.duck.velociwraptor.model.JsonTemplateData;
import com.asteroid.duck.velociwraptor.template.Directory;
import com.asteroid.duck.velociwraptor.user.UserInteractive;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


import static org.junit.Assert.*;

public class FileSystemTemplateTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private FileSystemTemplateRoot subject;

    @Before
    public void setUp() throws Exception {
        File root = temporaryFolder.newFolder();
        FileUtils.write(new File(root, "default.json"), "{ \"Name\": \"Test\"}", StandardCharsets.UTF_8);
        File folder = new File(root, "template");
        folder.mkdirs();
        FileUtils.write(new File(folder, "test.txt"), "This is just\na ${Name}.\nDo not be alarmed!", StandardCharsets.UTF_8);
        FileUtils.write(new File(folder,"test2.txt"), "#end-template ignored comment\nThis is also just a ${Name}!", StandardCharsets.UTF_8);
        FileUtils.write(new File(folder, "${if NotDefined}should-not-appear.txt${end}"), "This is - Should not happen", StandardCharsets.UTF_8);
        File sub = new File(folder, "sub");
        sub.mkdirs();
        FileUtils.write(new File(sub, "sub-test.txt"), "This is in the sub folder.", StandardCharsets.UTF_8);
        File noDir = new File(folder, "${if NotDefined}no-folder${end}");
        noDir.mkdirs();
        FileUtils.write(new File(noDir, "not-here.txt"), "Not here", StandardCharsets.UTF_8);
        subject = new FileSystemTemplateRoot(root.toPath());
    }

    @After
    public void tearDown() throws Exception {
        temporaryFolder.delete();
    }

    @Test
    public void walkFileSystem() throws IOException {
        Directory directory = subject.rootDirectory();
        assertNotNull("directory");
        assertEquals("template", directory.rawName());

        List<com.asteroid.duck.velociwraptor.template.File> files = directory.childFiles().collect(Collectors.toList());
        assertNotNull(files);
        assertEquals(3, files.size());
        for (com.asteroid.duck.velociwraptor.template.File file : files) {
            String content = IOUtils.toString(file.rawContent(), StandardCharsets.UTF_8);
            assertTrue(content.contains("This is"));
        }

        List<Directory> directories = directory.childDirs().collect(Collectors.toList());
        assertNotNull(directories);
        assertEquals(2, directories.size());

        Directory sub = directories.get(1);
        assertNotNull(sub);
        assertEquals("sub", sub.rawName());

        assertEquals(0, sub.childDirs().count());

        com.asteroid.duck.velociwraptor.template.File subFile = sub.childFiles().findFirst().orElseThrow(FileNotFoundException::new);
        assertEquals("sub-test.txt", subFile.rawName());
        String content = IOUtils.toString(subFile.rawContent(), StandardCharsets.UTF_8);
        assertEquals("This is in the sub folder.", content);
    }

    @Test
    public void applyFileSystem() throws IOException {
        File output = temporaryFolder.newFolder();
        JsonTemplateData data = new JsonTemplateData(subject.projectSettings(), UserInteractive.nullInteractive());
        Session session = new Session(subject.rootDirectory(), data, output);
        session.run();

        assertFalse(Arrays.asList(output.list()).contains("should-not-appear.txt"));
        assertFalse(Arrays.asList(output.list()).contains("no-folder"));

        String content = FileUtils.readFileToString(new File(output, "test2.txt"), StandardCharsets.UTF_8);
        assertTrue(content.contains("${Name}"));
    }
}