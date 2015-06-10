package rak.maven

import java.io.File

case class MavenArgs(sourceFiles : Set[File], packageInfos : Set[File], classPath : Set[File], miscArgs : Set[String]) {

  def summarize : String = {
    val sb = new StringBuilder
    sb ++= "Source Files: \n"
    sb ++= sourceFiles.map(_.getAbsolutePath).mkString("\n")
    sb ++= "\nClass Path: \n"
    sb ++= classPath.map(_.getAbsolutePath).mkString(":")
    sb ++= "\nMisc. Args: \n"
    sb ++= miscArgs.mkString("\n")
    sb.toString
  }
}