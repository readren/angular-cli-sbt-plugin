package readren

import sbt._
import Keys._
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.io.IOException
import java.io.ByteArrayInputStream

/**
 * An SBT plug-in that adapts SBT to Angular-CLI.
 */
object AngularCliPlugin extends AutoPlugin {

	/**
	 * Defines all settings/tasks that get automatically imported,
	 * when the plugin is enabled
	 */
	object autoImport {
		val appDirectory = settingKey[File]("Directory containing all the local application files (all except the global ones).")
		val distDirectory = settingKey[File]("Base directory for the files fetched from the web. This directory is added to the runtime `managedClasspath`")
		val build = taskKey[Unit]("Executes `ng build`, which builds your app and places it into the output path (dist/ by default).")
		val tsc = taskKey[Unit]("runs the typescript compiler")
		val ng = inputKey[Unit]("runs the specified ng command")
	}

	import autoImport._

	/**
	 * Provide default settings
	 */
	override def projectSettings: Seq[Setting[_]] =
		Seq(
			appDirectory := sourceDirectory.value / "app",
			distDirectory := baseDirectory.value / "dist",
			build := buildTask(outputPath = distDirectory.value),
			tsc := tscTask(distDirectory.value),
			ng := ngTask(complete.DefaultParsers.any.*.map(_.mkString).parsed)
		)

	/** SBT resets any change to standard settings done in the `projectSettings` method. So users of this plug-in should append this settings explicitly in order to reconfigure the source directory structure. */
	val overridenProjectSettings: Seq[Setting[_]] =
		Seq(
			// follow angular2 convention which discriminates main code, test code and resource files with a tag in the file name, instead of by the directory with contains it
			scalaSource in Compile := appDirectory.value,
			scalaSource in Test := appDirectory.value,
			javaSource in Compile := appDirectory.value,
			javaSource in Test := appDirectory.value,
			
			resourceDirectory := baseDirectory.value / "global-resources", // "runtime-conf" could be more appropriate but more specific than necessary 
			resourceDirectory in Compile := resourceDirectory.value / "main",
			resourceDirectory in Test := resourceDirectory.value / "test",
			
			(managedClasspath in Runtime) += distDirectory.value,
//			(managedClasspath in Runtime) <<= (managedClasspath in Runtime).dependsOn(build),
			fork := true // runtime class-path settings have no effect unless fork == true
		)

	private def buildTask(environment: String = "development", outputPath: File = new File("dist"), watch: Boolean = false): Unit = {
		Process(s"node node_modules/angular-cli/bin/ng build --environment $environment --output-path ${outputPath.getPath} --watch $watch").!
	}

	private def ngTask(args: String): Unit = {
		Process(s"node node_modules/angular-cli/bin/ng $args").!
	}

	private def tscTask(outDir: File): Unit = {
		Process(s"node node_modules/typescript/lib/tsc.js --outDir ${outDir.getPath}").!
	}

}