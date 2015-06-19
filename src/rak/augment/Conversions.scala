package rak.augment

/**
 * The augment package is intended to hold "Rich" or "Ops" wrappers for common Java, Scala, and project
 * specific classes (e.g. RakString is a wrapper that ads operations to java.lang.String).  It's so named
 * because we are augmenting these classes with new behaviors.
 *
 * The Conversions class holds implicit conversions for the classes in Augment.
 * To use:
 *
 * import rak.augment.Conversions._
 * And call methods as normally using implicit conversion.
 *
 * You can always import individual conversions instead:
 * import rak.augment.Conversions.strToRakString
 *
 */
object Conversions {

  implicit def strToRakString(str :String) = new RakString(str)
}
