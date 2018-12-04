package com.asteroid.duck.velociwraptor;

import org.apache.commons.cli.*;

import java.io.File;

public class Main {

    private static final Options options() {
        Options options = new Options();
        Option help = new Option( "h","help", false, "print this message" );
        options.addOption(help);
        options.addRequiredOption( "t","input", true, "The kind of template to use, one of " + TemplateType.listTemplateTypes() );
        return options;
    }

    public static void main(String[] args) {
        final Options options = options();
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine commandLine = parser.parse(options, args);
            if (commandLine.hasOption("help")) {
                doHelp(options);
            }
            else {
                String typeString = commandLine.getOptionValue("t");
                try {
                    TemplateType type = TemplateType.valueOf(typeString);
                    String[] remArgs = commandLine.getArgs();
                    File dir = new File(".");
                    if (remArgs.length > 0) {
                        dir = new File(remArgs[0]);
                    }
                    Session s = new Session(type, dir);
                }
                catch(IllegalArgumentException e) {
                    System.err.println("Unknown type: '"+typeString+"'");
                    doHelp(options);
                }
            }
        }
        catch (ParseException e) {
            System.err.println(e.getMessage());
            doHelp(options);
        }
    }

    private static void doHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        System.out.println("Main [options] [target directory]");
        System.out.println("    if no target directory is specified the current working directory is used.");
        formatter.printHelp("Main", options);
    }
}
