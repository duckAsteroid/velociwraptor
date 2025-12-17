package com.asteroid.duck.velociwraptor.template.fs;

import com.asteroid.duck.velociwraptor.Session;

import com.asteroid.duck.velociwraptor.model.vars.TemplateDataModel;
import com.asteroid.duck.velociwraptor.template.TemplateDirectory;
import com.asteroid.duck.velociwraptor.template.TemplateFile;
import com.asteroid.duck.velociwraptor.user.NullInteractive;
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

public class TemplateFileSystemTemplateTest {
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
        FileUtils.write(new File(folder,"test2.txt"), "#no-template\nNOT INCLUDED!!\n\n#end-template ignored comment\nThis is also just a ${Name}!", StandardCharsets.UTF_8);
        FileUtils.write(new File(folder, "#some.bin"), "#no-template This is treated as \n#begin-template\nbinary data! ${no processing} is done!!", StandardCharsets.UTF_8);
        FileUtils.write(new File(folder, "##some.bin"), "#end-template\nThis is ${Name} treated as \n#begin-template\ntemplate data! ${Name} is done!!", StandardCharsets.UTF_8);
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
        TemplateDirectory templateDirectory = subject.rootDirectory();
        assertNotNull("directory");
        assertEquals("template", templateDirectory.rawName());

        List<TemplateFile> templateFiles = templateDirectory.childFiles().collect(Collectors.toList());
        assertNotNull(templateFiles);
        assertEquals(5, templateFiles.size());
        for (TemplateFile templateFile : templateFiles) {
            String content = IOUtils.toString(templateFile.rawContent(), StandardCharsets.UTF_8);
            assertTrue(content.contains("This is"));
        }

        List<TemplateDirectory> directories = templateDirectory.childDirs().collect(Collectors.toList());
        assertNotNull(directories);
        assertEquals(2, directories.size());

        TemplateDirectory sub = directories.get(1);
        assertNotNull(sub);
        assertEquals("sub", sub.rawName());

        assertEquals(0, sub.childDirs().count());

        TemplateFile subTemplateFile = sub.childFiles().findFirst().orElseThrow(FileNotFoundException::new);
        assertEquals("sub-test.txt", subTemplateFile.rawName());
        String content = IOUtils.toString(subTemplateFile.rawContent(), StandardCharsets.UTF_8);
        assertEquals("This is in the sub folder.", content);
    }

    @Test
    public void applyFileSystem() throws IOException {
        File output = temporaryFolder.newFolder();
        var project = subject.projectSettings();
        TemplateDataModel model = new TemplateDataModel(List.of(project), new NullInteractive());
        Session session = new Session(subject.rootDirectory(), model, output);
        session.run();

        assertFalse(Arrays.asList(output.list()).contains("should-not-appear.txt"));
        assertFalse(Arrays.asList(output.list()).contains("no-folder"));


        // normal template processing
        File test2 = new File(output, "test2.txt");
        assertTrue(test2.exists());
        String content = FileUtils.readFileToString(test2, StandardCharsets.UTF_8);
        assertTrue(content.contains("${Name}"));
        assertFalse(content.contains("NOT INCLUDED"));

        // check that the binary file was not processed
        File binary = new File(output, "some.bin");
        assertTrue(binary.exists());
        assertTrue(FileUtils.readFileToString(binary, StandardCharsets.UTF_8).contains("${no processing}"));

        // this file gets a # name and is processed
        File notBinary = new File(output, "#some.bin");
        assertTrue(notBinary.exists());
        String notBinaryContent = FileUtils.readFileToString(notBinary, StandardCharsets.UTF_8);
        assertTrue(notBinaryContent.contains("Test is done!!"));


    }
}