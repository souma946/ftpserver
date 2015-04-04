lazy val root = (project in file(".")).
  settings(
    name := "ftpserver",
    version := "0.0.1",
    scalaVersion := "2.11.6",
	libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.9"
  )
