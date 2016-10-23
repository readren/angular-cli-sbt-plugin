package webServer

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

import scala.collection.mutable;
import scala.language.implicitConversions;

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.http.scaladsl.model.Uri
import spray.json.DefaultJsonProtocol
import spray.json.JsObject
import spray.json.JsonParser
import spray.json.ParserInput.apply
import akka.actor.ActorSystem

object MissingResourcesManager {
	val props: Props = Props(new MissingResourcesManager)
	def create(implicit as:ActorSystem):ActorRef = as.actorOf(props) 
}

class MissingResourcesManager extends Actor with ActorLogging with DefaultJsonProtocol {

	private case class Package(name: String, main: String)
	private implicit val packageFormat = jsonFormat2(Package)

	private case class Struct(packages: Seq[Package], files: Seq[String], notFound: Seq[String])
	
	private val nodeModulesPath = BuildInfo.nodeModulesDirectory.toPath()
	private val packages = mutable.Map[String, String]()
	private val files = mutable.Set[String]()
	private val notFound = mutable.Set[String]()

	override def preStart = {
		if (BuildInfo.autodetectedVendorNpmFilesKnower.exists()) {
			val jsonObject = JsonParser(scala.io.Source.fromFile(BuildInfo.autodetectedVendorNpmFilesKnower).mkString).asJsObject("unable to convert the `autodetectedVendorNpmFilesKnower` to json")

			val Seq(p, f) = jsonObject.getFields("packages", "files")
			packages ++= p.convertTo[Seq[Package]].map(p => (p.name, p.main))
			files ++= f.convertTo[Seq[String]]
		}
	};

	def receive: Actor.Receive = {

		case relativePath: Uri.Path =>
			log.info(s"relativePath=$relativePath")
			val path = nodeModulesPath.resolve {
				if (relativePath.startsWithSlash)
					relativePath.tail.toString
				else
					relativePath.toString
			}

			if (path.toFile().isDirectory()) {
				val packageFile = path.resolve("package.json").toFile()
				log.info("absolutePath={}, packageFile={}", path, packageFile)
				val mainFilePath = if (packageFile.exists()) {
					val packageJson: JsObject = JsonParser(scala.io.Source.fromFile(packageFile).mkString).asJsObject(s"unable to convert $packageFile to Json")
					val main = packageJson.getFields("main").head.toString()
					log.info("main={}", main)
					path.resolve(main.replaceAll("\"", ""))
				} else {
					path.resolve("index.js")
				}

				log.info("mainFilePath={}", mainFilePath)
				if (mainFilePath.toFile().exists()) {
					packages.put(pathToString(nodeModulesPath.relativize(path)), pathToString(nodeModulesPath.relativize(mainFilePath)))
				} else {
					notFound += nodeModulesPath.relativize(mainFilePath)
				}
			} else if (path.toFile.exists()) {
				files += nodeModulesPath.relativize(path)
			} else {
				notFound += nodeModulesPath.relativize(path)
			}

			persist()
	}


	private def persist(): Unit = {
		val structFormat = jsonFormat3(Struct)
		val json = structFormat.write(Struct(packages.map(e => Package(e._1, e._2)).toSeq, files.toSeq, notFound.toSeq))
		Files.write(
			Paths.get(BuildInfo.autodetectedVendorNpmFilesKnower.toURI()),
			json.prettyPrint.getBytes(StandardCharsets.UTF_8),
			StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
		)
	}

	private implicit def pathToString(path: Path): String =
		path.toString().replaceAll("""\\""", "/")

}




