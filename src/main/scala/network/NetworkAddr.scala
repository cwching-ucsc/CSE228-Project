package network

/**
 * Base class used to represent a network address
 *
 * @param addr Address
 */
abstract class NetworkAddr(val addr: Long, val width: Int) {
  val binaryAddr: Vector[Int]

  /**
   * Compare two network addresses and make sure their width and address are equal.
   * @param obj Other `NetworkAddr`
   */
  override def equals(obj: Any): Boolean = {
    obj match {
      case o: NetworkAddr => this.width == o.width && this.addr == o.addr
      case _ => false
    }
  }
}

/**
 * Class used to represent an IPv4 address (32 bits)
 */
class IPv4Addr(override val addr: Long) extends NetworkAddr(addr, 32) {
  /**
   * Convert internal addr representation using human readable format
   * @return Human readable format of IPv4 address in String
   * @example {{{
   *            val addr = IPv4Addr("1.2.4.8")
   *            addr.toString
   *            > "1.2.4.8"
   * }}}
   */
  override def toString: String = {
    binaryAddr
      .grouped(8)
      .map { i =>
        i.reverse
          .map(i => i.toString.toInt)
          .zipWithIndex
          .map { case (n, idx) => math.pow(2, idx).toInt * n }
          .sum
      }
      .mkString(".")
  }

  /**
   * Convert internal addr representation using machine readable format
   * @return Machine readable format of IPv4 address in Vector[Int]
   * @example {{{
   *            val addr = IPv4Addr("1.2.4.8")
   *            addr.binaryAddr
   *            > Vector(0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0)
   * }}}
   */
  override val binaryAddr: Vector[Int] = {
    addr.toBinaryString
      .reverse
      .padTo(32, "0")
      .reverse
      .map(_.toString.toInt)
      .toVector
  }
}

/**
 * Companion object used to initialize an IPv4 address
 */
object IPv4Addr {
  val MAX_NUM = 4294967295L
  val MIN_NUM = 0L
  /**
   * Use human readable format to create an IPv4Addr class instance
   * @param v4Addr `String` representation of IPv4 address
   * @example `IPv4Addr("1.2.4.8")`
   * @return IPv4Addr class
   */
  def apply(v4Addr: String): IPv4Addr = {
    val addrNum = v4Addr
      .split('.')
      .reverse
      .zipWithIndex
      .map { case (n, i) => math.pow(256, i).toLong * n.toLong }
      .sum
    assert(MIN_NUM <= addrNum && addrNum <= MAX_NUM)
    new IPv4Addr(addrNum)
  }

  /**
   * Use machine readable format (binary) to create an IPv4Addr class instance
   * @param v4AddrVec `Vector[Int]` representation of IPv4 address (left to right)
   * @example `IPv4Addr(Vector(0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0))`
   * @return IPv4Addr class
   */
  def apply(v4AddrVec: Vector[Int]): IPv4Addr = {
    val addrNum = v4AddrVec
      .reverse
      .zipWithIndex
      .map { case (n, i) => math.pow(2, i).toLong * n.toLong }
      .sum
    assert(MIN_NUM <= addrNum && addrNum <= MAX_NUM)
    new IPv4Addr(addrNum)
  }

  /**
   * Use machine readable format (number) to create an IPv4Addr class instance
   * @param addrNum `Long` representation of IPv4 address (left to right)
   * @example `IPv4Addr(1L)`
   * @return IPv4Addr class
   */
  def apply(addrNum: Long): IPv4Addr = {
    new IPv4Addr(addrNum)
  }
}

/**
 * Class used to represent an IPv6 address (128 bits)
 * @todo Implement IPv6Addr
 */
class IPv6Addr(override val addr: Long) extends NetworkAddr(addr, 128) {
  override val binaryAddr: Vector[Int] = Vector.empty
}

// @todo Implement IPv6Addr
object IPv6Addr {
  def apply(v6Addr: String): IPv6Addr = {
    new IPv6Addr(v6Addr.replace(":", "").toInt)
  }
}


