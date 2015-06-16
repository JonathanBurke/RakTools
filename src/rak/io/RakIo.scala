package rak.io

import java.io.{FileWriter, BufferedWriter, File}

/**
 */
object RakIo {

  /**
   * Writes the input set of lines to the given file
   * @param file destination file for lines
   * @param append whether or not to append the lines to the file
   * @param lines the lines to write
   */
  private def writeLines(file : File, append : Boolean, lines : Traversable[_]): Unit = {
    val writer = new BufferedWriter(new FileWriter(file, append))
    lines.foreach(
      line => {
        writer.write(line.toString)
        writer.newLine()
      })

    writer.flush()
    writer.close()
  }

  /**
   * Writes the input lines to the given file, this method does not APPEND (see appendLines
   */
  def writeLines(file : File, lines : Traversable[_]): Unit = {
    writeLines(file, false, lines)
  }

  /**
   * Appends lines to the given file, creates the file if not already created
   */
  def appendLines(file : File, lines : Traversable[_]): Unit = {
    writeLines(file, true, lines)
  }
  /**
   * Read all of the lines from file NOW (do not do it lazily)
   * @param file The file to read
   * @return A list of strings corresponding to the lines in the given file using the OS's newline character
   */
  def readLinesNow(file : File) : List[String] = {
    val source = scala.io.Source.fromFile(file)
    val lines = source.getLines().toList
    source.close()
    lines
  }


  def deleteFileOrFail(file: File): Unit = {
    if (file.exists()) {
      if (!file.delete()) {
        throw new RuntimeException("Could not delete file: " + file.getAbsolutePath)
      }
    }
  }

  def makeDirOrFail(file : File) : Unit = {
    if (!file.exists() && !file.mkdir()) {
      throw new RuntimeException("Could not create directory: " + file.getAbsolutePath)
    }
  }

  def joinPath(strs : String*) = {
    strs.mkString(File.separator)
  }
}
