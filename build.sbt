
lazy val commonSettings = Seq(
	organization := "readren",
	version := "0.2.0-SNAPSHOT"
)


lazy val root = (project in file(".")).
	aggregate(webServer).
	settings(commonSettings: _*).
	settings(
		sbtPlugin := true,
		name := """angular-cli-plugin""",
		scalaVersion := "2.10.6", // the one used by sbt version 0.13.11
		resolvers += Resolver.sonatypeRepo("snapshots"),
		scalacOptions ++= Seq("-feature")
	)


lazy val webServer = project.settings(commonSettings: _*)

