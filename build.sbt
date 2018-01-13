// *****************************************************************************
// Projects
// *****************************************************************************

lazy val `whirlwind-tour-akka-typed` =
  project
    .in(file("."))
    .enablePlugins(AutomateHeaderPlugin, GitVersioning, DockerPlugin, JavaAppPackaging)
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaClusterShardingTyped,
        library.akkaClusterTools,
        library.akkaDistributedData,
        library.akkaHttp,
        library.akkaHttpCirce,
        library.akkaLog4j,
        library.akkaManagement,
        library.akkaStream,
        library.akkaPersistence,
        library.akkaPersistenceCassandra,
        library.akkaPersistenceQuery,
        library.akkaPersistenceTyped,
        library.catsCore,
        library.circeGeneric,
        library.circeRefined,
        library.disruptor,
        library.log4jApiScala,
        library.log4jCore,
        library.pureConfig,
        library.refined,
        library.refinedCats,
        library.scalapbRuntime   % "protobuf",
        library.akkaHttpTestkit  % Test,
        library.akkaTestkit      % Test,
        library.akkaTestkitTyped % Test,
        library.scalaCheck       % Test,
        library.scalaTest        % Test
      )
    )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val akka                     = "2.5.9"
      val akkaHttp                 = "10.0.11"
      val akkaHttpJson             = "1.19.0"
      val akkaLog4j                = "1.6.0"
      val akkaManagement           = "0.6"
      val akkaPersistenceCassandra = "0.59"
      val cats                     = "1.0.1"
      val circe                    = "0.9.0"
      val disruptor                = "3.3.7"
      val log4j                    = "2.10.0"
      val log4jApiScala            = "11.0"
      val pureConfig               = "0.8.0"
      val refined                  = "0.8.6"
      val scalaCheck               = "1.13.5"
      val scalaTest                = "3.0.4"
      val scalapb                  = com.trueaccord.scalapb.compiler.Version.scalapbVersion
    }
    val akkaClusterShardingTyped = "com.typesafe.akka"        %% "akka-cluster-sharding-typed"  % Version.akka
    val akkaClusterTools         = "com.typesafe.akka"        %% "akka-cluster-tools"           % Version.akka
    val akkaDistributedData      = "com.typesafe.akka"        %% "akka-distributed-data"        % Version.akka
    val akkaHttp                 = "com.typesafe.akka"        %% "akka-http"                    % Version.akkaHttp
    val akkaHttpTestkit          = "com.typesafe.akka"        %% "akka-http-testkit"            % Version.akkaHttp
    val akkaHttpCirce            = "de.heikoseeberger"        %% "akka-http-circe"              % Version.akkaHttpJson
    val akkaLog4j                = "de.heikoseeberger"        %% "akka-log4j"                   % Version.akkaLog4j
    val akkaManagement           = "com.lightbend.akka"       %% "akka-management-cluster-http" % Version.akkaManagement
    val akkaPersistence          = "com.typesafe.akka"        %% "akka-persistence"             % Version.akka
    val akkaPersistenceCassandra = "com.typesafe.akka"        %% "akka-persistence-cassandra"   % Version.akkaPersistenceCassandra
    val akkaPersistenceQuery     = "com.typesafe.akka"        %% "akka-persistence-query"       % Version.akka
    val akkaStream               = "com.typesafe.akka"        %% "akka-stream"                  % Version.akka
    val akkaTestkit              = "com.typesafe.akka"        %% "akka-testkit"                 % Version.akka
    val akkaPersistenceTyped     = "com.typesafe.akka"        %% "akka-persistence-typed"       % Version.akka
    val akkaTestkitTyped         = "com.typesafe.akka"        %% "akka-testkit-typed"           % Version.akka
    val catsCore                 = "org.typelevel"            %% "cats-core"                    % Version.cats
    val circeGeneric             = "io.circe"                 %% "circe-generic"                % Version.circe
    val circeRefined             = "io.circe"                 %% "circe-refined"                % Version.circe
    val disruptor                = "com.lmax"                 %  "disruptor"                    % Version.disruptor
    val log4jApiScala            = "org.apache.logging.log4j" %% "log4j-api-scala"              % Version.log4jApiScala
    val log4jCore                = "org.apache.logging.log4j" %  "log4j-core"                   % Version.log4j
    val log4jSlf4jImpl           = "org.apache.logging.log4j" %  "log4j-slf4j-impl"             % Version.log4j
    val pureConfig               = "com.github.pureconfig"    %% "pureconfig"                   % Version.pureConfig
    val refined                  = "eu.timepit"               %% "refined"                      % Version.refined
    val refinedCats              = "eu.timepit"               %% "refined-cats"                 % Version.refined
    val scalaCheck               = "org.scalacheck"           %% "scalacheck"                   % Version.scalaCheck
    val scalaTest                = "org.scalatest"            %% "scalatest"                    % Version.scalaTest
    val scalapbRuntime           = "com.trueaccord.scalapb"   %% "scalapb-runtime"              % Version.scalapb
  }

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val settings =
  commonSettings ++
  gitSettings ++
  scalafmtSettings ++
  protoSettings ++
  dockerSettings ++
  commandAliases

lazy val commonSettings =
  Seq(
    // scalaVersion from .travis.yml via sbt-travisci
    // scalaVersion := "2.12.4",
    organization := "de.heikoseeberger",
    organizationName := "Heiko Seeberger",
    startYear := Some(2017),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-target:jvm-1.8",
      "-encoding", "UTF-8",
      "-Ypartial-unification"
    ),
    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
    Test / unmanagedSourceDirectories := Seq((Test / scalaSource).value),
    Compile / packageDoc / publishArtifact := false,
    Compile / packageSrc / publishArtifact := false
  )

lazy val gitSettings =
  Seq(
    git.useGitDescribe := true
  )

lazy val scalafmtSettings =
  Seq(
    scalafmtOnCompile := true
  )

lazy val protoSettings =
  Seq(
    Compile / PB.targets :=
      Seq(scalapb.gen(flatPackage = true) -> (Compile / sourceManaged).value)
  )

lazy val dockerSettings =
  Seq(
    Docker / daemonUser := "root",
    Docker / maintainer := "Heiko Seeberger",
    Docker / version := "latest",
    dockerBaseImage := "openjdk:8u151-slim",
    dockerExposedPorts := Vector(8000),
    dockerRepository := Some("hseeberger")
  )

lazy val commandAliases =
  addCommandAlias(
    "r0",
    """|reStart
       |---
       |-Dwtat.use-cluster-bootstrap=off
       |-Dcassandra-journal.contact-points.0=127.0.0.1:9042
       |-Dcassandra-snapshot-store.contact-points.0=127.0.0.1:9042
       |-Dakka.remote.netty.tcp.hostname=127.0.0.1
       |-Dakka.remote.netty.tcp.port=2550
       |-Dakka.cluster.seed-nodes.0=akka.tcp://wtat@127.0.0.1:2550""".stripMargin
  ) ++
  addCommandAlias(
    "r1",
    """|reStart
       |---
       |-Dwtat.use-cluster-bootstrap=off
       |-Dwtat.api.port=8001
       |-Dcassandra-journal.contact-points.0=127.0.0.1:9042
       |-Dcassandra-snapshot-store.contact-points.0=127.0.0.1:9042
       |-Dakka.remote.netty.tcp.hostname=127.0.0.1
       |-Dakka.remote.netty.tcp.port=2551
       |-Dakka.cluster.seed-nodes.0=akka.tcp://wtat@127.0.0.1:2550""".stripMargin
  )
