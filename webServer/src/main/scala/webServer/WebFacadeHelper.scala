package webServer

import scala.language.postfixOps

import akka.NotUsed
import akka.actor.ActorSystem
import akka.actor.actorRef2Scala
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable.apply
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directive.addByNameNullaryApply
import akka.http.scaladsl.server.Directive.addDirectiveApply
import akka.http.scaladsl.server.Directives._enhanceRouteWithConcatenation
import akka.http.scaladsl.server.Directives._segmentStringToPathMatcher
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Directives.encodeResponse
import akka.http.scaladsl.server.Directives.extractRequest
import akka.http.scaladsl.server.Directives.extractUnmatchedPath
import akka.http.scaladsl.server.Directives.getFromResource
import akka.http.scaladsl.server.Directives.logResult
import akka.http.scaladsl.server.Directives.pathPrefix
import akka.http.scaladsl.server.Directives.pathSingleSlash
import akka.http.scaladsl.server.Directives.reject
import akka.http.scaladsl.server.PathMatcher
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteResult
import akka.http.scaladsl.server.directives.LogEntry
import akka.http.scaladsl.server.directives.LoggingMagnet.forMessageFromFullShow
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.actor.ActorRef
import scala.concurrent.Future

class WebFacadeHelper(missingResourcesManager: ActorRef)(implicit actorSystem: ActorSystem, materializer: ActorMaterializer) {
	val log = Logging.getLogger(actorSystem, this);

	def indexRoute(indexFilePath: String = "index.html", isAngularRouteMatcher: PathMatcher[Unit] = PathMatcher("ar" /)): Route = // "ar" stands for angular router
		(pathSingleSlash | pathPrefix(isAngularRouteMatcher)) {
			getFromResource(indexFilePath)
		}

	def defaultFilter: String => Boolean = fileName => fileName.endsWith(".html") || fileName.endsWith(".js") || fileName.endsWith(".css") || fileName.endsWith(".map") || fileName.endsWith(".ico") || fileName.endsWith(".png") || fileName.endsWith(".mp3");
	
	def staticFilesRoute(filter: String => Boolean = defaultFilter): Route =
		extractUnmatchedPath { path =>
			assert(path.head.toString == "/")
			val fileName = path.tail.toString
			log.debug("path={}, fileName={}, head={}, tail={}", path, fileName, path.head, path.tail);
			if (filter(fileName))
				getFromResource(fileName)
			else {
				missingResourcesManager ! path;
				reject
			}
		}

	def apiRoute(apiHandlerRoute: Route): Route =
		pathPrefix("api") {
			apiHandlerRoute ~
				complete(StatusCodes.NotImplemented)
		}

	/**Produces a log entry for every RouteResult. The log entry includes the request URI */
	def logAccess(innerRoute: Route): Route = {
		extractRequest { request =>
			val in = s"${request.method.name} ${request.uri} ==> "
			logResult((r: Any) => r match {
				case c: RouteResult.Complete =>
					LogEntry(
						in + s"${c.response.status}",
						if (c.response.status.isSuccess())
							akka.event.Logging.InfoLevel
						else
							akka.event.Logging.ErrorLevel
					)
				case c: RouteResult.Rejected =>
					LogEntry(
						in + s"request rechazado con " + c.rejections,
						akka.event.Logging.WarningLevel
					)
				case x =>
					LogEntry(
						in + s"unknown response part of type ${x.getClass}",
						akka.event.Logging.ErrorLevel
					)
			})(innerRoute)
		}
	}

	def compoundRoute(apiHandlerRoute: Route): Route =
		logAccess {
			encodeResponse {
				apiRoute(apiHandlerRoute) ~ indexRoute() ~ staticFilesRoute()
			}
		}

	def bindAndHandle(apiHandlerRoute: Route, interface: String = "0.0.0.0", port: Int = 9000): Future[Http.ServerBinding] = {
		val routeFlow: Flow[HttpRequest, HttpResponse, NotUsed] = Route.handlerFlow(compoundRoute(apiHandlerRoute))
		val fServerBinding = Http().bindAndHandle(routeFlow, interface = interface, port = port)
		log.info(s"listening to $interface:$port")
		fServerBinding
	}

}

