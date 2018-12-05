package com.asteroid.duck.velociwraptor.template;

import com.asteroid.duck.velociwraptor.model.Directory;
import com.sun.nio.zipfs.ZipFileSystem;
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
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class ZipFileSystemTemplateTest {

    private FileSystemTemplate subject;

    @Before
    public void setUp() throws Exception {
        URL zipUrl = ZipFileSystemTemplateTest.class.getResource("template.zip");
        Path path = Paths.get(zipUrl.toURI());
        FileSystem zipFs = FileSystems.newFileSystem(path, getClass().getClassLoader());
        Path root = zipFs.getPath(".");
        subject = new FileSystemTemplate(root);
    }

    @After
    public void tearDown() throws Exception {
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