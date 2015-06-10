package rak.programs

import java.io.File

import rak.maven.{MavenArgs, MavenArgsUtil}

object SummarizeMavenArgs {

  def main(args : Array[String]) : Unit = {

    if (args.length != 2) {
      println( "ParseMavenArgs takes exactly 2 arguments: input-file output-file" )
      System.exit(1)
    }

    val sourceFile = new File(args(0))
    val destFile = new File(args(1))

    val mavenArgs : MavenArgs = MavenArgsUtil.parse(sourceFile)
    MavenArgsUtil.writeSummary(mavenArgs, destFile)
  }

}

