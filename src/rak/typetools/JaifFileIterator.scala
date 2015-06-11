package rak.typetools

import java.io.File

import scala.collection.mutable.ListBuffer
import scala.io.Source

object JaifFileIterator {

  val PackageRegex = """package (.*)""".r

  case class Insertions(lines: List[String])
  case class Package(name: String, entries: List[Insertions]) {

    def getLines = {
      ("package " + name) ++
      entries.map(_.lines ++ "\n").flatten
    }
  }
}

import JaifFileIterator._

class JaifFileIterator(jaifFile : File) extends Iterator[Package] {

  private var currentPackage : Option[Package] = None
  private var nextPackageName : Option[String] = None

  private var source : Option[Source] = None
  private lazy val lines = {
    if (!isOpen)
      throw new RuntimeException("You must first call open!")
    else
      source.get.getLines()
  }

  def isOpen = source.isDefined

  def open() = {
    source = Some(scala.io.Source.fromFile(jaifFile))
    readPackage()
  }

  def close() = {
    source.get.close()
    source = None
  }

  private def readPackage() : Unit = {
    if (!lines.hasNext) {
      currentPackage = None
    }

    val lineBuffer = new ListBuffer[String]
    val insertionBuffer = new ListBuffer[Insertions]

    while ( lines.next.trim match {
      case PackageRegex(packageName : String) =>
        if (nextPackageName.isDefined) {
          addInsertions(lineBuffer, insertionBuffer)
          currentPackage = Some(Package(nextPackageName.get, insertionBuffer.toList))
        }

        nextPackageName = Some(packageName)
        false

      case "" =>
        addInsertions(lineBuffer, insertionBuffer)
        true

      case line : String =>
        lineBuffer += line
        true
    }) {}
  }

  private def addInsertions(lineBuffer : ListBuffer[String], insertionBuffer : ListBuffer[Insertions]): Unit = {
    if (!lineBuffer.isEmpty) {
      insertionBuffer += Insertions(lineBuffer.toList)
      lineBuffer.clear()
    }
  }

  override def hasNext: Boolean = {
    checkOpen();
    currentPackage.isDefined
  }

  override def next(): Package = {
    checkOpen()
    if (!currentPackage.isDefined) {
      throw new NoSuchElementException("JaifFileIterator is empty")
    }

    val previousPackage = currentPackage.get
    readPackage()
    previousPackage
  }

  def checkOpen(): Unit = {
    if (!isOpen) {
      throw new RuntimeException("JaifSplitter has not been opened.")
    }
  }
}