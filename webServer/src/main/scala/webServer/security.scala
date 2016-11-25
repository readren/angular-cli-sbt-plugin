package webServer

import java.io.InputStream
import java.security.{ SecureRandom, KeyStore }
import javax.net.ssl.{ SSLContext, TrustManagerFactory, KeyManagerFactory }

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{ RouteResult, Route, Directives }
import akka.http.scaladsl.{ ConnectionContext, HttpsConnectionContext, Http }
import akka.stream.ActorMaterializer
import com.typesafe.sslconfig.akka.AkkaSSLConfig
import akka.event.Logging
import akka.event.LoggingAdapter
import akka.event.LogSource

object security {

	/** Manual HTTPS configuration. This code was taken from http://doc.akka.io/docs/akka-http/current/scala/http/server-side-https-support.htm */
	def createAndInitSslContext(keystoreStream: InputStream, keyStorePassword: Array[Char], keyStoreType: String = "JKS")(implicit log: LoggingAdapter): SSLContext = {
		log.debug("Creating the https connection context.");

		val keyStore: KeyStore = KeyStore.getInstance(keyStoreType); // TODO en la nueva versión de la doc usa "PKCS12" en lugar de "JKS". Investigar cuál es la diferencia y cambiarlo. 
		keyStore.load(keystoreStream, keyStorePassword)

		val keyManagerFactory: KeyManagerFactory = KeyManagerFactory.getInstance("SunX509")
		keyManagerFactory.init(keyStore, keyStorePassword)

		val trustManagerFactory: TrustManagerFactory = TrustManagerFactory.getInstance("SunX509")
		trustManagerFactory.init(keyStore)

		val sslContext: SSLContext = SSLContext.getInstance("TLS")
		log.debug("Key managers={}", keyManagerFactory.getKeyManagers);
		log.debug("Trust managers={}", trustManagerFactory.getTrustManagers);
		sslContext.init(keyManagerFactory.getKeyManagers, trustManagerFactory.getTrustManagers, new SecureRandom)
		sslContext
	}

	def configureAndSetHttpsContext(keystoreStream: InputStream, keyStorePassword: Array[Char], keyStoreType: String = "JKS")(implicit actorSystem: ActorSystem) {
		val log = akka.event.Logging(actorSystem, this.getClass())
		val sslContext = createAndInitSslContext(keystoreStream, keyStorePassword)(log);
		val httpsConnectionContext = ConnectionContext.https(sslContext)
		log.debug("Https connection context created with: isSecure={}, enabledProtocols={}, enabledCipherSuites={}", httpsConnectionContext.isSecure, httpsConnectionContext.enabledProtocols, httpsConnectionContext.enabledCipherSuites);

		// sets default context to HTTPS – all Http() bound servers for this ActorSystem will use HTTPS from now on
		Http().setDefaultServerHttpContext(httpsConnectionContext);
	}
}