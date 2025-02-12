You can define your own templates quite easily, ultimately velociwraptor expects a directory called `template` to contain
your template files.

## File/Folder Naming

Each file and directory in the template are copied/created in the target folder. If a file already exists, then it is skipped. If a folder already exists - the template continues processing.

A file/directory name can include [JMTE](https://github.com/duckAsteroid/jmte/wiki/LanguageSpecification) markup. As an example you could have a file (or folder) named: `some-${user.name}.txt` which would include the value of the `user.name` property in the resulting file.

If the resulting filename (after the template has been applied) is either empty, or not legal (on the target filesystem) it is skipped from further processing.

Therefore if you wish to make a file or directory optional (i.e. only copy when a template variable is present and has data) then you can use the pattern: `${if address}my-file.txt{end}`. This results in an empty value (no file/folder copied) when the variable `address` is not defined. You can also supply alternatives such as: `${if address}has-address{else}no-address{end}`.

## File Content
File content is ordinarily processed as if it were a [JMTE](https://github.com/duckAsteroid/jmte/wiki/LanguageSpecification) template so all manner of complex syntax is possible.

Some special lines are used to indicate special blocks and control template processing within them (the lines themselves are skipped from the output):
* `#end-template` - stops further processing of template variables in the file, the content is copied "as-is" to the destination.
* `#begin-template` - re-starts template processing.
* `#no-template` - skips all remaining lines from the template (neither copied nor processed as template).

So as an example:
```text
Hello ${Greeting} - this is an ordinary template section.
#no-template
This is neither passed to the template engine - nor the output. It's like a comment...
${Greeting} has no special meaning here.
#end-template
${Greeting} has no special meaning here, but this string is copied to the output.
```
If the input data contains `Greeting="World"` then the output would be:
```text
Hello World - this is an ordinary template section.
${Greeting} has no special meaning here, but this string is copied to the output.
```
# Template Variable Values
A template is normally supplied with a set of default values for variables in a `default.json` file.

These values can be overridden at run time by supplying (one or more) JSON files via the command line `-j` parameter. These files are searched in order, last value defined is used.

When run in interactive mode, each value found via the above JSON file(s) is then prompted to the user console (accept the default or supply your own value).
