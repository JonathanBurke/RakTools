package rak.javac

import java.io.File

import rak.augment.Conversions._

object JavacTools {

  /**
   * Splits a classpath using the pathSeparator, creates files from the resultant tokens, and
   * returns them as a list
   * @param classPath The classpath to separate
   * @return A list of Files corresponding to the classpath
   */
  def classpathToFiles(classPath : String) : List[File] =
    classPath
      .split(File.pathSeparator)
      .map(_.toFile)
      .toList

  def qualifiedNameToPath(packageStr : String) = packageStr.replace(".", File.separator)

  def fileToQualifiedName(file : File) = file.getAbsolutePath.replaceAll(File.separator, ".")
}
