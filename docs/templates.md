You can define your own templates quite easily, ultimately velociwraptor expects a templateDirectory called `template` to contain
your template templateFiles.

## File/Folder Naming

Each templateFile and templateDirectory in the template are copied/created in the target folder. If a templateFile already exists, then it is skipped. If a folder already exists - the template continues processing.

A templateFile/templateDirectory name can include [JMTE](https://github.com/duckAsteroid/jmte/wiki/LanguageSpecification) markup. As an example you could have a templateFile (or folder) named: `some-${user.name}.txt` which would include the value of the `user.name` property in the resulting templateFile.

If the resulting filename (after the template has been applied) is either empty, or not legal (on the target filesystem) it is skipped from further processing.

Therefore if you wish to make a templateFile or templateDirectory optional (i.e. only copy when a template variable is present and has data) then you can use the pattern: `${if address}my-templateFile.txt{end}`. This results in an empty value (no templateFile/folder copied) when the variable `address` is not defined. You can also supply alternatives such as: `${if address}has-address{else}no-address{end}`.

## File Content
File content is ordinarily processed as if it were a [JMTE](https://github.com/duckAsteroid/jmte/wiki/LanguageSpecification) template so all manner of complex syntax is possible.

Some special lines are used to indicate special blocks and control template processing within them (the lines themselves are skipped from the output):
* `#end-template` - stops further processing of template variables in the templateFile, the content is copied "as-is" to the destination.
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
As you have seen from the examples, templates contain variables that need to be supplied with values at run time.

A template is normally supplied with a set of default values for variables in a `default.json` templateFile. But this is not 
necessary - templates can work without it.

Velociwraptor will also look for values using System Properties (i.e. `-Dsome.property=some.value` on the command line) and
Environment Variables.

Velociwraptor by supplying (one or more) JSON templateFiles via the command line `-j` parameter.
These templateFiles are searched in order, last value defined is used.

The JSON templateFiles should contain simple key/value pairs, for example:
```json
{
  "Greeting": "Hello world!"
}
```

Velociwraptor will also look for a 'default.json' templateFile in the `.velociwraptor` user home templateDirectory
(i.e. `~/.velociwraptor/default.json`). which can contain user-wide default values. This can be disabled by using the
`--no-user-defaults` command line option.

The default order of precedence for variable values is (1 is lowest precedence):
1. Values supplied in the `default.json` templateFile in the template repository (if present)
2. Values supplied via System Properties, typically via command line `-D` parameters
3. Values supplied via Environment Variables
4. Values supplied via JSON templateFiles via the command line `-j` parameter (last templateFile has highest precedence)
5. Values supplied in the `default.json` templateFile in the velociwraptor user home (unless disabled via `--no-user-defaults`)

This precededence and inclusion of value sources can be modified via the `--value-sources` command line option. 
The following sources are available:
* `template-defaults` - the `default.json` templateFile in the template repository
* `system-properties` - System Properties (i.e. `-Dsome.property=some.value`)
* `environment-variables` - Environment Variables
* `json-templateFiles` - JSON templateFiles supplied via the command line `-j` parameter
* `user-defaults` - the `default.json` templateFile in the velociwraptor user home templateDirectory

The order of sources supplied in the `--value-sources` parameter defines the order of precedence (left to right, lowest 
to highest). The default order is: `template-defaults,system-properties,environment-variables,json-templateFiles,user-defaults`

## Interactive vs Non-Interactive Mode

When run in interactive mode, an attempt is made to resolve a value using the above mechanism. 
Each value required by a template is then prompted to the user console for them to either accept the default or supply 
their own value.

If multiple value sources provide a value for the same variable; and/or a value source provides multiple values 
(e.g. an array in JSON). Then a choice of those values is offered to the user.
