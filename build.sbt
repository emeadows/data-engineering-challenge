name := "data-engineer-challenge"
organization := "com.mogtech"
scalaVersion := "2.12.11"

lazy val root = project
  .in(file("."))
  .configs(IntegrationTest extend Test)
  .enablePlugins(JavaAgent)
  .settings(
    scalaVersion := "2.12.11",
    scalacOptions := Seq(
          "-unchecked",
          "-feature",
          "-deprecation",
          "-encoding",
          "utf8",
          "-Xfatal-warnings", // Fail the compilation if there are any warnings.
          "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
          "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
          "-Xlint:package-object-classes", // Class or object defined in package object.
          "-Xlint:unsound-match", // Pattern match may not be typesafe.
          "-Yno-adapted-args", // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
          "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
          "-Ywarn-value-discard", // Warn when non-Unit expression results are unused.
          "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
          "-Ywarn-unused:locals", // Warn if a local definition is unused.
          "-Ywarn-unused:params", // Warn if a value parameter is unused.
          "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
          "-Ywarn-unused:privates" // Warn if a private member is unused.
        ),
    scalacOptions in (Compile, console) ~= (_.filterNot(Set("-Ywarn-unused:imports", "-Xfatal-warnings"))),
    libraryDependencies ++= Dependencies.all,
    parallelExecution in Test := false,
    fork in run := true,
    fork in Test := true,
    testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oD")
  )
