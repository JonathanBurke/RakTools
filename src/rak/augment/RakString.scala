package rak.augment

import java.io.File

import scala.util.matching.Regex

/**
 * A wrapper around string that provides useful operations.  @see rak.augment.Conversions
 */
class RakString(val str : String) {

  def toFile : File = new File(str)

  /**
   * Drops the outer set of double-quotes on this String.  Throws an exception if the
   * first or last character of this string does not equal "
   * @return a string without the outermost characters
   */
  def dropQuotes : String =
    if ( (str.toCharArray.head == '"') &&
         (str.toCharArray.last == '"') ) {
      str.substring(1, str.length - 1)
    } else {
      throw new IllegalArgumentException("String is not quoted: " + str)
    }

  /**
   * If this string is double-quoted, drops the outer set of quotes on this String.  Returns the original
   * string if it is not quoted
   * @return the string without it's outermost quotes or the original string if it's not quoted
   */
  def dropIfQuoted : String =
    if ( (str.toCharArray.head == '"') &&
      (str.toCharArray.last == '"') ) {
      str.substring(1, str.length - 1)
    } else {
      return str
    }

  /**
   * Finds the first match of regex and returns IT and everything to the right of it.
   * @return
   */
  def dropLeftOf(regex : String) = {
    val ToFind = regex.r
    ToFind.findFirstMatchIn(str) match {
      case Some (found : Regex.Match) => found.toString() + found.after
      case None => throw new IllegalArgumentException("Did not find " + regex + " in " + str)
    }
  }

  /**
   * Finds the first match of regex and returns IT and everything to the left of it.
   * @return
   */
  def dropRightOf(regex : String) = {
    val ToFind = regex.r
    ToFind.findFirstMatchIn(str) match {
      case Some (found : Regex.Match) => found.before + found.toString()
      case None => throw new IllegalArgumentException("Did not find " + regex + " in " + str)
    }
  }
}
