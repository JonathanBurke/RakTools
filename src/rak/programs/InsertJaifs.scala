package rak.programs

import java.io.File

import rak.typetools.JaifInserter

object InsertJaifs {

  def main(args : Array[String]) = {
    if (args.length < 3) {
      printError("Too few arguments!")
    }

    val jaifCommandFile = new File(args(0))
    val srcDir = new File(args(1))
    val logFile = new File(args(2))

    val perJaifoutputDir = if (args.length > 3) Some(new File(args(3))) else None

    JaifInserter.insertJaifs(jaifCommandFile, srcDir, logFile, perJaifoutputDir)
  }

  def printError(message : String): Unit = {
    println(message)
    printUsage()
    System.exit(1)
  }

  def printUsage() : Unit = {
    println("Usage: InsertJaifs jaifCommandFile logFile [perJaifOutputDir]")
    println("jaifCommandFile -- a file that consists of insert-annotations-to-source commands line by line")
    println("srcDir -- a directory, or parent directory that is within 2 levels of all src dirs") //TODO: CHANGE OR EXPLAIN THIS BETTER
    println("logFile -- the file to output the entire log to")
    println("perJaifOutputDir -- if this command is specified, then this directory is used to record the AFU output for each jaif inserted")
  }
}
