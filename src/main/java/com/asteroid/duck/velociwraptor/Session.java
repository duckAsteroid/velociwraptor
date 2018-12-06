package com.asteroid.duck.velociwraptor;

import com.asteroid.duck.velociwraptor.model.Directory;
import com.asteroid.duck.velociwraptor.model.Template;
import com.asteroid.duck.velociwraptor.project.InteractiveMap;
import com.asteroid.duck.velociwraptor.project.UserInteractive;
import com.floreysoft.jmte.Engine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * A session is used to apply the template in a target directory
 */
public class Session {
    /**
     * Logger (SLF4J)
     */
    private static final Logger LOG = getLogger(Session.class);

    private final Template template;
    private final File targetDirectory;
    /** Template engine */
    private final Engine engine = new Engine();
    private final UserInteractive user;

    public Session(Template template, File targetDirectory, UserInteractive user) {
        if (template == null) {
            throw new IllegalArgumentException("Template cannot be null");
        }
        this.template = template;
        if (targetDirectory == null) {
            throw new IllegalArgumentException("Target directory cannot be null");
        }
        if (!targetDirectory.exists()) {
            throw new IllegalArgumentException("Target directory '"+targetDirectory.getAbsolutePath()+"' does not exist");
        }
        if (!targetDirectory.isDirectory()) {
            throw new IllegalArgumentException("Target directory '"+targetDirectory.getAbsolutePath()+"' is not a directory");
        }
        this.targetDirectory = targetDirectory;
        this.user = user;
    }

    public void run() throws IOException {
        InteractiveMap model = new InteractiveMap(template.projectSettings(), user);
        apply(template.rootDirectory(), targetDirectory, model);
    }

    public void apply(Directory templateSource, File targetDirectory, InteractiveMap model) throws IOException {
        // TODO Check for conditional files and folders
        templateSource.childDirs().forEach(tmp -> {
            File newDirectory = new File(targetDirectory, convertRawName(tmp.rawName(), model));
            newDirectory.mkdir();
            // recurse
            try {
                apply(tmp, newDirectory, model);
            }
            catch(IOException e) {
                LOG.error("Error processing dir "+newDirectory.getAbsolutePath());
            }
        });

        templateSource.childFiles().forEach(tmp -> {
            // read template
            try {
                String template = IOUtils.toString(tmp.rawContent(), Charset.defaultCharset());
                String newContent = engine.transform(template, model);

                File newFile = new File(targetDirectory, convertRawName(tmp.rawName(), model));
                FileUtils.write(newFile, newContent, Charset.defaultCharset(), false);
            }
            catch(IOException e) {
                LOG.error("Unable to read template", e);
            }
        });
    }

    private String convertRawName(String rawName, InteractiveMap model) {
        return engine.transform(rawName, model);
    }
}
