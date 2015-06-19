package rak.typetools

import java.io.{Writer, FileWriter, BufferedWriter, File}

import rak.io.RakIo
import rak.javac.{JavacFileMapper, JavacTools}
import rak.augment.Conversions._

import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.sys.process._

import JaifSplitter.CommandRegex


/**
 * Note: The file that is parsed by this class can literally just be run as a shell script,
 * and probably should be when you're running it on small examples.  This class is written
 * so that I might spit out some information while monitoring the insertion process.
 */
object JaifInserter {

  case class JaifStats(index : Int, cmd : String, jaifFile : File, fileLength : Long) {
  }


  def insertJaifs( insertionFile : File, srcDir : File, outputLog : File, perJaifOutputDir : Option[File] ) : Unit = {

    val indexedInsertions =
      readInsertionCommands(insertionFile)
        .zipWithIndex

    //TODO: THIS IS TAILORED TO HADOOP
    val javaSrcFiles =
      JavacFileMapper.mapJavaFiles(srcDir)
        .filter(!_._1.getAbsolutePath.contains("src" + File.separator + "test"))
        .filter(_._1.getAbsolutePath.contains(RakIo.joinPath("org", "apache", "hadoop")))

    val packagePathToFiles =
      javaSrcFiles.map({
        case (dir : File, files : List[File]) => {
          val name = JavacTools.fileToQualifiedName(dir)
                               .dropLeftOf("org.apache.hadoop")
          name -> files
        }
      })

    val jaifStats : List[JaifStats] =
      indexedInsertions.flatMap(insertion => {
        val (cmd, index) = insertion
        cmd match {
          case CommandRegex(jaifPath : String, findCommand : String) =>

            val jaifFile = new File(jaifPath)
            val jaifPackageName = jaifFile.getName.dropRight(5)
            val files : Option[List[File]] = packagePathToFiles.get(jaifPackageName)

            if (!files.isEmpty) {
                val jaifLines = RakIo.numLines(jaifFile)
                val substitutedCommand = JaifSplitter.makeUnexpandedCommand(jaifFile, files.get)
                Some(JaifStats(index, substitutedCommand, jaifFile, jaifLines))

            } else {
                println("Could not find source file for : " + jaifPath + "\n")
                None
            }

          case cmd: String =>
            println("Skipping malformed insertion command #" + index + ": " + cmd)
            None
        }
       })

    //it would be nice to have a Conversion to a "RakNumberList" with a total and average method
    val totalLines : Long = jaifStats.foldLeft(0L)(_ + _.fileLength)

    var linesProcessed = 0L
    jaifStats.foreach(

      stats => {
        val percentProgress = calculateProgress(stats.fileLength, totalLines)
        println("Inserting Jaif: " + stats.jaifFile.getAbsolutePath)
        println("  with size: " + stats.fileLength + "  %" + percentProgress)
        println("  Command: " + stats.cmd)

        val time = System.currentTimeMillis()
        execute( stats, outputLog, perJaifOutputDir )
        val timeExecuting = System.currentTimeMillis() - time

        linesProcessed += stats.fileLength
        println("Finished: " + (timeExecuting / 1000f) + " seconds")
        println("Total progress: " + calculateProgress(linesProcessed, totalLines))

      })
  }

  private def calculateProgress(sizeCompleted : Long, total : Long) =
    (sizeCompleted.asInstanceOf[Double] / total.asInstanceOf[Double]) * 100

  private def executeFind( find : String, outputLog : File) : List[File] = {
    println( "Executing find: " + find)
    val lineBuffer = new ListBuffer[String]

    val successful = find !(ProcessLogger(lineBuffer += _, lineBuffer += _)) == 0

    val lines = lineBuffer.toList

    val logger = new JaifInserterProcessorLogger(outputLog, None, true)
    logger.writeCmd(find)
    lines.foreach( line => logger.out _ )
    logger.close()

    if (successful) {
      if (lines.isEmpty) {
        List.empty

      } else {
        lines.map(path => new File(path))
      }
    } else {
      List.empty

    }
  } 
  
  private def execute( jaifStat : JaifStats, outputLog : File, perJaifOutputDir : Option[File]) : Unit = {
    val jaifOutput = perJaifOutputDir.map( dir => new File(dir, jaifStat.jaifFile.getName + ".log"))
    val logger = new JaifInserterProcessorLogger(outputLog, jaifOutput)

    //run command and output to logger
    logger.writeCmd(jaifStat.cmd)
    jaifStat.cmd ! logger
    logger.flush()
    logger.close()
  }

  private def readInsertionCommands(insertionFile : File) : List[String] = {
    val source = Source.fromFile(insertionFile)
    val lines = source.getLines().toList
    source.close()
    lines
  }

  class JaifInserterProcessorLogger(val outputLog : File, val subJaifOutputFile : Option[File], val append : Boolean = true) extends ProcessLogger {
    val logWriter = new BufferedWriter(new FileWriter(outputLog, append))
    val jaifWriter = subJaifOutputFile.map( outputFile => new BufferedWriter(new FileWriter(outputFile, append)))

    def writeCmd(cmd : String) = out(cmd)

    override def out(str: => String): Unit = {
      val outStr = str  //call str method only once
      writeLn( logWriter, outStr )
      jaifWriter.map( wr => writeLn( wr, outStr) )
    }

    override def err(str: => String) : Unit = {
      val errStr = str  //call str method only once
      writeLn( logWriter, errStr )
      jaifWriter.map( wr => writeLn( wr, errStr) )
    }

    def flush() : Unit = {
      logWriter.flush()
      jaifWriter.map( _.flush() )
    }

    def close() : Unit = {
      flush()
      logWriter.close()
      jaifWriter.map( _.close() )
    }

    private def writeLn(writer : BufferedWriter, str : String): Unit = {
      writer.write(str)
      writer.newLine()
    }

    override def buffer[T](f: => T): T = { f }
  }

}
