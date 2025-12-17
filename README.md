[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2FduckAsteroid%2Fvelociwraptor.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2FduckAsteroid%2Fvelociwraptor?ref=badge_shield)
[![Known Vulnerabilities](https://snyk.io/test/github/duckAsteroid/velociwraptor/badge.svg?targetFile=build.gradle)](https://snyk.io/test/github/duckAsteroid/velociwraptor?targetFile=build.gradle)

![velociwraptor logo](icons/icon128.png) velociwraptor
===========
A project templating library based on the ideas in https://github.com/tmrts/boilr.

It is designed to be a simple way to create a new software project from a template. The user will 
supply various values to the template and velociwraptor will create a new project (a collection of templateFiles and folders) 
based on the template and those values. These values can be either from defaults or those entered by the user.

Template engine used is [JMTE](https://github.com/DJCordhose/jmte).

Download
-----------

You can grab an executable installer from the [releases](github.com/duckAsteroid/velociwraptor/releases) link above.

Getting Started
---------------

Let's start with a simple "hello world" example. 

We will use `velociwraptor` with the world's most simplistic template. This template only contains one 
templateFile `hello.txt`.
```text
velociwraptor -g duckAsteroid/hello-world/master
```
We asked velociwraptor to use a template in a GitHub repository `duckAsteroid/hello-world/master` this is shorthand 
for the `master` branch of the https://github.com/duckAsteroid/hello-world repository. 

Velociwraptor is in interactive mode, it prompts us to confirm the greeting we would
like to see "templated" into `hello.txt`...
```text
Velociwraptor v0.0.1
[?] Please choose an option for "Greeting" [default: Hello world!]:
```
The default looks fine, so we hit &lt;ENTER&gt;. The template runs, and we now have a new
`hello.txt` templateFile. 

Let's look at the structure of [that github repo](https://github.com/duckAsteroid/hello-world):
* `template/hello.txt` - this folder `template` contains the templateFiles that are copied as part of our template.
* `default.json` - this JSON templateFile contains the default values for variables to use in the template (not copied into 
template output).
* `Readme.md` - this is a readme templateFile for GitHub (not copied into template output)
* `LICENSE` - this is a licence for the template (not copied into template output)

Looking inside `template/hello.txt` we can see:

```text
${Greeting}

If you can read this - velociwraptor is working!
```

The first line refers to a template variable `Greeting`. Velociwraptor prompted us for the value we would like to use
and `default.json` contains a default to use (should we choose not to supply a value):

```json
{
  "Greeting": "Hello world!"
}
```

So let's look inside our template output and see what's inside it...
```text
more hello.txt 

Hello world!

If you can read this - velociwraptor is working!
```

As you can see our greeting was placed on the first line. 

This is a very simple example. We could create many directories and templateFiles and use many
more template variables.

Next Steps
----
Read about how [templates](https://duckasteroid.github.io/velociwraptor/templates.html) behave or the 
[JMTE](https://github.com/duckAsteroid/jmte/wiki/LanguageSpecification) syntax used to define them.

Template Sources
-------
Velociwraptor will use templates from many sources including:
* A template templateDirectory in the local templateFile system
* A template ZIP (including .jar etc.) in the local templateFile system
* A template ZIP (including .jar etc.) on some public URI
* A maven repository co-ordinate (it will fetch the JAR and use it as the template)
* A github repository (it will grab a snapshot ZIP from github.com)

## License
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2FduckAsteroid%2Fvelociwraptor.svg?type=large)](https://app.fossa.io/projects/git%2Bgithub.com%2FduckAsteroid%2Fvelociwraptor?ref=badge_large)
