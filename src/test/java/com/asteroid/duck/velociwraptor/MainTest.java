package com.asteroid.duck.velociwraptor;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import spark.Spark;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.asteroid.duck.velociwraptor.AssertFile.assertStandardTemplateApplied;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MainTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    public void runMain(String ... args) {
        Main.main(args);
    }

    @Test
    public void localOverride() throws IOException {
        String userDir = System.getProperty("user.dir");
        File template = temporaryFolder.newFolder("template");
        OverrideJsonSessionTest.createBasicTemplate(template);

        File json = temporaryFolder.newFolder("json");
        System.setProperty("user.dir", json.getAbsolutePath());

        File oneJson = new File(json, "1.json");
        FileUtils.write(oneJson, "{ \"Name\": \"Override-1\" }", StandardCharsets.UTF_8);
        File twoJson = new File(json, "2.json");
        FileUtils.write(twoJson, "{ \"Name\": \"Override-2\" }", StandardCharsets.UTF_8);

        File output = temporaryFolder.newFolder("local");

        runMain("-q",
                "-d", template.getAbsolutePath(),
                "-j", oneJson.getAbsolutePath() + ";"+twoJson.getAbsolutePath(),
                "-o", output.getAbsolutePath());

        File expectedFile = new File(output, "test.txt");
        assertTrue(expectedFile.exists());
        assertTrue(expectedFile.isFile());
        String content = FileUtils.readFileToString(expectedFile, StandardCharsets.UTF_8);
        assertEquals("Override-1", content);

        output = temporaryFolder.newFolder("local2");

        runMain("-q",
                "-d", template.getAbsolutePath(),
                "-j", twoJson.getAbsolutePath() + ";"+oneJson.getAbsolutePath(),
                "-o", output.getAbsolutePath());

        expectedFile = new File(output, "test.txt");
        assertTrue(expectedFile.exists());
        assertTrue(expectedFile.isFile());
        content = FileUtils.readFileToString(expectedFile, StandardCharsets.UTF_8);
        assertEquals("Override-2", content);
    }

    @Test
    public void remoteZipTest() throws IOException {
        File output = temporaryFolder.newFolder("remote-zip-test");
        runMain("-q",
                "-i", "velociwraptor-template",
                "-z", "https://github.com/duckAsteroid/velociwraptor/archive/template.zip",
                "-o", output.getAbsolutePath());

        assertStandardTemplateApplied(output);
    }

    @Test
    public void githubTest() throws IOException {
        File output = temporaryFolder.newFolder("github-test");
        runMain("-q",
                "-g", "duckAsteroid/velociwraptor/template",
                "-o", output.getAbsolutePath());

        assertStandardTemplateApplied(output);
    }

    @Test
    public void githubPartialTest() throws IOException {
        File output = temporaryFolder.newFolder("github-partial-test");
        runMain("-q",
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
        InputStream jarInput = MainTest.class.getResourceAsStream("template.jar");
        byte[] bytes = IOUtils.toByteArray(jarInput);
        Spark.port(port);
        Spark.get("/maven2/"+ groupId + "/" + artifactId +"/" + version + "/" + artifactId + "-"+version +".jar", ((request, response) -> {
            response.type("application/java-archive");
            return bytes;
        }));

        File output = temporaryFolder.newFolder("maven-test");

        runMain("-q", "-c",
                "-r", "http://localhost:"+port+"/maven2/",
                "-m", groupId+":"+artifactId+":"+version,
                "-o", output.getAbsolutePath());

        Spark.stop();

        assertStandardTemplateApplied(output);
    }
}