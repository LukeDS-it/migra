import sbt.*
import sbt.Keys.*

object CompilerSettings {
  lazy val settings: Seq[Def.Setting[?]] = Seq(
    Compile / scalacOptions ++= scalaCompilerOptions
  )
  private val scalaCompilerOptions = Seq(
    "-encoding", // Specify character encoding used by source files
    "UTF-8",
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-unchecked", // Enable detailed unchecked (erasure) warnings
    "-feature", // more verbose warning on language features that have not been enabled
    "-Ywarn-dead-code", // compiler generates warnings for some unreachable pieces of code
    "-Ywarn-numeric-widen", // warning on numeric implicit conversions to a wider type
    "-Wconf:cat=other-match-analysis:error",
    "-Xsource:3"
  )
}
