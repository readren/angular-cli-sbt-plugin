package webServer

import java.io.InputStream
import java.security.{ SecureRandom, KeyStore }
import javax.net.ssl.{ SSLContext, TrustManagerFactory, KeyManagerFactory }

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{ RouteResult, Route, Directives }
import akka.http.scaladsl.{ ConnectionContext, HttpsConnectionContext, Http }
import akka.stream.ActorMaterializer
import com.typesafe.sslconfig.akka.AkkaSSLConfig

object security {

	/** Manual HTTPS configuration. This code was taken from "http://doc.akka.io/docs/akka/current/scala/http/server-side-https-support.html" */
	def createHttpsConnectionContext(keyStoreFilePath: String, keyStorePassword: Array[Char])(implicit system: ActorSystem): HttpsConnectionContext = {

		val keyStore: KeyStore = KeyStore.getInstance("JKS")
		val keystoreStream: InputStream = getClass.getClassLoader.getResourceAsStream(keyStoreFilePath);

		require(keystoreStream != null, "Keystore required!")
		keyStore.load(keystoreStream, keyStorePassword)

		val keyManagerFactory: KeyManagerFactory = KeyManagerFactory.getInstance("SunX509")
		keyManagerFactory.init(keyStore, keyStorePassword)

		val tmf: TrustManagerFactory = TrustManagerFactory.getInstance("SunX509")
		tmf.init(keyStore)

		val sslContext: SSLContext = SSLContext.getInstance("TLS")
		sslContext.init(keyManagerFactory.getKeyManagers, tmf.getTrustManagers, new SecureRandom)
		ConnectionContext.https(sslContext)
	}

	def configureAndSetHttpsContext(keyStoreFilePath: String, password: Array[Char])(implicit system: ActorSystem) {
		val httpsConnectioniContext = createHttpsConnectionContext(keyStoreFilePath, password)
		// sets default context to HTTPS – all Http() bound servers for this ActorSystem will use HTTPS from now on
		Http().setDefaultServerHttpContext(httpsConnectioniContext);
	}
}