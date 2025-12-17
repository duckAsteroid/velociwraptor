package com.asteroid.duck.velociwraptor;


import com.asteroid.duck.velociwraptor.model.vars.*;
import com.asteroid.duck.velociwraptor.template.TemplateRoot;
import com.asteroid.duck.velociwraptor.template.fs.FileSystemTemplateRoot;
import com.asteroid.duck.velociwraptor.user.ConsoleInteractive;
import com.asteroid.duck.velociwraptor.user.NullInteractive;
import com.asteroid.duck.velociwraptor.user.UserInteractive;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Entry point for command line - holds parser CLI options for PicoCLI
 */
@Command(
        name = "velociwraptor",
        description = "Template generation tool",
        mixinStandardHelpOptions = true
)
public class Velociwraptor implements Runnable {
    /**
     * Logger (SLF4J)
     */
    private static final Logger LOG = getLogger(Velociwraptor.class);

    /**
     * Current version of this application
     */
    public static final String VERSION = loadVersionFromProperties();

    private File currentWorkingDir;

    public void setCurrentWorkingDir(File currentWorkingDir) {
        this.currentWorkingDir = currentWorkingDir;
    }


    @ArgGroup(exclusive = true, multiplicity = "1", heading = "Template source (choose one):%n")
    private TemplateSource templateSource;

    static class TemplateSource {

        public Optional<TemplateRoot> templateRoot(final File cwd, final TemplateDownloadOptions opts) {
            return dir(opts).or(() -> zip(cwd, opts)).or( () -> mvn(cwd, opts)).or(() -> github(cwd, opts));
        }

        @Option(names = {"-d", "--dir"}, description = "Use a local directory as template", paramLabel = "<dir>")
        private Optional<String> dir;

        private Optional<TemplateRoot> dir(TemplateDownloadOptions opts) {
            try {
                if (dir.isPresent()) {
                    Path path = Paths.get(dir.get());
                    return Optional.of(new FileSystemTemplateRoot(path));
                }
            }
            catch(IOException e) {
                LOG.error(e.getMessage());
            }
            return Optional.empty();
        }

        @Option(names = {"-z", "--zip"}, description = "Use a ZIP file (local file/public web URI) as a template", paramLabel = "<uri>")
        private Optional<String> zip;

        private Optional<TemplateRoot> zip(File cwd, TemplateDownloadOptions opts) {
            try {
                if (zip.isPresent()) {
                    // ZIP (local or remote)
                    URI path = URI.create(zip.get());
                    LOG.debug("ZIP templateRoot URI=" + path);
                    if (!path.isAbsolute()) {
                        path = cwd.toURI().resolve(path);
                    }
                    LOG.debug("Resolved to " + path);
                    if (!path.getScheme().equals("file")) {
                        LOG.debug("Downloading remote ZIP");
                        path = downloadAndMakeLocal(path, opts.retainCache);
                        LOG.debug("Using local cache " + path);
                    }
                    return Optional.of( FileSystemTemplateRoot.fromZip(Paths.get(path), opts.zipRoot.orElse(".")));
                }
            }
            catch(IOException ioe) {
                LOG.error(ioe.getMessage());
            }
            return Optional.empty();
        }

        @Option(names = {"-m", "--mvn"}, description = "Use a maven artefact (JAR) as a template. Maven ':' separated coordinate syntax", paramLabel = "<maven-coords>")
        private Optional<String> mvn;

        private Optional<TemplateRoot> mvn(File cwd, TemplateDownloadOptions opts) {
            try {
                if (mvn.isPresent()) {
                    // maven repository coords
                    // e.g. com.google.auto.value:auto-value-annotations:1.6.3rc2
                    // http://central.maven.org/maven2/com/google/auto/value/auto-value-annotations/1.6.3rc2/auto-value-annotations-1.6.3rc2.jar
                    final String[] mavenCoords = mvn.get().split(":");
                    final String groupId = mavenCoords[0].replace('.', '/');
                    final String artifactId = mavenCoords[1];
                    final String version = mavenCoords[2];
                    // base URI
                    URI baseUri = URI.create(opts.repo.orElse("http://central.maven.org/maven2/"));
                    URI path = URI.create(groupId + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".jar");
                    URI uri = baseUri.resolve(path);

                    URI local = downloadAndMakeLocal(uri, opts.retainCache);
                    return Optional.of(FileSystemTemplateRoot.fromZip(Paths.get(local), opts.zipRoot.orElse(".")));
                }
            }
            catch (IOException ioe) {
                LOG.error(ioe.getMessage());
            }
            return Optional.empty();
        }

        @Option(names = {"-g", "--github"}, description = "Use a GitHub repo as the template. Format: {user}/{repo}(/{branch})", paramLabel = "<github-coords>")
        private Optional<String> github;

        private Optional<TemplateRoot> github(File currentWorkingDir, TemplateDownloadOptions opts) {
            try {
                if(github.isPresent()) {
                    // github repository
                    // e.g. duckAsteroid/velociwraptor/templateRoot
                    // https://github.com/duckAsteroid/velociwraptor/archive/template.zip
                    final String[] githubCoords = github.get().split("\\/");
                    if (githubCoords.length < 2) {
                        throw new IllegalArgumentException("Incorrect format for github repo co-ords");
                    }
                    final String owner = githubCoords[0];
                    final String repository = githubCoords[1];
                    final String branch = (githubCoords.length > 2) ? githubCoords[2] : "master";
                    // base URI
                    URI baseUri = URI.create(opts.repo.orElse("https://github.com/"));
                    URI path = URI.create(owner + "/" + repository + "/archive/" + branch + ".zip");
                    URI uri = baseUri.resolve(path);

                    URI local = downloadAndMakeLocal(uri, opts.retainCache);
                    return Optional.of(FileSystemTemplateRoot.fromZip(Paths.get(local), opts.zipRoot.orElse(repository + "-" + branch)));
                }
            } catch (IOException ioe) {
                LOG.error(ioe.getMessage());
            }
            return Optional.empty();
        }
    }

    @Option(names = {"-o", "--output"}, description = "Target directory for template output", paramLabel = "OUT")
    private String output;

    @ArgGroup(exclusive = false, heading = "Template download options:%n")
    private TemplateDownloadOptions templateDownloadOptions = new TemplateDownloadOptions();

    static class TemplateDownloadOptions {
        @Option(names = {"-r", "--repo"}, description = "URI to maven or github repo when using those template sources", paramLabel = "URI")
        private Optional<String> repo;

        @Option(names = {"-i", "--zip-root"}, description = "Path to template root inside ZIP/JAR", paramLabel = "PATH")
        private Optional<String> zipRoot;

        @Option(names = {"-c", "--retain"}, description = "Retain any cached downloads of ZIP/JAR files")
        private boolean retainCache;
    }

    @Option(names = {"-x", "--no-color"}, description = "No color in console")
    private boolean noColors;

    @ArgGroup(exclusive = false, heading = "Template variable sources:%n")
    private TemplateVariableSources templateVariableSources = new TemplateVariableSources();

    static class TemplateVariableSources {

        @Option(names = {"-p", "--properties"}, description = "Use system properties to resolve template variables", defaultValue = "false")
        private boolean sysProps;

        @Option(names = {"-e", "--env"}, description = "Use system ENVIRONMENT to resolve template variables", defaultValue = "false")
        private boolean env;

        @Option(names = {"-j", "--json"}, description = "Use JSON file(s) to resolve template variables. Filenames separated by ';'",
                split = ";", splitSynopsisLabel = ";", paramLabel = "JSON_FILE")
        private List<String> json;
    }

    @Option(names = "--value-sources", description = "The value sources to use, and their order", defaultValue = "TEMPLATE;SYSTEM_PROPERTIES;ENVIRONMENT_VARIABLES;JSON_FILES;USER_DEFAULTS", split = ";", splitSynopsisLabel = ";", paramLabel = "SOURCE")
    private SortedSet<ValueSource> valueSources;


    @Option(names = {"-q", "--quiet"}, description = "Disable interactive mode", defaultValue = "false")
    private boolean quiet;

    private static String loadVersionFromProperties() {
        try {
            Properties properties = new Properties();
            properties.load(Velociwraptor.class.getResourceAsStream("/project-info.properties"));
            return properties.getProperty("version", "not-specified");
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return "no project-info.properties";
    }



    public void run() {
        System.out.println("Velociwraptor v"+VERSION);

        // use colors in console
        // create an interactive user if needed
        try (UserInteractive interactive = quiet ? new NullInteractive() : ConsoleInteractive.console(noColors)) {

            // output directory (if specified - else CWD)

            Optional<String> target = Optional.ofNullable(output);
            final File targetDir = target.map(File::new).orElse(currentWorkingDir);

            // create a templateRoot
            Optional<TemplateRoot> templateRoot = templateSource.templateRoot(currentWorkingDir, templateDownloadOptions);

            // if we have a templateRoot - create a session
            if(templateRoot.isPresent()) {
                TemplateRoot root = templateRoot.get();
                // get the list of value providers...
                var valueProviders = loadValueProviders(root, currentWorkingDir);
                TemplateDataModel templateData = new TemplateDataModel(valueProviders, interactive);
                Session session = new Session(root.rootDirectory(), templateData, targetDir);
                session.run();
            }
            else {
                System.err.println("No recognised templateRoot source specified");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<ValueProvider> loadValueProviders(TemplateRoot root, File currentWorkingDir) {
        List<ValueProvider> providers = new ArrayList<>();
        for (ValueSource source : valueSources) {
            switch (source) {
                case SYSTEM_PROPERTIES -> {
                    if (templateVariableSources.sysProps) {
                        providers.add(PropertyValueSource.systemProperties());
                    }
                    else {
                        LOG.warn("SYSTEM_PROPERTIES source specified but not enabled");
                    }
                }
                case ENVIRONMENT_VARIABLES -> {
                    if (templateVariableSources.env) {
                        providers.add(PropertyValueSource.environmentVariables());
                    }
                    else {
                        LOG.warn("ENVIRONMENT_VARIABLES source specified but not enabled");
                    }
                }
                case JSON_FILES -> {
                    if (templateVariableSources.json != null) {
                        for (String jsonFile : templateVariableSources.json) {
                            File file = new File(currentWorkingDir, jsonFile);
                            var jsonValueSource = JsonValueSource.fromFile(ValueSource.JSON_FILES, file);
                            if (jsonValueSource == null) {
                                LOG.warn("Could not load JSON file: {}", file.getAbsolutePath());
                                continue;
                            }
                            providers.add(jsonValueSource);
                        }
                    }
                }
                case TEMPLATE -> {
                    providers.add(root.projectSettings());
                }
                case USER_DEFAULTS -> {
                    File userHome = new File(System.getProperty("user.home"));
                    File file = new File(userHome, ".velociwraptor/defaults.json");
                    var jsonValueSource = JsonValueSource.fromFile(ValueSource.USER_DEFAULTS, file);
                    if (jsonValueSource == null) {
                        LOG.warn("Could not load User default file: {}", file.getAbsolutePath());
                        continue;
                    }
                    providers.add(jsonValueSource);
                }
            }
        }
        // reverse the list so that the last added has highest precedence
        Collections.reverse(providers);
        return providers;
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
}
