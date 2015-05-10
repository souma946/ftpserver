lazy val root = (project in file(".")).
  settings(
    name := "ftpserver",
    version := "0.0.2",
    scalaVersion := "2.11.6",
  	libraryDependencies ++= Seq(
  		"com.typesafe.akka" %% "akka-actor" % "2.3.9",
		  "net.java.dev.jna" % "jna-platform" % "4.0.0" ,
      "org.kohsuke" % "libpam4j" % "1.8"
    )
  )
