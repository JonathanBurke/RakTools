package rak.maven

import java.io.File

import rak.augment.Conversions._
import rak.io.RakIo
import rak.javac.JavacTools

import scala.collection.mutable.ListBuffer

object MavenArgsUtil {

  def parseArgs(lines : Iterator[String]) : MavenArgs = {
    val classpathLines = new ListBuffer[String]
    val sourceFileLines = new ListBuffer[String]
    val packageLines = new ListBuffer[String]
    val miscArgs = new ListBuffer[String]

    lines.foreach(
      _.trim match {
        case "\"-classpath\"" =>  classpathLines += lines.next()
        case line if line.endsWith("package-info.java\"") => packageLines += line
        case line if line.endsWith(".java\"") => sourceFileLines += line
        case line => miscArgs += line
      })

    val packageFiles = packageLines.map( _.dropQuotes.toFile ).toSet[File]
    val sourceFiles = sourceFileLines.map( _.dropQuotes.toFile ).toSet[File]
    val classpaths =
      classpathLines
        .map( _.dropQuotes )
        .map( JavacTools.classpathToFiles _ )
        .flatten
        .toSet[File]

    new MavenArgs(sourceFiles, packageFiles, classpaths, miscArgs.toSet[String])
  }

  def parse(argsFile : File) : MavenArgs = {
    println( "Reading file: " + argsFile.getAbsolutePath )
    val source = scala.io.Source.fromFile(argsFile)
    val lines = source.getLines()

    val mavenArgs = parseArgs(lines)
    source.close()

    mavenArgs
  }

  def writeSummary(mavenArgs : MavenArgs, destFile : File) : Unit = {
    println( "Writing summary to file: " + destFile.getAbsolutePath )
    RakIo.writeLines(destFile, mavenArgs.summarize)
  }

  def writeSourceFile(mavenArgs : MavenArgs, filesToExclude : Option[Set[File]], destFile : File): Unit = {
    println( "Writing sources to file: " + destFile.getAbsolutePath )
    val filteredFiles = filesToExclude.map( mavenArgs.sourceFiles -- _ )
    RakIo.writeLines(destFile, filteredFiles)
  }

  def writeCompilerArgs(mavenArgs : MavenArgs, outputClassDir : File, destFile : File) : Unit = {
    println( "Writing classpath and arguments to file: " + destFile.getAbsolutePath )
    RakIo.writeLines( destFile,
      "-d", outputClassDir.getAbsolutePath,
      "-classpath", mavenArgs.classPath.mkString(File.pathSeparator)
    )
  }

  def writeIgnoredArgs(mavenArgs : MavenArgs, destFile : File) : Unit = {
    println( "Writing ignored args to file: " + destFile.getAbsolutePath )
    RakIo.writeLines( destFile, mavenArgs.miscArgs)
  }
}
