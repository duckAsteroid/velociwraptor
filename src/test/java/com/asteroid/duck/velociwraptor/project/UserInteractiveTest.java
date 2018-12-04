package com.asteroid.duck.velociwraptor.project;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonString;
import java.io.*;
import java.nio.file.Files;
import java.util.Scanner;

import static org.junit.Assert.*;

public class UserInteractiveTest {
    private UserInteractive subject;

    private static Scanner wrap(String ... lines) {
        StringBuilder output = new StringBuilder();
        for(String line : lines) {
            output.append(line).append("\n");
        }
        byte[] data = output.toString().getBytes();
        return new Scanner(new ByteArrayInputStream(data));
    }

    @After
    public void tearDown() throws Exception {
        if (subject != null) {
            subject.close();
        }
    }

    @Test
    public void askOption() throws IOException {
        InputStream stream = UserInteractiveTest.class.getResourceAsStream("askOption1.bin");
        final byte[] expected = IOUtils.toByteArray(stream);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter printWriter = new PrintWriter(baos, true)) {
            subject = new UserInteractive(wrap("980", "-5", "0", "wibble", "2"), printWriter);
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            arrayBuilder.add("One");
            arrayBuilder.add("Two");
            arrayBuilder.add("Three");

            Object result = subject.askOption("Key", arrayBuilder.build());
            assertNotNull(result);
            assertTrue(result instanceof String);
            assertEquals("Two", result);



            printWriter.flush();
            //FileUtils.writeByteArrayToFile(new File("temp.bin"), baos.toByteArray());
            assertArrayEquals(expected, baos.toByteArray());
        }

    }

    @Test
    public void askOptionDefault() throws IOException {
        InputStream stream = UserInteractiveTest.class.getResourceAsStream("askOption2.bin");
        final byte[] expected = IOUtils.toByteArray(stream);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter printWriter = new PrintWriter(baos, true)) {
            subject = new UserInteractive(wrap(""), printWriter);
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            arrayBuilder.add("One");
            arrayBuilder.add("Two");
            arrayBuilder.add("Three");

            Object result = subject.askOption("Key", arrayBuilder.build());
            assertNotNull(result);
            assertTrue(result instanceof String);
            assertEquals("One", result);

            printWriter.flush();
            //FileUtils.writeByteArrayToFile(new File("temp.bin"), baos.toByteArray());
            assertArrayEquals(expected, baos.toByteArray());


        }

    }


    @Test
    public void askForDefault() throws IOException {
        InputStream stream = UserInteractiveTest.class.getResourceAsStream("askForDefault.bin");
        final byte[] expected = IOUtils.toByteArray(stream);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter printWriter = new PrintWriter(baos, true)) {
            subject = new UserInteractive(wrap(""), printWriter);

            Object result = subject.askFor("Key", "Default");
            assertNotNull(result);
            assertTrue(result instanceof String);
            assertEquals("Default", result);

            printWriter.flush();
            //FileUtils.writeByteArrayToFile(new File("temp.bin"), baos.toByteArray());
            assertArrayEquals(expected, baos.toByteArray());
        }
    }

    @Test
    public void askFor() throws IOException {
        InputStream stream = UserInteractiveTest.class.getResourceAsStream("askFor.bin");
        final byte[] expected = IOUtils.toByteArray(stream);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter printWriter = new PrintWriter(baos, true)) {
            subject = new UserInteractive(wrap("wibble"), printWriter);

            Object result = subject.askFor("Key", "Current");
            assertNotNull(result);
            assertTrue(result instanceof String);
            assertEquals("wibble", result);

            printWriter.flush();
            FileUtils.writeByteArrayToFile(new File("temp.bin"), baos.toByteArray());
            assertArrayEquals(expected, baos.toByteArray());
        }
    }
}