package com.asteroid.duck.velociwraptor;

import io.javalin.Javalin;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.asteroid.duck.velociwraptor.AssertFile.assertStandardTemplateApplied;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VelociwraptorTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    public void runMain(File cwd, String ... args) {
        Main.runApplication(cwd, args);
    }

    @Test
    public void localOverride() throws IOException {
        File template = temporaryFolder.newFolder("template");
        OverrideJsonSessionTest.createBasicTemplate(template);

        File json = temporaryFolder.newFolder("json");

        File oneJson = new File(json, "1.json");
        FileUtils.write(oneJson, "{ \"Name\": \"Override-1\" }", StandardCharsets.UTF_8);
        File twoJson = new File(json, "2.json");
        FileUtils.write(twoJson, "{ \"Name\": \"Override-2\" }", StandardCharsets.UTF_8);

        File output = temporaryFolder.newFolder("local");

        runMain( json,"-q",
                "-d", template.getAbsolutePath(),
                "-j", "1.json;2.json",
                "-o", output.getAbsolutePath());

        File expectedFile = new File(output, "test.txt");
        assertTrue(expectedFile.exists());
        assertTrue(expectedFile.isFile());
        String content = FileUtils.readFileToString(expectedFile, StandardCharsets.UTF_8);
        assertEquals("Override-2", content);

        output = temporaryFolder.newFolder("local2");

        runMain(json,"-q",
                "-d", template.getAbsolutePath(),
                "-j", "2.json;1.json",
                "-o", output.getAbsolutePath());

        expectedFile = new File(output, "test.txt");
        assertTrue(expectedFile.exists());
        assertTrue(expectedFile.isFile());
        content = FileUtils.readFileToString(expectedFile, StandardCharsets.UTF_8);
        assertEquals("Override-1", content);
    }

    @Test
    public void remoteZipTest() throws IOException {
        File output = temporaryFolder.newFolder("remote-zip-test");
        runMain(temporaryFolder.getRoot(), "-q",
                "-i", "velociwraptor-template",
                "-z", "https://github.com/duckAsteroid/velociwraptor/archive/template.zip",
                "-o", output.getAbsolutePath());

        assertStandardTemplateApplied(output);
    }

    @Test
    public void githubTest() throws IOException {
        File output = temporaryFolder.newFolder("github-test");
        runMain(temporaryFolder.getRoot(),"-q",
                "-g", "duckAsteroid/velociwraptor/template",
                "-o", output.getAbsolutePath());

        assertStandardTemplateApplied(output);
    }

    @Test
    public void githubPartialTest() throws IOException {
        File output = temporaryFolder.newFolder("github-partial-test");
        runMain(temporaryFolder.getRoot(), "-q",
                "-g", "duckAsteroid/velociwraptor/template",
                "-o", output.getAbsolutePath());

        assertStandardTemplateApplied(output);
    }

    @Test
    public void mavenTest() throws IOException {
        final String groupId = "duckAsteroid";
        final String artifactId = "velociwraptor-test";
        final String version = "0.0.1";
        final int port = 12789;
        InputStream jarInput = VelociwraptorTest.class.getResourceAsStream("template.jar");
        byte[] bytes = IOUtils.toByteArray(jarInput);
        Javalin webServer = Javalin.create().start(port);
        webServer.get("/maven2/"+ groupId + "/" + artifactId +"/" + version + "/" + artifactId + "-"+version +".jar", (ctx) -> {
            ctx.contentType("application/java-archive");
            ctx.result( bytes);
        });

        File output = temporaryFolder.newFolder("maven-test");

        runMain(temporaryFolder.getRoot(), "-q", "-c",
                "-r", "http://localhost:"+port+"/maven2/",
                "-m", groupId+":"+artifactId+":"+version,
                "-o", output.getAbsolutePath());

        webServer.stop();

        assertStandardTemplateApplied(output);
    }
}
