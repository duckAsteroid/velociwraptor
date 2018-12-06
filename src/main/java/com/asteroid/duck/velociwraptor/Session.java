package com.asteroid.duck.velociwraptor;

import com.asteroid.duck.velociwraptor.model.JsonConverter;
import com.asteroid.duck.velociwraptor.model.TemplateData;
import com.asteroid.duck.velociwraptor.template.Directory;
import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.message.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import javax.json.JsonValue;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * A session is used to apply the template in a target directory
 */
public class Session {
    /**
     * Logger (SLF4J)
     */
    private static final Logger LOG = getLogger(Session.class);

    private final Directory root;
    private final TemplateData template;
    private final File targetDirectory;

    /** Template engine */
    private final Engine engine = new Engine();


    public Session(Directory root, TemplateData templateData, File targetDirectory) {
        if (root == null) {
            throw new IllegalArgumentException("Template root dir cannot be null");
        }
        this.root = root;
        if (templateData == null) {
            throw new IllegalArgumentException("Template data cannot be null");
        }
        this.template = templateData;

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
        // special renderer for JSON objects
        engine.registerRenderer(JsonValue.class, new JsonConverter());
    }

    public void run() throws IOException {
        apply(root, targetDirectory, template);
    }

    public void apply(Directory templateSource, File targetDirectory, TemplateData model) throws IOException {
        // TODO Check for conditional files and folders
        templateSource.childDirs().forEach(tmp -> {
            try {
                String folderName = convertRawName(tmp.rawName(), model);
                if (isValid(folderName)) {
                    File newDirectory = new File(targetDirectory, convertRawName(tmp.rawName(), model));
                    if (!newDirectory.exists()) {
                        newDirectory.mkdir();
                    }
                    // if we have a file with this name already!!
                    if (newDirectory.isDirectory()) {
                        // recurse
                        apply(tmp, newDirectory, model);
                    }
                } else {
                    LOG.trace(tmp.rawName() + " was skipped from processing");
                }
            }
            catch(ParseException e) {
                LOG.error("Unable to parse "+tmp.rawName(), e);
            }
            catch(IOException e) {
                LOG.error("IO exception processing dir:"+tmp.rawName(), e);
            }
        });

        templateSource.childFiles().forEach(tmp -> {
            // read template
            try {
                String filename = convertRawName(tmp.rawName(), model);
                if (isValid(filename)) {
                    File newFile = new File(targetDirectory, filename);
                    if (!newFile.exists()) {
                        String template = IOUtils.toString(tmp.rawContent(), Charset.defaultCharset());
                        String newContent = engine.transform(template, model);
                        FileUtils.write(newFile, newContent, Charset.defaultCharset(), false);
                    } else {
                        LOG.warn(newFile.getCanonicalPath() + " already exists, skipping in template");
                    }
                }
                else {
                    LOG.trace(tmp.rawName()+" was skipped from processing");
                }
            }
            catch(ParseException e) {
                LOG.error("Unable to parse "+tmp.rawName(), e);
            }
            catch(IOException e) {
                LOG.error("IO exception processing template", e);
            }
        });
    }

    private static boolean isValid(String filename) {
        return filename != null && filename.length() > 0;
    }

    private String convertRawName(String rawName, TemplateData model) {
        return engine.transform(rawName, model);
    }
}
