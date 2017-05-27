
name := "reactive-microservice-prac"



lazy val commonSettings = Seq(
  version := "1.0",
  scalaVersion := "2.11.7"
)

lazy val projectSetting = Seq(
  organization := "reactive-microservice-prac",
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-encoding", "utf8")
)

lazy val `auth-codecard` = (project in file("auth-codecard")).settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      `slick`,
      `postgres`,
      "com.typesafe.slick" %% "slick-hikaricp" % "3.1.1",
      `akka`,
      `akkaHttp`,
      `akkaStream`,
      `akkaSpray`
    )
  )

lazy val `identity-manager` = (project in file("identity-manager")).settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      `slick`,
      `postgres`,
      "com.typesafe.slick" %% "slick-hikaricp" % "3.1.1",
      `akka`,
      `akkaHttp`,
      `akkaStream`,
      `akkaSpray`
    )
  )

lazy val metricsCommon = (project in file("metric-common")).settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      `akka`,
      `akkaHttp`,
      `akkaStream`,
      `akkaSpray`
    )
  )

lazy val `metric-collector` = (project in file("metric-collector")).dependsOn(metricsCommon).enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      `akka`,
      `akkaHttp`,
      `akkaStream`,
      `akkaSpray`,
      `reactivemongo`,
      `slf4`
    )
  )

lazy val `token-manager` = (project in file("token-manager")).dependsOn(metricsCommon)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      `akka`,
      `akkaHttp`,
      `akkaStream`,
      `akkaSpray`,
      `reactivemongo`,
      `slf4`,
      `akkaTestKit`,
      `scalaTest`,
      `scalaCheck`
    )
  )

lazy val `reactive-microservice-prac` = (project in file(".")).settings(projectSetting: _*).aggregate(`auth-codecard`,`identity-manager`, metricsCommon, `metric-collector`, `token-manager`)



val `slickV` = "3.1.1"
val `slf4jV` = "1.7.22"
val `akkaV` = "2.4.14"
val postgresV = "9.4-1201-jdbc41"
val `akkaHttpV` = "10.0.0"
val `reactiveMongoV` = "0.12.1"
val `slick` = "com.typesafe.slick" %% "slick"  % slickV
val `akka` = "com.typesafe.akka" %% "akka-actor" % akkaV
val `slf4` =    "org.slf4j" % "slf4j-simple" % slf4jV
val `akkaStream` = "com.typesafe.akka" %% "akka-stream" % akkaV
val `akkaHttp` =  "com.typesafe.akka" %% "akka-http" % akkaHttpV
val `akkaSpray` = "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV
val `akkaTestKit` =  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV
val `reactivemongo` = "org.reactivemongo" %% "reactivemongo" % reactiveMongoV
val `postgres` = "org.postgresql" % "postgresql" % postgresV
val `scalaTest` =  "org.scalatest" %% "scalatest" % "3.0.0"
val `scalaCheck` =  "org.scalacheck" % "scalacheck_2.11" % "1.13.1"

val runAll = inputKey[Unit]("Run all subprojects")

runAll := {
  (run in Compile in `token-manager`).evaluated
  (run in Compile in `identity-manager`).evaluated
  (run in Compile in `auth-codecard`).evaluated
}

fork in run := true