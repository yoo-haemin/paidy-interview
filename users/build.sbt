name := "users"
version := "1.0.0"

scalaVersion := "2.12.6"
scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-Ypartial-unification",
  "-Ybackend-parallelism", "4"
)

libraryDependencies ++= {
  val http4sV = "0.18.15"

  Seq(
    "com.softwaremill.quicklens" %% "quicklens"           % "1.4.11",
    "co.fs2"                     %% "fs2-core"            % "0.10.3",
    "org.http4s"                 %% "http4s-blaze-server" % http4sV,
    "org.http4s"                 %% "http4s-blaze-client" % http4sV,
    "org.http4s"                 %% "http4s-dsl"          % http4sV,
    "org.http4s"                 %% "http4s-circe"        % http4sV,
    "io.circe"                   %% "circe-generic"       % "0.9.2",
    "org.slf4j"                  % "slf4j-simple"         % "1.7.25",
    "com.lihaoyi"                %% "utest"               % "0.6.4",
    "org.zalando"                %% "grafter"             % "2.4.1",
    compilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4"),
    compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
  )
}

testFrameworks += new TestFramework("utest.runner.Framework")
