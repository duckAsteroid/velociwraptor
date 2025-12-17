package com.asteroid.duck.velociwraptor.template.fs;

import com.asteroid.duck.velociwraptor.template.TemplateDirectory;
import com.asteroid.duck.velociwraptor.template.TemplateFile;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

public class ZipTemplateFileSystemTemplateTest {

    private FileSystemTemplateRoot subject;

    @Before
    public void setUp() throws Exception {
        URL zipUrl = ZipTemplateFileSystemTemplateTest.class.getResource("template.zip");
        Path path = Paths.get(zipUrl.toURI());
        FileSystem zipFs = FileSystems.newFileSystem(path, getClass().getClassLoader());
        Path root = zipFs.getPath(".");
        subject = new FileSystemTemplateRoot(root);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void walkFileSystem() throws IOException {
        TemplateDirectory templateDirectory = subject.rootDirectory();
        assertNotNull("directory");
        assertEquals("template", templateDirectory.rawName());

        List<TemplateFile> templateFiles = templateDirectory.childFiles().collect(Collectors.toList());
        assertNotNull(templateFiles);
        assertEquals(2, templateFiles.size());
        for (TemplateFile templateFile : templateFiles) {
            String content = IOUtils.toString(templateFile.rawContent(), StandardCharsets.UTF_8);
            assertTrue(content.contains("This is"));
        }

        List<TemplateDirectory> directories = templateDirectory.childDirs().collect(Collectors.toList());
        assertNotNull(directories);
        assertEquals(1, directories.size());

        TemplateDirectory sub = directories.get(0);
        assertNotNull(sub);
        assertEquals("sub", sub.rawName());

        assertEquals(0, sub.childDirs().count());

        TemplateFile subTemplateFile = sub.childFiles().findFirst().orElseThrow(FileNotFoundException::new);
        assertEquals("sub-test.txt", subTemplateFile.rawName());
        String content = IOUtils.toString(subTemplateFile.rawContent(), StandardCharsets.UTF_8);
        assertEquals("This is in the sub folder.", content);
    }
}