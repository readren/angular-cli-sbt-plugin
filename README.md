# angular-cli-sbt-plugin
An SBT plugin that adapts a SBT project to follow the angular2 style guide, and collaborate with _angular-cli_.

## Prerequisites

The _angular-cli_ beta.6 generated project has dependencies that require Node 4.4 and npm 3.10 or greater. Be sure you fullfill that:

`node --version` should be >= 4.4

`npm --version` should be >= 3.10

[Angular-cli](https://github.com/angular/angular-cli) should be already installed. You can do that with:

`npm install -g angular-cli`

[SBT](http://www.scala-sbt.org/download.html) and the [lightbend activator](https://www.lightbend.com/activator/download) should be installed. The minimal version of activator is fine.

## Installation

####Use _activator_ to create a new _SBT_ project:

`activator new my-project-name minimal-scala` 

The example use the *minimal-scala* template, but any other template would be fine provided it doesn't conflict with this plugin.

####Add the _angular-cli_ nature to the project:

`cd my-project-name`

`ng init`

A conflict with the ".gitignore" file may be shown. In that case, choose "overwrite" after opening the file and copying its contents to the clipboard. When the "ng init" commands finishes, append the clipped text to the new ".gitignore" file.

####Check the ng command is working:
Use the `ng` command to check if it is working:
`ng`

If an error similar to "Error: Cannot find module 'exists-sync'" is thrown, solve it with:

`npm install`

####Add this plugin to the build:
Create a new file named "angular-cli.sbt" inside the "<projectBase>/project" folder and append the following line to it:

`addSbtPlugin("readren" % "angular-cli-plugin" % "0.1.1-SNAPSHOT")`

####Enable this plugin:
Open the "build.sbt" file and append this block:

<pre>
lazy val root = (project in file("."))
	.enablePlugins(AngularCliPlugin, BuildInfoPlugin)
	.settings(AngularCliPlugin.overridenProjectSettings)
</pre>

####Remove non used directories
Remove the default sbt source directories `<projectBase>/src/main` and `<projectBase>/src/test` created by the "minimal-scala" template.

##Usage
The SBT part of the project would be as usual except the source directory structure. The `scalaSource` and `javaSource` settings point to "<projectBase>/src/app" for both, the compile and test configurations.
The criteria to discriminate between compile, test, server, or client source are file name tags, instead of the containing directory.   

The _angular-cli_ part of the project can be managed as usual from the console, or inside the SBT interactive CLI. This plugin adds two sbt task to handle _angular-cli_ from SBT:

Use the `build` task to build the client side of the application. It's equivalent to `ng build`.

Use the `ng <parameters>` input task to execute any other _angular-cli_ command.
