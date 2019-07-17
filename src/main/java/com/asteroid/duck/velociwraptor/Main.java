package com.asteroid.duck.velociwraptor;

import com.asteroid.duck.velociwraptor.model.JsonTemplateData;
import com.asteroid.duck.velociwraptor.model.TemplateData;
import com.asteroid.duck.velociwraptor.template.TemplateRoot;
import com.asteroid.duck.velociwraptor.template.fs.FileSystemTemplateRoot;
import com.asteroid.duck.velociwraptor.user.ConsoleInteractive;
import com.asteroid.duck.velociwraptor.user.UserInteractive;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Entry point for command line
 */
public class Main {
    /**
     * Logger (SLF4J)
     */
    private static final Logger LOG = getLogger(Main.class);

    /**
     * Current version of this application
     */
    public static final String VERSION = loadVersionFromProperties();

    private static final char DIR = 'd';
    private static final char ZIP = 'z';
    private static final char MAVEN = 'm';
    private static final char GITHUB = 'g';
    private static final char OUT = 'o';
    private static final char SYS_PROPS = 'p';
    private static final char ENV = 'e';
    private static final char JSON = 'j';
    private static final char REPO = 'r';
    private static final char QUIET = 'q';
    private static final char ZIP_ROOT = 'i';
    private static final char CACHE = 'c';
    private static final char NO_COLOR = 'x';

    private static String loadVersionFromProperties() {
        try {
            Properties properties = new Properties();
            properties.load(Main.class.getResourceAsStream("/project-info.properties"));
            return properties.getProperty("version", "not-specified");
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return "no project-info.properties";
    }

    /**
     * Must specify a template:
     * <ul>
     *     <li>The path to a local template folder</li>
     *     <li>The path to a local template ZIP file</li>
     *     <li>A maven co-ordinate (see optional repository URL)</li>
     *     <li>A github repository user/repo/branch (optional branch)</li>
     * </ul>
     * May specify a target directory either relative or absolute, if not specified current working dir is used
     * May disable interactive mode
     * May give maven repository URL
     * @return
     */
    private static final Options options() {
        Options options = new Options();

        Option disableInteractive = Option.builder(opt(QUIET)).longOpt("quiet")
                .desc("Disable interactive mode")
                .build();

        options.addOption(disableInteractive);

        Option repo = Option.builder(opt(REPO)).longOpt("repo")
                .desc("URI to maven or github repo when using those template sources")
                .hasArg(true).argName("URI").optionalArg(false)
                .build();
        options.addOption(repo);

        Option zipRoot = Option.builder(opt(ZIP_ROOT)).longOpt("zip-root")
                .desc("path to template root inside ZIP/JAR")
                .hasArg(true).argName("PATH").optionalArg(false)
                .build();
        options.addOption(zipRoot);

        Option retainCache = Option.builder(opt(CACHE)).longOpt("retain")
                .desc("Retain any cached downloads of ZIP/JAR files")
                .build();
        options.addOption(retainCache);

        Option noColor = Option.builder(opt(NO_COLOR)).longOpt("no-color")
                .desc("No color in console")
                .build();
        options.addOption(noColor);

        OptionGroup template = new OptionGroup();
        template.setRequired(true);
        Option dir = Option.builder(opt(DIR)).longOpt("dir")
                .desc("Use a local directory <DIR> as template")
                .hasArg(true).argName("DIR").optionalArg(false)
                .build();
        template.addOption(dir);

        Option zip = Option.builder(opt(ZIP)).longOpt("zip")
                .desc("Use a ZIP file (local file/public web URI) as a template")
                .hasArg(true).argName("URI").optionalArg(false)
                .build();
        template.addOption(zip);

        Option mvn = Option.builder(opt(MAVEN)).longOpt("mvn")
                .desc("Use a maven artefact (JAR) as a template. Use maven ':' separated coordinate syntax - see [1], [2].")
                .hasArg(true).argName("MVN").optionalArg(false)
                .build();
        template.addOption(mvn);

        Option github = Option.builder(opt(GITHUB)).longOpt("github")
                .desc("Use a GitHub repo as the template. Repository is defined as {user}/{repo}/{branch} (branch optional). See [1]")
                .hasArg(true).argName("REPO").optionalArg(false)
                .build();
        template.addOption(github);

        options.addOptionGroup(template);

        Option target = Option.builder(opt(OUT)).longOpt("output")
                .desc("Target directory for template output")
                .hasArg(true).argName("OUT").optionalArg(false)
                .build();
        options.addOption(target);

        Option sysProps = Option.builder(opt(SYS_PROPS)).longOpt("properties")
                .desc("Use system properties to resolve template variables")
                .build();
        options.addOption(sysProps);

        Option env = Option.builder(opt(ENV)).longOpt("env")
                .desc("Use system ENVIRONMENT to resolve template variables")
                .build();
        options.addOption(env);

        Option userJson = Option.builder(opt(JSON)).longOpt("json")
                .desc("Use JSON file(s) to resolve template variables. If a list, filenames are separated by ';'." +
                        "Relative paths are resolved based on current working dir")
                .hasArg(true).argName("JSON")
                .build();
        options.addOption(userJson);

        return options;
    }

    private static String opt(char c) {
        return new String(new char[] {c});
    }


    public static void main(String[] args) {
        System.out.println("Velociwraptor v"+VERSION);
        final Options options = options();
        CommandLineParser parser = new DefaultParser();
        try {
            final File currentWorkingDir = new File(".");
            CommandLine commandLine = parser.parse(options, args);
            // use colors in console
            boolean noColors = commandLine.hasOption(NO_COLOR);
            // create an interactive user if needed
            try (UserInteractive interactive = commandLine.hasOption('q') ? UserInteractive.nullInteractive() : ConsoleInteractive.console(noColors)) {
                // optional remote repository name (see later)
                Optional<String> repo = Optional.ofNullable(commandLine.getOptionValue('r'));
                // zip root (see later)
                Optional<String> zipRoot = Optional.ofNullable(commandLine.getOptionValue('i'));
                // retain cached ZIPs
                boolean retainCache = commandLine.hasOption('c');

                // output directory (if specified - else CWD)
                String target = commandLine.getOptionValue(OUT);
                final File targetDir = target == null ? currentWorkingDir : new File(target);

                // create a templateRoot
                TemplateRoot templateRoot = null;
                if (commandLine.hasOption(DIR)) {
                    // using local directory
                    Path path = Paths.get(commandLine.getOptionValue(DIR));
                    templateRoot = new FileSystemTemplateRoot(path);
                }
                else if (commandLine.hasOption(ZIP)) {
                    // ZIP (local or remote)
                    URI path = URI.create(commandLine.getOptionValue(ZIP));
                    LOG.debug("ZIP templateRoot URI="+path);
                    if (!path.isAbsolute()) {
                        path = currentWorkingDir.toURI().resolve(path);
                    }
                    LOG.debug("Resolved to "+path);
                    if (!path.getScheme().equals("file")) {
                        LOG.debug("Downloading remote ZIP");
                        path = downloadAndMakeLocal(path, retainCache);
                        LOG.debug("Using local cache "+path);
                    }
                    templateRoot = FileSystemTemplateRoot.fromZip(Paths.get(path), zipRoot.orElse("."));
                }
                else if (commandLine.hasOption(MAVEN)) {
                    // maven repository coords
                    // e.g. com.google.auto.value:auto-value-annotations:1.6.3rc2
                    // http://central.maven.org/maven2/com/google/auto/value/auto-value-annotations/1.6.3rc2/auto-value-annotations-1.6.3rc2.jar
                    final String[] mavenCoords = commandLine.getOptionValue(MAVEN).split(":");
                    final String groupId = mavenCoords[0].replace('.', '/');
                    final String artifactId = mavenCoords[1];
                    final String version = mavenCoords[2];
                    // base URI
                    URI baseUri = URI.create(repo.orElse("http://central.maven.org/maven2/"));
                    URI path = URI.create(groupId + "/" + artifactId +"/" + version + "/" + artifactId + "-"+version +".jar");
                    URI uri = baseUri.resolve(path);

                    URI local = downloadAndMakeLocal(uri, retainCache);
                    templateRoot = FileSystemTemplateRoot.fromZip(Paths.get(local), zipRoot.orElse("."));
                }
                else if (commandLine.hasOption(GITHUB)) {
                    // github repository
                    // e.g. duckAsteroid/velociwraptor/templateRoot
                    // https://github.com/duckAsteroid/velociwraptor/archive/template.zip
                    final String[] githubCoords = commandLine.getOptionValue(GITHUB).split("\\/");
                    if (githubCoords.length < 2) {
                        throw new ParseException("Incorrect format for github repo co-ords");
                    }
                    final String owner = githubCoords[0];
                    final String repository = githubCoords[1];
                    final String branch = (githubCoords.length > 1) ? githubCoords[2] : "master";
                    // base URI
                    URI baseUri = URI.create(repo.orElse("https://github.com/"));
                    URI path = URI.create(owner + "/" + repository +"/archive/"+branch +".zip");
                    URI uri = baseUri.resolve(path);

                    URI local = downloadAndMakeLocal(uri, retainCache);
                    templateRoot = FileSystemTemplateRoot.fromZip(Paths.get(local), zipRoot.orElse(repository+"-"+branch));
                }

                // if we have a templateRoot - create a session
                if (templateRoot != null) {
                    // do we need to setup some delegates
                    TemplateData templateData = null;
                    if (commandLine.hasOption(SYS_PROPS)) {
                        templateData = TemplateData.systemProperties(templateData);
                    }
                    if (commandLine.hasOption(ENV)) {
                        templateData = TemplateData.systemEnvironment(templateData);
                    }
                    if (commandLine.hasOption(JSON)) {
                        String[] jsonFiles = commandLine.getOptionValue(JSON).split(";");
                        for (String json : jsonFiles) {
                            Path jsonPath = Paths.get(json);
                            JsonReader reader = Json.createReader(Files.newBufferedReader(jsonPath));
                            templateData = new JsonTemplateData(null, templateData, reader.readObject(), interactive);
                        }
                    }
                    // add on any project defaults
                    JsonObject jsonObject = templateRoot.projectSettings();
                    if (jsonObject != null) {
                        templateData = new JsonTemplateData(null, templateData, jsonObject, interactive);
                    }
                    Session session = new Session(templateRoot.rootDirectory(), templateData, targetDir);
                    session.run();
                }
                else {
                    System.err.println("No recognised templateRoot source specified");
                    doHelp(options);
                }
            }
        }
        catch(ParseException e) {
            doHelp(options);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static URI downloadAndMakeLocal(URI path, boolean retainCache) throws IOException {
        File tempDirectory = new File(FileUtils.getTempDirectory(), "velociwraptor-cache");
        tempDirectory.mkdirs();
        String urlPath = path.getPath();
        String filename = urlPath.substring(urlPath.lastIndexOf('/') + 1);
        if (filename.endsWith(".jar")) {
            filename += ".zip";
        }
        File tempFile = new File(tempDirectory, filename);
        if (!retainCache) {
            FileUtils.forceDeleteOnExit(tempFile);
        }
        FileUtils.copyURLToFile(path.toURL(), tempFile);
        LOG.trace("Downloaded "+tempFile.getCanonicalPath());
        return tempFile.toURI();
    }


    private static void doHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(null);
        //System.out.println("usage: Main [--quiet] --dir <DIR> | --zip <URI> | --mvn <CO-ORDs> |" +
         //                  "       --github <CO-ORDs>    [--repo <URI>] [<TARGET>]");
        formatter.printHelp("Main", "", options,
                "[1] - Non standard repository URI may be specified with the --repo option.\n"+
                        "[2] - https://maven.apache.org/pom.html#Maven_Coordinates", true);
    }
}
