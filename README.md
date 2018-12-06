velociwraptor
===========
A project templating library based on the ideas in https://github.com/tmrts/boilr

Template engine used is [JMTE][https://github.com/DJCordhose/jmte]

Getting Started
---------------

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