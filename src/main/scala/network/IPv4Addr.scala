package network

/**
 * Class used to represent an IPv4 address (32 bits)
 */
class IPv4Addr(val addr: Seq[Byte]) extends NetworkAddr(addr.map(_.toShort), 32, ".") {
  /**
   * Convert internal addr representation using human readable format
   *
   * @return Human readable format of IPv4 address in String
   * @example {{{
   *            val addr = IPv4Addr("1.2.4.8")
   *            addr.toString
   *            > "1.2.4.8"
   * }}}
   */
  override def toString = super.toString
}

/**
 * Companion object used to initialize an IPv4 address
 */
object IPv4Addr extends TNetworkAddr[IPv4Addr] {
  val MAX_NUM = 255
  val MIN_NUM = 0

  private def unsignedHelper(i: Int): Byte = {
    if (i <= Byte.MaxValue.toInt) {
      i.toByte
    } else {
      (-(i - Byte.MaxValue.toInt)).toByte
    }
  }

  /**
   * Use human readable format to create an IPv4Addr class instance
   *
   * @param addr `String` representation of IPv4 address
   * @example `IPv4Addr("1.2.4.8")`
   * @return IPv4Addr class
   */
  def apply(addr: String): IPv4Addr = {
    new IPv4Addr(
      addr
        .split('.')
        .map(_.toInt)
        .toIndexedSeq
        .map { i =>
          assert(MIN_NUM <= i && i <= MAX_NUM)
          unsignedHelper(i)
        }
    )
  }

  /**
   * Use BigInt to create an IPv4Addr class instance
   *
   * @param number `BigInt` representation of IPv4 address
   * @example {{{
   *            IPv4Addr(BigInt(258)).toString
   *            > 0.0.1.2
   * }}}
   * @return IPv4Addr class
   */
  override def apply(number: BigInt): IPv4Addr = {
    assert(MIN_NUM <= number && number <= BigInt(1, Array.fill(4)(255.toByte)))
    val addr = number
      .toString(2)
      .reverse
      .padTo(32, '0')
      .reverse
      .grouped(8)
      .map { g =>
        unsignedHelper(java.lang.Integer.parseInt(g, 2))
      }
      .toSeq
    new IPv4Addr(addr)
  }
}
