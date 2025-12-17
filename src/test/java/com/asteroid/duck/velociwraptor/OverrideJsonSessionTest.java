package com.asteroid.duck.velociwraptor;

import com.asteroid.duck.velociwraptor.model.vars.JsonValueSource;
import com.asteroid.duck.velociwraptor.model.vars.TemplateDataModel;
import com.asteroid.duck.velociwraptor.model.vars.ValueSource;
import com.asteroid.duck.velociwraptor.template.fs.FileSystemTemplateRoot;
import com.asteroid.duck.velociwraptor.user.NullInteractive;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OverrideJsonSessionTest {
    public static final String EXPECTED_CONTENT = "This was here";
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private File target;
    private Session subject;
    private TemplateDataModel data;
    private final ObjectMapper mapper = new ObjectMapper();
    @Before
    public void setUp() throws Exception {
        target = temporaryFolder.newFolder("session-test");
        File template = temporaryFolder.newFolder("template-test");

        createBasicTemplate(template);

        NullInteractive interactive = new NullInteractive();

        FileSystemTemplateRoot templateRoot = new FileSystemTemplateRoot(template.toPath());

        var firstOverride = mapper.createObjectNode();
        firstOverride.put("Name", "Override-1");
        JsonValueSource firstTemplate = new JsonValueSource(ValueSource.JSON_FILES, "firstOverride", firstOverride);
        var secondOverride = mapper.createObjectNode();
        secondOverride.put("Name", "Override-2");
        JsonValueSource secondTemplate = new JsonValueSource(ValueSource.JSON_FILES, "secondOverride", secondOverride);

        data = new TemplateDataModel(List.of(firstTemplate, secondTemplate, templateRoot.projectSettings()), interactive);
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
        var actual = data.get("Name");
        assertEquals("Override-1", actual);
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