val BuildPath                     = "build"
val ExplodedWarWebInf             = BuildPath + "/orbeon-war/WEB-INF"
val ExplodedWarClassesPath        = ExplodedWarWebInf + "/classes"
val ExplodedWarResourcesPath      = ExplodedWarWebInf + "/resources"
val FormBuilderResourcesPathInWar = "forms/orbeon/builder/resources"
val BuildClasses                  = BuildPath + "/classes"
val BuildTestClasses              = BuildPath + "/test-classes"

val ScalaJSFileNameFormat = "((.+)-(fastopt|opt)).js".r

val fastOptJSToExplodedWar   = taskKey[Unit]("Copy fast-optimized JavaScript files to the exploded WAR.")
val fullOptJSToExplodedWar   = taskKey[Unit]("Copy full-optimized JavaScript files to the exploded WAR.")

def copyScalaJSToExplodedWar(sourceFile: File, rootDirectory: File) = {

  val (prefix, optType) =
    sourceFile.name match { case ScalaJSFileNameFormat(_, prefix, optType) ⇒ prefix → optType }

  val launcherName  = s"$prefix-launcher.js"
  val sourceMapName = s"${sourceFile.name}.map"

  val targetDir =
    rootDirectory / ExplodedWarResourcesPath / FormBuilderResourcesPathInWar / "scalajs"

  IO.createDirectory(targetDir)

  val names = List(
    sourceFile                                 → s"$prefix.js",
    (sourceFile.getParentFile / launcherName)  → launcherName,
    (sourceFile.getParentFile / sourceMapName) → sourceMapName
  )

  for ((file, newName) ← names)
    IO.copyFile(
      sourceFile           = file,
      targetFile           = targetDir / newName,
      preserveLastModified = true
    )
}

lazy val formBuilder = (project in file("builder")).
  enablePlugins(ScalaJSPlugin).
  settings(
    organization                   := "org.orbeon",
    name                           := "form-builder",
    version                        := "4.11-SNAPSHOT",

    scalaVersion                   := "2.11.7",

    scalaSource         in Compile := baseDirectory.value / "src" / "builder" / "scala",
    javaSource          in Compile := baseDirectory.value / "src" / "builder" / "java",
    resourceDirectory   in Compile := baseDirectory.value / "src" / "builder" / "resources",

    jsDependencies                 += RuntimeDOM,

    libraryDependencies            += "org.scala-js" %%% "scalajs-dom"    % "0.8.1",

    // Temporary, until there is an 0.8.1 of scalajs-jquery
    unmanagedBase                  := baseDirectory.value / "lib",
    jsDependencies                 +=  "org.webjars" % "jquery" % "2.1.3" / "2.1.3/jquery.js",
//    libraryDependencies            += "be.doeraene"  %%% "scalajs-jquery" % "0.8.1-SNAPSHOT",


    persistLauncher     in Compile := true,

    fastOptJSToExplodedWar := copyScalaJSToExplodedWar(
      (fastOptJS in Compile).value.data,
      baseDirectory.value.getParentFile
    ),

    fullOptJSToExplodedWar := copyScalaJSToExplodedWar(
      (fullOptJS in Compile).value.data,
      baseDirectory.value.getParentFile
    )
  )

lazy val core = (project in file(".")).
  settings(
    organization                 := "org.orbeon",
    name                         := "orbeon-core",
    version                      := "4.11-SNAPSHOT",

    scalaVersion                 := "2.11.7",

    scalaSource       in Compile := baseDirectory.value / "src" / "main" / "scala",
    javaSource        in Compile := baseDirectory.value / "src" / "main" / "java",
    resourceDirectory in Compile := baseDirectory.value / "src" / "main" / "resources",

    unmanagedBase                := baseDirectory.value / "lib",

    classDirectory    in Compile := baseDirectory.value / ExplodedWarClassesPath
  )

lazy val test = (project in file(".")).
  settings(
    organization                 := "org.orbeon",
    name                         := "orbeon-test",
    version                      := "4.11-SNAPSHOT",

    scalaVersion                 := "2.11.7",

    scalaSource       in Compile := baseDirectory.value / "src" / "test" / "scala",
    javaSource        in Compile := baseDirectory.value / "src" / "test" / "java",
    resourceDirectory in Compile := baseDirectory.value / "src" / "test" / "resources",

    unmanagedBase                := baseDirectory.value / "lib",

    unmanagedClasspath in Compile += baseDirectory.value / BuildClasses,

    classDirectory    in Compile := baseDirectory.value / BuildTestClasses
  )

sound.play(compile in Compile, Sounds.Blow, Sounds.Basso)
