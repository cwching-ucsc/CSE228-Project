package chiselRouter

/**
 * Base class used to represent a network address
 *
 * @param addr Address
 */
class NetworkAddr(val addr: Int) {
  val width: Int = addr.toBinaryString.length


}

/**
 * Class used to represent an IPv4 address (32 bits)
 */
class IPv4Addr(override val addr: Int) extends NetworkAddr(addr) {
  assert(this.width == 32)

  override def toString: String = {
    addr.toHexString
  }
}

object IPv4Addr {
  def apply(v4Addr: String): IPv4Addr = {
    val addrNum = v4Addr.replace(".", "").toInt
    assert(0x00000000 < addrNum && addrNum <= 0xffffffff)
    new IPv4Addr(v4Addr.replace(".", "").toInt)
  }
}

/**
 * Class used to represent an IPv6 address (128 bits)
 */
class IPv6Addr(override val addr: Int) extends NetworkAddr(addr) {
  assert(this.width == 128)
}

object IPv6Addr {
  def apply(v6Addr: String): IPv6Addr = {
    new IPv6Addr(v6Addr.replace(":", "").toInt)
  }
}


