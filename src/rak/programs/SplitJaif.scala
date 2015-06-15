package rak.programs

import java.io.File

import rak.io.RakIo
import rak.typetools.JaifSplitter

object SplitJaif {

  def main(args : Array[String]) = {
    if (args.length < 4) {
      printError("Too few arguments!")
    }

    val (filePaths, annos) = args.splitAt(3)
    val files = filePaths.map( filePath => new File(filePath))

    val outputDir = files(1)
    RakIo.makeDirOrFail(outputDir)

    JaifSplitter.splitJaif( files(0), files(1), files(2), JaifSplitter.makeHeader( annos.toList) )
  }

  def printError(message : String): Unit = {
    println(message)
    printUsage()
    System.exit(1)
  }

  def printUsage() : Unit = {
    println("Usage: JaifSplitter jaifFile outputDir commandFile anno [anno ...]")
    println("jaifFile -- the file to split")
    println("outputDir -- the directory in which to put the new jaifs")
    println("commandFile -- the sh file in which to place sh commands to insert the new jaifs")
    println("anno -- annotations used in the jaif, there must be at least 1")
  }
}
