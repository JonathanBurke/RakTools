package rak.javac

import java.io.{File, IOException}
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file._

import scala.collection.mutable.ListBuffer

object JavacFileMapper {

  def findJavaFiles(file : File) : List[File] = {
    val path = Paths.get(file.getAbsolutePath)
    val mapper = new JavacFileMapper
    Files.walkFileTree(path, mapper)
    mapper.pathBuffer
      .toList
      .map(filePathStr => new File(filePathStr))
  }

  def mapJavaFiles(file : File) : Map[File, List[File]] = {
    findJavaFiles(file)
      .groupBy(_.getParentFile)
  }
}

private class JavacFileMapper  extends SimpleFileVisitor[Path] {

  val pathBuffer = new ListBuffer[String]

  override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
    if ( !attrs.isSymbolicLink ) {

      if (file.toString().endsWith(".java") && !attrs.isDirectory) {
        pathBuffer += file.toString
      }

      FileVisitResult.CONTINUE

    } else {
      FileVisitResult.SKIP_SUBTREE

    }
  }
}