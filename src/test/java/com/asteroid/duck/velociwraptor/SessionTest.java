package com.asteroid.duck.velociwraptor;

import com.asteroid.duck.velociwraptor.model.JsonTemplateData;
import com.asteroid.duck.velociwraptor.model.TemplateData;
import com.asteroid.duck.velociwraptor.template.Directory;
import com.asteroid.duck.velociwraptor.template.TemplateRoot;
import com.asteroid.duck.velociwraptor.template.fs.FileSystemTemplateRoot;
import com.asteroid.duck.velociwraptor.user.UserInteractive;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.asteroid.duck.velociwraptor.AssertFile.assertStandardTemplateApplied;
import static org.junit.Assert.*;

public class SessionTest {
    public static final String EXPECTED_CONTENT = "This was here";
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private File target;
    private Session subject;

    @Before
    public void setUp() throws Exception {
        UserInteractive interactive = UserInteractive.nullInteractive();
        Path zipPath = Paths.get(SessionTest.class.getResource("template.jar").toURI());
        TemplateRoot templateRoot = FileSystemTemplateRoot.fromZip(zipPath);
        Directory root = templateRoot.rootDirectory();
        TemplateData data = new JsonTemplateData(templateRoot.projectSettings(), interactive);
        target = temporaryFolder.newFolder("session-test");
        subject = new Session(root, data, target);
    }

    @After
    public void tearDown() throws Exception {
        //FileUtils.forceDelete(target);
    }

    @Test
    public void targetContainsNonCollidingFiles() throws IOException {
        File nonColliding = new File(target, "non-colliding.txt");
        FileUtils.writeStringToFile(nonColliding, "This was here", StandardCharsets.UTF_8);

        subject.run();

        assertStandardTemplateApplied(target, false);

        assertTrue(nonColliding.exists());
        String content = FileUtils.readFileToString(nonColliding, StandardCharsets.UTF_8);
        assertEquals(EXPECTED_CONTENT, content);
    }

    @Test
    public void targetContainsCollidingSimpleFileName() throws IOException {
        File collider = new File(target, "test.txt");
        FileUtils.writeStringToFile(collider, "This was here", StandardCharsets.UTF_8);

        subject.run();

        assertStandardTemplateApplied(target, false);
        assertTrue(collider.exists());
        String content = FileUtils.readFileToString(collider, StandardCharsets.UTF_8);
        assertEquals(EXPECTED_CONTENT, content);
    }
}