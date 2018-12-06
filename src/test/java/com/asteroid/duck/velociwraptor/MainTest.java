package com.asteroid.duck.velociwraptor;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.*;

public class MainTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    public void runMain(String ... args) {
        Main.main(args);
    }

    @Test
    public void remoteZipTest() {
        runMain("-q", "-i", "velociwraptor-template", "-z", "https://github.com/duckAsteroid/velociwraptor/archive/template.zip");
    }

    @Test
    public void githubTest() {
        runMain("-q",  "-g", "duckAsteroid/velociwraptor/template");
    }
}