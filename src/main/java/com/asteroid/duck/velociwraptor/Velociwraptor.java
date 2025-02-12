package com.asteroid.duck.velociwraptor;

import picocli.CommandLine;

import java.nio.file.Path;

import static picocli.CommandLine.*;

@Command(resourceBundle = "com.asteroid.duck.velociwraptor.Messages")
public class Velociwraptor {
	@Option(names = {"-q", "--quiet"})
	boolean disableInteractive = false;
	@Option(names={"-r", "--repo"})
	String repo;
	@Option(names={"-i", "--zip-root"})
	String zipRoot;
	@Option(names={"-c", "--retain"})
	boolean retainCache = true;
	@Option(names={"-x", "--no-color"})
	boolean noColor = false;
	@Option(names={"-p", "--properties"})
	boolean useSysProps = false;
	@Option(names={"-e", "--env"})
	boolean useEnvironment = false;
	@Option(required=true, names={"-o", "--output"})
	Path outputDir = Path.of(".");

	Path[] json;

	@ArgGroup(headingKey = "template", exclusive = true, multiplicity = "1")
	TemplateSource template;

	public static class TemplateSource {
		@Option(names = {"-d", "--dir"}, paramLabel = "DIR", required = true) String directory;
		@Option(names = {"-z", "--zip"}, paramLabel = "FILE", required = true) String zip;
		@Option(names = {"-m", "--mvn"}, paramLabel = "COORD", required = true) String maven;
		@Option(names = {"-g", "--git"}, paramLabel = "REPO", required = true) String github;
	}


	public static void main(String[] args) {
		int exitCode = new CommandLine(new Velociwraptor()).execute(args);
	}
}
