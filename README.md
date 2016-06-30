# angular-cli-sbt-plugin
An SBT plugin that adapts a SBT project to follow the angular2 style guide, and collaborate with angular-cli.

## Prerequisites

The _Angular-cli_ generated project has dependencies that require Node 4 or greater.

_Angular-cli_ should be already installed.

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

####Add this plugin to the build:

Create a new file named "angular-cli.sbt" inside the "project" folder and append the following line to it:

`addSbtPlugin("readren" % "angular-cli-plugin" % "0.1.1-SNAPSHOT")`

####Enable this plugin:

Open the "build.sbt" and append this block:

<pre>
lazy val root = (project in file("."))
	.enablePlugins(AngularCliPlugin, BuildInfoPlugin)
	.settings(AngularCliPlugin.overridenProjectSettings)
</pre>

##Usage

Use the `build` task to build the client side of the application. Its equivalent to `ng build`.
Use the `ng <parameters>` input task to execute any _angular-cli_ command.
