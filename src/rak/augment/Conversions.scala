package rak.augment

object Conversions {

  implicit def strToRakString(str :String) = new RakString(str)
}
