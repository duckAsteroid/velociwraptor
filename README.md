velociwraptor
===========
A project templating library based on the ideas in https://github.com/tmrts/boilr

Getting Started
---------------

EDIT ME

Download
-----------

TO DO 
-------
* Main class with args to run generator
    * Pass in the template as a DIR/ZIP/Git
    * (optional) give a working DIR for the output
* Parse the `project.json` into velocity context
* Walk the template and write to target 
    * Use a marker file to control directory/folder creation (i.e. omit/include files/directories)
    * Allow inclusion of other templates (versioning?)
    * Use velocity to drive the template
    * Have a marker line : in the marker file to indicate this is really part of a template (not a marker)
* A template registry server (maven?)
    * Allow templates to find/reference each other by maven co-ord


