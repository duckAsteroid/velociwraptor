package com.asteroid.duck.velociwraptor;

import picocli.CommandLine;

import java.io.File;
import java.nio.file.Path;

import static picocli.CommandLine.*;
/**
 * Main executable class for Velociwraptor application.
 * Uses PicoCLI to handle command line arguments and execute the application.
 * Loads the {@link Velociwraptor} class and populates using command line args.
 */
public class Main {

    public static int runApplication(File currentWorkingDir, String[] args) {
        CommandLine commandLine = new CommandLine(new Velociwraptor());
        Velociwraptor velociwraptor = commandLine.getCommand();
        velociwraptor.setCurrentWorkingDir(currentWorkingDir);
        return commandLine.execute(args);
    }

	public static void main(String[] args) {
        System.exit(runApplication(currentWorkingDir(), args));
	}

    public static File currentWorkingDir() {
        return new File(".");
    }
}
