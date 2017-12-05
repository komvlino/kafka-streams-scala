import sbtassembly.MergeStrategy
import NativePackagerHelper._

import Dependencies._

name in ThisBuild := "iq-example-dsl"

organization in ThisBuild := "com.lightbend"

version in ThisBuild := "0.0.1"

scalaVersion in ThisBuild := Versions.scalaVersion

(sourceDirectory in AvroConfig) := baseDirectory.value / "src/main/resources/com/lightbend/kafka/scala/iq/example"
(stringType in AvroConfig) := "String"

def appProject(id: String)(base:String = id) = Project(id, base = file(base))
  .enablePlugins(JavaAppPackaging)

lazy val app = appProject("app")(".")
  .settings(
    scalaVersion := Versions.scalaVersion,
    libraryDependencies ++= Seq(
      ks,
      kq,
      algebird,
      chill,
      alpakka,
      reactiveKafka,
      bijection,
      confluentAvro,
      akkaSlf4j,
      akkaHttp,
      akkaHttpCirce,
      akkaStreams,
      circeCore,
      circeGeneric,
      circeParser,
      logback,
      scalaLogging
    ),
    scalacOptions ++= Seq(
      "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
      "-encoding", "utf-8",                // Specify character encoding used by source files.
      "-explaintypes",                     // Explain type errors in more detail.
      "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
      "-language:existentials",            // Existential types (besides wildcard types) can be written and inferred
      "-language:experimental.macros",     // Allow macro definition (besides implementation and application)
      "-language:higherKinds",             // Allow higher-kinded types
      "-language:implicitConversions",     // Allow definition of implicit functions called views
      "-language:postfixOps",              // Allow postfix operator
      "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
      "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
      "-Xfatal-warnings",                  // Fail the compilation if there are any warnings.
      "-Xfuture",                          // Turn on future language features.
      "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
      "-Xlint:by-name-right-associative",  // By-name parameter of right associative operator.
      "-Xlint:constant",                   // Evaluation of a constant arithmetic expression results in an error.
      "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
      "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
      "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
      "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
      "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
      "-Xlint:nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
      "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
      "-Xlint:option-implicit",            // Option.apply used implicit view.
      "-Xlint:package-object-classes",     // Class or object defined in package object.
      "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
      "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
      "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
      "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
      "-Xlint:unsound-match",              // Pattern match may not be typesafe.
      "-Yno-adapted-args",                 // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
      "-Ypartial-unification",             // Enable partial unification in type constructor inference
      "-Ywarn-dead-code",                  // Warn when dead code is identified.
      "-Ywarn-extra-implicit",             // Warn when more than one implicit parameter section is defined.
      "-Ywarn-inaccessible",               // Warn about inaccessible types in method signatures.
      "-Ywarn-infer-any",                  // Warn when a type argument is inferred to be `Any`.
      "-Ywarn-nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
      "-Ywarn-nullary-unit",               // Warn when nullary methods return Unit.
      "-Ywarn-unused:implicits",           // Warn if an implicit parameter is unused.
      "-Ywarn-unused:locals",              // Warn if a local definition is unused.
      "-Ywarn-unused:params",              // Warn if a value parameter is unused.
      "-Ywarn-unused:patvars",             // Warn if a variable bound in a pattern is unused.
      "-Ywarn-unused:privates",            // Warn if a private member is unused.
      "-Ywarn-value-discard"               // Warn when non-Unit expression results are unused.
    ),
    assemblyMergeStrategy in assembly := {
      case PathList("application_dsl.conf") => MergeStrategy.discard
      case PathList("logback-dsl.xml") => MergeStrategy.discard
      case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
      case PathList("META-INF", xs @ _*) => MergeStrategy.last
      case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.last
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    }
  )

lazy val dslRun = project
  .in(file("run/dsl"))
  .settings (
    fork in run := true,
    mainClass in Compile := Some("com.lightbend.kafka.scala.iq.example.WeblogProcessing"),
    resourceDirectory in Compile := (resourceDirectory in (app, Compile)).value,
    javaOptions in run ++= Seq(
      "-Dconfig.file=" + (resourceDirectory in Compile).value / "application-dsl.conf",
      "-Dlogback.configurationFile=" + (resourceDirectory in Compile).value / "logback-dsl.xml"),
    addCommandAlias("dsl", "dslRun/run")
  )
  .dependsOn(app)

lazy val dslPackage = appProject("dslPackage")("build/dsl")
  .settings(
    scalaVersion := Versions.scalaVersion,
    resourceDirectory in Compile := (resourceDirectory in (app, Compile)).value,
    mappings in Universal ++= {
      Seq(((resourceDirectory in Compile).value / "application-dsl.conf") -> "conf/application.conf") ++
        Seq(((resourceDirectory in Compile).value / "logback-dsl.xml") -> "conf/logback.xml")
    },
    scriptClasspath := Seq("../conf/") ++ scriptClasspath.value,
    mainClass in Compile := Some("com.lightbend.kafka.scala.iq.example.WeblogProcessing")
  )
  .dependsOn(app)

