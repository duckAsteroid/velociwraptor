* Add a `velociwraptor.properties` templateFile in user home to control config
* Have a default GH 'username' to search for projects (in properties?)
* Ability to declare 'overlay' projects 
    * create rules: if 'java' then 'gradle'
    * declare local preferences
    * certainly chain projects together e.g. "Do you want to include GitHub Actions?"
    * lookup/discovery strategy?
    * Support pre/post commands in the project (e.g. init git repo)
      * Let those commands use velociwraptor variables
* Switch to PicoCLI tool and dump Apache
* Add velociwraptor to OS package installers
  * JReleaser
* Add a way for templates to extract "latest" versions of dependencies etc. (e.g. a maven co-ordinate)
  * Maybe the same "init" process can do this?