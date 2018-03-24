name := "users"
version := "1.0.0"

scalaVersion := "2.12.5"
scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-Ypartial-unification"
)

libraryDependencies ++= {
  val http4sV = "0.18.3"

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
    compilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4")
  )
}

testFrameworks += new TestFramework("utest.runner.Framework")
