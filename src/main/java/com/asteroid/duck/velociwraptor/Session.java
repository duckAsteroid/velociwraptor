package com.asteroid.duck.velociwraptor;

import com.asteroid.duck.velociwraptor.model.JavaPackageRenderer;
import com.asteroid.duck.velociwraptor.model.JsonConverter;
import com.asteroid.duck.velociwraptor.model.vars.TemplateDataModel;
import com.asteroid.duck.velociwraptor.template.TemplateDirectory;
import com.asteroid.duck.velociwraptor.template.TemplateFile;
import com.asteroid.duck.velociwraptor.template.section.SectionParser;
import com.asteroid.duck.velociwraptor.template.section.TemplateSection;
import com.fasterxml.jackson.databind.JsonNode;
import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.message.ParseException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;


import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
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

    private final TemplateDirectory root;
    private final TemplateDataModel template;
    private final File targetDirectory;

    /** JMTE Template engine */
    private final Engine engine = new Engine();


    public Session(TemplateDirectory root, TemplateDataModel templateData, File targetDirectory) {
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
        engine.registerRenderer(JsonNode.class, new JsonConverter());
        // special renderer for Java package names
        engine.registerNamedRenderer(new JavaPackageRenderer());
    }

    public void run() throws IOException {
        apply(root, targetDirectory, template);
    }

    public void apply(TemplateDirectory templateSource, File targetDirectory, TemplateDataModel model) throws IOException {
        LOG.trace("Applying template to {}", targetDirectory);
        // directories
        templateSource.childDirs().forEach(tmp -> applyTemplateDirectory(targetDirectory, tmp, model));
        // files
        templateSource.childFiles().forEach(tmp -> applyTemplate(targetDirectory, tmp, model));
    }

    private void applyTemplateDirectory(File parent, TemplateDirectory templateDirectory, TemplateDataModel model) {
        LOG.trace("Applying template to directory {}", templateDirectory.rawName());
        try {
            final String folderName = convertRawName(templateDirectory.rawName(), model);
            if (isValid(folderName)) {
                File newDirectory = new File(parent, folderName);
                if (!newDirectory.exists()) {
                    newDirectory.mkdir();
                }
                // we may have a file with this name already!!
                if (newDirectory.isDirectory()) {
                    // recurse
                    apply(templateDirectory, newDirectory, model);
                }
            } else {
                LOG.trace(templateDirectory.rawName() + " was skipped from processing");
            }
        }
        catch(ParseException e) {
            LOG.error("Unable to parse "+ templateDirectory.rawName(), e);
        }
        catch(IOException e) {
            LOG.error("IO exception processing dir:"+ templateDirectory.rawName(), e);
        }
    }

    private void applyTemplate(File parent, TemplateFile templateFile, TemplateDataModel model) {
        // read template
        LOG.trace("Applying template to file {}", templateFile.rawName());
        try {
            String rawTemplateFileName = templateFile.rawName();
            boolean isTemplate = true;
            // is this file to be ignored?
            if (rawTemplateFileName.startsWith("#")) {
                // chop off the #
                rawTemplateFileName = rawTemplateFileName.substring(1);
                // it's treated as a template if it did not start ##
                if (!rawTemplateFileName.startsWith("#")) {
                    // i.e. original template filename was not ##blah
                    // copy raw do not template content
                    isTemplate = false;
                }
            }
            final String filename = convertRawName(rawTemplateFileName, model);
            // only bother to create and populate if filename is valid
            if (isValid(filename)) {
                File newFile = new File(parent, filename);
                // if it does not already exist
                if (!newFile.exists()) {
                    if (isTemplate) {
                        // read template sections and process
                        SectionParser parser = new SectionParser(new InputStreamReader(templateFile.rawContent()));
                        List<TemplateSection> template = SectionParser.stream(parser).toList();
                        LOG.trace("Processing {} sections for file {}", template.size(), filename);
                        for (TemplateSection section : template) {
                            String newContent;
                            if (section.template()) {
                                newContent = engine.transform(section.content(), model);
                            } else {
                                newContent = section.content();
                            }
                            // append each section
                            FileUtils.write(newFile, newContent, Charset.defaultCharset(), true);
                        }
                    }
                    else {
                        // just copy raw content
                        FileUtils.copyInputStreamToFile(templateFile.rawContent(), newFile);
                    }
                } else {
                    LOG.warn(newFile.getCanonicalPath() + " already exists, skipping in template");
                }
            } else {
                LOG.trace(templateFile.rawName() + " was skipped from processing");
            }

        }
        catch(ParseException e) {
            LOG.error("Unable to parse "+ templateFile.rawName(), e);
        }
        catch(IOException e) {
            LOG.error("IO exception processing template", e);
        }
    }


    private static boolean isValid(String filename) {
        return filename != null && !filename.isEmpty();
    }

    private String convertRawName(String rawName, TemplateDataModel model) {
        return engine.transform(rawName, model);
    }
}
