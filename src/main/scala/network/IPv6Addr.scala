package network

/**
 * Class used to represent an IPv6 address (128 bits)
 */
class IPv6Addr(val addr: Seq[Short]) extends NetworkAddr(addr, 128, ":") {
  /**
   * Convert internal addr representation using human readable format
   *
   * @return Human readable format of IPv6 address in String
   * @example {{{
   *            val addr = IPv6Addr("2041:0000:140F:0000:0000:0000:AAAA:1CCC")
   *            addr.toString
   *            > "2041:0000:140F:0000:0000:0000:AAAA:1CCC"
   * }}}
   */
  override def toString = {
    super
      .toString
      .split(":")
      .map { i =>
        java.lang.Long.parseLong(i, 10)
          .toHexString
          .toUpperCase
          .reverse
          .padTo(4, '0')
          .reverse
      }
      .mkString(":")
  }
}

/**
 * Companion object used to initialize an IPv4 address
 */
object IPv6Addr extends TNetworkAddr[IPv6Addr] {
  /**
   * 2 ** 32 - 1
   */
  val MAX_NUM = 4294967295L
  val MIN_NUM = 0L

  /**
   * Use human readable format to create an IPv6Addr class instance
   *
   * @note IPv6 address shortening is NOT supported
   * @param v6Addr `String` representation of IPv6 address
   * @example `IPv6Addr("2041:0000:140F:0000:0000:0000:AAAA:1CCC")`
   * @return IPv6Addr class
   */
  def apply(v6Addr: String): IPv6Addr = {
    val addr = v6Addr
      .split(':')
      .map { i => java.lang.Long.parseLong(i, 16) }
      .map { i =>
        assert(MIN_NUM <= i && i <= MAX_NUM)
        if (i <= Short.MaxValue.toLong) {
          i.toShort
        } else {
          (-(i - Short.MaxValue.toLong)).toShort
        }
      }
    new IPv6Addr(addr)
  }
}
