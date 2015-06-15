package rak.typetools

import java.io.{Writer, File}

import rak.io.RakIo

import scala.collection.mutable

object JaifSplitter {

  val DefaultJaifName = new File("default.jaif")
  val SolvedJaifName = new File("solved.jaif")


  /**
   * Split a large jaif into smaller jaifs by package name.  The smaller jaifs are placed
   * in output dir.  This method also generates a shell script full of insert-annotations-to-source
   * commands that will insert all of the jaifs.
   * @param jaifFile The file to be split
   * @param outputDir The directory to place the output jaifs
   * @param commandFile The shell script in which to place the insert commands
   * @param header A header placed at the top of every jaif
   */
  def splitJaif(jaifFile : File, outputDir : File, commandFile : File, header : String ): Unit = {
    val insertionCommands = new mutable.HashSet[String]()
    val visitedFiles = new mutable.HashSet[File]()

    val jaifFileIterator = new JaifFileIterator(jaifFile)
    jaifFileIterator.open()

    jaifFileIterator.foreach(
      jaifPackage => {
        val outputJaif = new File(outputDir, jaifPackage.name + ".jaif")
        if (!visitedFiles.contains(outputJaif)) {
          RakIo.deleteFileOrFail(outputJaif)
          RakIo.appendLines(outputJaif, List(header, "\n"))

          insertionCommands += makeInsertionCommand(outputJaif, jaifPackage.name)
          visitedFiles += outputJaif
        }

        print("Writing to package file: " + outputJaif.getName + " -- ")

        val time = System.currentTimeMillis()
        RakIo.appendLines(outputJaif, jaifPackage.getLines)
        val timeWriting = time - System.currentTimeMillis()

        println("Done! " + (timeWriting / 1000f) + " seconds")
        System.out.flush()
      }
    )

    println("Writing " + insertionCommands.size + " insertions commands to file: " + commandFile.getAbsolutePath)
    RakIo.writeLines(commandFile, insertionCommands.toList)

    jaifFileIterator.close()
  }

  /**
   * Takes a list of fully qualified annotations and turns them into Jaif headers that
   * allow them to be used within insertions
   * @param fullyQualifiedAnnotations
   * @return
   */
  def makeHeader(fullyQualifiedAnnotations : List[String]) : String = {
    fullyQualifiedAnnotations
      .map( parseAnnotation _ )
      .map({
        case (packageName, className) =>
          "package " + packageName + ":\n" +
          "annotation " + className + ":"
      }).mkString("\n")
  }

  /**
   * This method does not handle nested annotations at the moment
   *
   * @param fullyQualifiedAnnotation
   * @return (packageName, AnnotationName)
   */
  def parseAnnotation(fullyQualifiedAnnotation : String ) : (String, String) = {
    val lastPeriod = fullyQualifiedAnnotation.lastIndexOf('.')
    fullyQualifiedAnnotation.splitAt(lastPeriod)
  }

  /**
   * Given a jaif file and the corresponding package name for that jaif file, return an insert-annotations-to-source
   * command that will find the correct files and insert the jaif
   */
  def makeInsertionCommand(jaifFile : File, packageName : String) : String = {
    val packagePath = packageName.replace(".", File.pathSeparator)
    val srcPath = "src" + File.pathSeparator + packagePath + File.pathSeparator + ".java"
    val findPackageSourcesCmd = "` find %1 -path '" + srcPath +"' -maxdepth 1`"

    "insert-annotations-to-source -i " + jaifFile.getAbsolutePath + " " + findPackageSourcesCmd
  }
}
