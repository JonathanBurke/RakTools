package rak.augment

import java.io.File

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

  def dropLeftOf(regex : String) = {
    val tokens = str.split(regex, 2)
    if (tokens.length != 2) {
      throw new IllegalArgumentException("Did not find " + regex + " in " + str)
    }
    regex + tokens(1)
  }

  def dropRightOf(regex : String) = {
    val tokens = str.split(regex, 2)
    if (tokens.length != 2) {
      throw new IllegalArgumentException("Did not find " + regex + " in " + str)
    }
    tokens(0) + regex
  }
}
