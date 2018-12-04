package com.asteroid.duck.velociwraptor.template;

import com.asteroid.duck.velociwraptor.model.Directory;
import org.apache.commons.io.Charsets;
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
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class FileSystemTemplateTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private FileSystemTemplate subject;

    @Before
    public void setUp() throws Exception {
        File root = temporaryFolder.newFolder();
        FileUtils.write(new File(root, "project.json"), "{ \"Name\": \"Test\"}", StandardCharsets.UTF_8);
        File folder = new File(root, "template");
        folder.mkdirs();
        FileUtils.write(new File(folder, "test.txt"), "This is just\na test.\nDo not be alarmed!", StandardCharsets.UTF_8);
        FileUtils.write(new File(folder,"test2.txt"), "This is also just a test.", StandardCharsets.UTF_8);
        File sub = new File(folder, "sub");
        sub.mkdirs();
        FileUtils.write(new File(sub, "sub-test.txt"), "This is in the sub folder.", StandardCharsets.UTF_8);

        subject = new FileSystemTemplate(root.toPath());
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

        List<com.asteroid.duck.velociwraptor.model.File> files = directory.childFiles().collect(Collectors.toList());
        assertNotNull(files);
        assertEquals(2, files.size());
        for (com.asteroid.duck.velociwraptor.model.File file : files) {
            String content = IOUtils.toString(file.rawContent(), StandardCharsets.UTF_8);
            assertTrue(content.contains("This is"));
        }

        List<Directory> directories = directory.childDirs().collect(Collectors.toList());
        assertNotNull(directories);
        assertEquals(1, directories.size());

        Directory sub = directories.get(0);
        assertNotNull(sub);
        assertEquals("sub", sub.rawName());

        assertEquals(0, sub.childDirs().count());

        com.asteroid.duck.velociwraptor.model.File subFile = sub.childFiles().findFirst().orElseThrow(FileNotFoundException::new);
        assertEquals("sub-test.txt", subFile.rawName());
        String content = IOUtils.toString(subFile.rawContent(), StandardCharsets.UTF_8);
        assertEquals("This is in the sub folder.", content);
    }
}