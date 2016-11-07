// builds application releases
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.3")

// generates Scala source from the build definitions.
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.6.1")

// This plugin adds commands to generate IDE project files
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")
