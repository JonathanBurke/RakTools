package rak.typetools

import java.io.{Writer, File}

import rak.io.RakIo

import scala.collection.mutable

object JaifSplitter {

  val DefaultJaifName = new File("default.jaif")
  val SolvedJaifName = new File("solved.jaif")
  val CommandRegex = """^insert-annotations-to-source -i "(.*)" `(.*)`$""".r

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
        case (packageName : String, className : String, argsList : List[String]) =>
          "package " + packageName + ":\n" +
          "annotation " + className + ":"  +
          argsList.mkString("\n")

      }).mkString("\n")
  }


  val AnnotationRegex = """^((?:\w+\.)*)(\w+)\[(.*)\]$""".r
  /**
   * This method does not handle nested annotations at the moment
   *
   * @param fullyQualifiedAnnotation
   * @return (packageName, AnnotationName)
   */
  def parseAnnotation(fullyQualifiedAnnotation : String ) : (String, String, List[String]) = {
    fullyQualifiedAnnotation match {
      case AnnotationRegex(packageNameStr : String, className : String, args : String) => {
        val packageName = packageNameStr.dropRight(1) //drop trailing (.)
        val annotatedName = "@" + className
        val argsList = args.split(",").toList.filter(!_.equals(""))

        (packageName, annotatedName, argsList)
      }

      case malformed =>
        throw new IllegalArgumentException(
          "Annotations must be in the form: package.path.ClassName[arg1,arg2,...,argN]\n" +
          "found: " + malformed
        )
    }
  }

  /**
   * Given a jaif file and the corresponding package name for that jaif file, return an insert-annotations-to-source
   * command that will find the correct files and insert the jaif
   */
  def makeInsertionCommand(jaifFile : File, packageName : String) : String = {
    val packagePath = packageName.replace(".", File.separator)
    //right now this is tailored for hadoop
    val srcPath = "**/src/main/java" + File.separator + packagePath + File.separator + "*.java"
    val findPackageSourcesCmd = "` find $1 -path " + srcPath + "`"

    "insert-annotations-to-source -i \"" + jaifFile.getAbsolutePath + "\" " + findPackageSourcesCmd
  }

  def makeUnexpandedCommand(jaifFile : File, srcFiles : List[File]) : String = {
    "insert-annotations-to-source -i " + jaifFile.getAbsolutePath + " " + srcFiles.mkString(" ")
  }
}
