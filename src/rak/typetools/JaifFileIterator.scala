package rak.typetools

import java.io.{IOException, File}

import scala.collection.mutable.ListBuffer
import scala.io.Source

/**
 * A JaifFileIterator will open a jaif and provide an iterator that reads
 * Jaif information package by package.  Note, if a package occurs multiple times
 * then there will be multiple Package objects for this package; they will NOT
 * be aggregated into 1
 *
 * To use JaifIterator
 *
 * 1.  Create a new iterator:  new JaifFileIterator(jaifFile)
 * 2.  Open the iterator (this opens the file, creates a file handle)
 * 3.  Iterate over the iterator
 * 4.  Close the iterator (this will close the file handle)
 */
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

  //the package that will be returned by next()
  private var currentPackage : Option[Package] = None

  //the current package ends when we find another package name
  //this package name is stored here
  private var nextPackageName : Option[String] = None

  // a file source for jaifFile
  private var source : Option[Source] = None

  // a line iterator for source
  private lazy val lines = {
    if (!isOpen)
      throw new IOException("You must first call open: " + jaifFile.getAbsolutePath)
    else
      source.get.getLines()
  }

  /**
   * @return true if the file is currently open for reading
   */
  def isOpen = source.isDefined

  /**
   * opens the jaif file for reading, this must be called before hasNext, or next is called
   */
  def open() : Unit = {
    if (isOpen) {
      throw new IOException("JaifIterator is already open for file: " + jaifFile.getAbsolutePath)
    }

    source = Some(scala.io.Source.fromFile(jaifFile))
    readPackage()
  }

  def reset() : Unit = {
    if (isOpen) {
      close()
      open()
    }
  }

  /**
   * Closes the file handle for JaifFile, you must re-open it if you want to reset the iterator
   */
  def close() = {
    source.get.close()
    source = None
  }

  /**
   * Reads the next package in the file and sets it to currentPackage
   */
  private def readPackage() : Unit = {
    if (!lines.hasNext) {
      currentPackage = None
    }

    val lineBuffer = new ListBuffer[String]
    val insertionBuffer = new ListBuffer[Insertions]

    while ( lines.next.trim match {
      // if we have a packageName (and it isn't the first one)
      // create a package and stop reading lines
      case PackageRegex(packageName : String) =>
        if (nextPackageName.isDefined) {
          addInsertions(lineBuffer, insertionBuffer)
          currentPackage = Some(Package(nextPackageName.get, insertionBuffer.toList))
        }

        nextPackageName = Some(packageName)
        false //stop reading lines

      //each Insertions object corresponds to 1 block of insertions.
      //we break out the insertions so we can preserve the block structure of the Jaif
      case "" =>
        addInsertions(lineBuffer, insertionBuffer)
        true //continue reading lines

      //this line is part of an insertion
      case line : String =>
        lineBuffer += line
        true
    }) {}
  }

  /**
   * Creates an Insertions object using the lines in lineBuffer and clears the lineBuffer.  The resulting
   * insertions object is added to insertionBuffer.  If lineBuffer is empty, then no object is created or added
   */
  private def addInsertions(lineBuffer : ListBuffer[String], insertionBuffer : ListBuffer[Insertions]): Unit = {
    if (!lineBuffer.isEmpty) {
      insertionBuffer += Insertions(lineBuffer.toList)
      lineBuffer.clear()
    }
  }

  /**
   * Returns true if there are more packages in this iterator.  If this method returns false then
   * next will throw a NoSuchElementException.  IF this iterator is not open then this method
   * will throw an IOException exception
   */
  override def hasNext: Boolean = {
    checkOpen();
    currentPackage.isDefined
  }

  /**
   * @return the Package in this iterator.  Throws a NoSuchElementException if !hasNext
   */
  override def next(): Package = {
    checkOpen()
    if (!currentPackage.isDefined) {
      throw new NoSuchElementException("JaifFileIterator is empty: " + jaifFile.getAbsolutePath)
    }

    val previousPackage = currentPackage.get
    readPackage()
    previousPackage
  }

  /**
   * Throws a runtime exception if this iterator is not open
   */
  def checkOpen(): Unit = {
    if (!isOpen) {
      throw new IOException("JaifSplitter has not been opened: " + jaifFile.getAbsolutePath)
    }
  }
}