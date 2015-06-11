package rak.programs

import java.io.File

import rak.augment.Conversions._
import rak.io.RakIo
import rak.maven.{MavenArgsUtil, MavenArgs}

object SplitMavenArgs {

  def main(args: Array[String]): Unit = {

    if (args.length < 5) {
      printUsage("Too few arguments")
      System.exit(1)

    } else if (args.length > 6) {
      printUsage("Too many arguments")
      System.exit(1)

    }

    val inputFile = new File(args(0))
    val outputClassesDir = new File(args(1))
    val outputSources = new File(args(2))
    val outputOptions = new File(args(3))
    val outputIgnoredOptions = new File(args(4))
    val exclusionFile = if (args.length == 6) Some(new File(args(5))) else None

    MavenArgsUtil.splitMavenArgs(inputFile, outputClassesDir, outputSources,
                                 outputOptions, outputIgnoredOptions, exclusionFile)
  }

  def printUsage(errorMsg : String): Unit = {
    println("Error: " + errorMsg)
    println("ParseMavenArgs takes at least 5 arguments: input-file class-destionation-dir output-sources output-options output-ignored [exclusion-file]")
    println("input-file - file to parse")
    println("class-destination-dir - the destination of class files when inference is run")
    println("output-sources - the file in which to place the sources inference will be run over")
    println("output-options - the options file passed to the compiler which includes classpath and the destination dir (-d) specified by class-destination-dir")
    println("output-ignored - the file in which to write miscellaneous options found in input-file that were ignored")
    println("[exclusion-file] - a file which contains 1 source file per line.  These source files will be removed from the list of source files written to output-sources")
  }
}