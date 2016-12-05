enablePlugins(BuildInfoPlugin);
	
name := "angular-cli-plugin-web-server";
scalaVersion := "2.11.8";
EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Managed;
		
lazy val akkaVersion = settingKey[String]("version to use for akka dependencies");
akkaVersion := "2.4.14";
lazy val akkaHttpVersion = settingKey[String]("version to use for akka http dependencies");
akkaHttpVersion := "10.0.0";

libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion.value,
	"com.typesafe.akka" %% "akka-stream" % akkaVersion.value,

	"com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion.value,
	"com.typesafe.akka" %% "akka-http" % akkaHttpVersion.value,
	"com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion.value
);

lazy val siteIndex = settingKey[String]("html to use as the site index");
siteIndex := "index.html";
buildInfoKeys += siteIndex;

lazy val nodeModulesDirectory = settingKey[File]("Directory containing the node modules managed by NPM");
nodeModulesDirectory := baseDirectory.value / "node_modules";
buildInfoKeys += nodeModulesDirectory;

lazy val autodetectedVendorNpmFilesKnower = settingKey[File]("Points to the file that contains the names of the vendor NPM files that were autodetected by the server. See `MissingResourcesManager`");
autodetectedVendorNpmFilesKnower := target.value / "autodetected-vendor-npm-files.json";
buildInfoKeys += autodetectedVendorNpmFilesKnower;

buildInfoPackage := "webServer";

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Managed

scalacOptions ++= Seq("-feature")