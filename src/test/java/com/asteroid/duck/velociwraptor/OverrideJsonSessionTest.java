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

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonString;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.asteroid.duck.velociwraptor.AssertFile.assertStandardTemplateApplied;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OverrideJsonSessionTest {
    public static final String EXPECTED_CONTENT = "This was here";
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private File target;
    private Session subject;
    private JsonTemplateData data;

    @Before
    public void setUp() throws Exception {
        target = temporaryFolder.newFolder("session-test");
        File template = temporaryFolder.newFolder("template-test");

        createBasicTemplate(template);

        UserInteractive interactive = UserInteractive.nullInteractive();

        FileSystemTemplateRoot templateRoot = new FileSystemTemplateRoot(template.toPath());

        JsonObject firstOverride = Json.createObjectBuilder().add("Name", "Override-1").build();
        JsonTemplateData firstTemplate = new JsonTemplateData(firstOverride, interactive);
        JsonObject secondOverride = Json.createObjectBuilder().add("Name", "Override-2").build();
        JsonTemplateData secondTemplate = new JsonTemplateData(null, firstTemplate, secondOverride, interactive);
        data = new JsonTemplateData(null, secondTemplate, templateRoot.projectSettings(), interactive);
        subject = new Session(templateRoot.rootDirectory(), data, target);
    }

    public static void createBasicTemplate(File template) throws IOException {
        FileUtils.write(new File(template, "default.json"), "{ \"Name\": \"Default\" }", StandardCharsets.UTF_8);
        File sub = new File(template, "template");
        sub.mkdirs();
        FileUtils.write(new File(sub, "test.txt"), "${Name}", StandardCharsets.UTF_8);
    }

    @After
    public void tearDown() throws Exception {
        //FileUtils.forceDelete(target);
    }

    @Test
    public void dataOverride() {
        JsonString actual = (JsonString) data.get("Name");
        assertEquals("Override-1", actual.getString());
    }

    @Test
    public void testOverrideFiles() throws IOException {
        subject.run();
        File expectedFile = new File(target, "test.txt");
        assertTrue(expectedFile.exists());
        assertTrue(expectedFile.isFile());
        String content = FileUtils.readFileToString(expectedFile, StandardCharsets.UTF_8);
        assertEquals("Override-1", content);
    }
}