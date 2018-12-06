velociwraptor
===========
A project templating library based on the ideas in https://github.com/tmrts/boilr

Template engine used is [JMTE][https://github.com/DJCordhose/jmte]

Getting Started
---------------

Usage
-------
```text
Velociwraptor v0.0.1
usage: Main [-q] [-r <URI>] [-i <PATH>] [-c] -d <DIR> | -z <URI> | -m
       <MVN> | -g <REPO>    [-o <OUT>] [-p] [-e] [-j <JSON>]
 -q,--quiet             Disable interactive mode
 -r,--repo <URI>        URI to maven or github repo when using those
                        template sources
 -i,--zip-root <PATH>   path to template root inside ZIP/JAR
 -c,--retain            Retain any cached downloads of ZIP/JAR files
 -d,--dir <DIR>         Use a local directory <DIR> as template
 -z,--zip <URI>         Use a ZIP file (local file/public web URI) as a
                        template
 -m,--mvn <MVN>         Use a maven artefact (JAR) as a template. Use
                        maven ':' separated coordinate syntax - see [1],
                        [2].
 -g,--github <REPO>     Use a GitHub repo as the template. Repository is
                        defined as {user}/{repo}/{branch} (branch
                        optional). See [1]
 -o,--output <OUT>      Target directory for template output
 -p,--properties        Use system properties to resolve template
                        variables
 -e,--env               Use system ENVIRONMENT to resolve template
                        variables
 -j,--json <JSON>       Use JSON file(s) to resolve template variables. If
                        a list, filenames are separated by ';'.Relative
                        paths are resolved based on current working dir

[1] - Non standard repository URI may be specified with the --repo option.
[2] - https://maven.apache.org/pom.html#Maven_Coordinates
```


Templates
-------
Velociwraptor will use templates from many places including:
* A template directory in the local file system
* A template ZIP (including .jar etc.) in the local file system
* A template ZIP (including .jar etc.) on some public URI
* A maven repository co-ordinate (it will fetch the JAR and use it as the template)
* A github repository (it will grab a snapshot ZIP from github.com)

If you want to create your own templates check out the guide.

Download
-----------

TO DO 
-------
* Override local project.json with one from the command line

* Main class with args to run generator
    * Pass in the template as a DIR/ZIP/Git
    * (optional) give a working DIR for the output
* Walk the template and write to target 
    * Use a marker file to control directory/folder creation (i.e. omit/include files/directories)
    * Allow inclusion of other templates (versioning?)
    * Use velocity to drive the template
    * Have a marker line : in the marker file to indicate this is really part of a template (not a marker)
* A template registry server (maven?)
    * Allow templates to find/reference each other by maven co-ord




[JMTE]: https://github.com/DJCordhose/jmte

[https://github.com/DJCordhose/jmte]: https://github.com/DJCordhose/jmte