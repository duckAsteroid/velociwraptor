package com.asteroid.duck.velociwraptor;

import com.asteroid.duck.velociwraptor.model.JsonConverter;
import com.asteroid.duck.velociwraptor.model.TemplateData;
import com.asteroid.duck.velociwraptor.template.Directory;
import com.asteroid.duck.velociwraptor.template.SectionParser;
import com.asteroid.duck.velociwraptor.template.TemplateSection;
import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.message.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import javax.json.JsonValue;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

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

    /** TemplateRoot engine */
    private final Engine engine = new Engine();


    public Session(Directory root, TemplateData templateData, File targetDirectory) {
        if (root == null) {
            throw new IllegalArgumentException("TemplateRoot root dir cannot be null");
        }
        this.root = root;
        if (templateData == null) {
            throw new IllegalArgumentException("TemplateRoot data cannot be null");
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
        // directories
        templateSource.childDirs().forEach(tmp -> applyTemplateDirectory(targetDirectory, tmp, model));
        // files
        templateSource.childFiles().forEach(tmp -> applyTemplate(targetDirectory, tmp, model));
    }

    private void applyTemplateDirectory(File parent, Directory directory, TemplateData model) {
        try {
            String folderName = convertRawName(directory.rawName(), model);
            if (isValid(folderName)) {
                File newDirectory = new File(parent, convertRawName(directory.rawName(), model));
                if (!newDirectory.exists()) {
                    newDirectory.mkdir();
                }
                // we may have a file with this name already!!
                if (newDirectory.isDirectory()) {
                    // recurse
                    apply(directory, newDirectory, model);
                }
            } else {
                LOG.trace(directory.rawName() + " was skipped from processing");
            }
        }
        catch(ParseException e) {
            LOG.error("Unable to parse "+directory.rawName(), e);
        }
        catch(IOException e) {
            LOG.error("IO exception processing dir:"+directory.rawName(), e);
        }
    }

    private void applyTemplate(File parent, com.asteroid.duck.velociwraptor.template.File file, TemplateData model) {
        // read template
        try {
            final String rawTemplateFileName = file.rawName();
            final String filename = convertRawName(rawTemplateFileName, model);
            // only bother to create and populate if filename is valid
            if (isValid(filename)) {
                File newFile = new File(parent, filename);
                // if it does not already exist
                if (!newFile.exists()) {
                    // read template sections and process
                    List<TemplateSection> template = SectionParser.parse(file.rawContent());
                    for(TemplateSection section : template) {
                        String newContent;
                        if (section.isTemplate()) {
                             newContent = engine.transform(section.content(), model);
                        }
                        else {
                            newContent = section.content();
                        }
                        // append each section
                        FileUtils.write(newFile, newContent, Charset.defaultCharset(), true);
                    }
                } else {
                    LOG.warn(newFile.getCanonicalPath() + " already exists, skipping in template");
                }
            } else {
                LOG.trace(file.rawName() + " was skipped from processing");
            }

        }
        catch(ParseException e) {
            LOG.error("Unable to parse "+file.rawName(), e);
        }
        catch(IOException e) {
            LOG.error("IO exception processing template", e);
        }
    }


    private static boolean isValid(String filename) {
        return filename != null && filename.length() > 0;
    }

    private String convertRawName(String rawName, TemplateData model) {
        return engine.transform(rawName, model);
    }
}
