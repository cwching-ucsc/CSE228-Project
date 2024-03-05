package chiselRouter

/**
 * Base class used to represent a network address
 *
 * @param addr Address
 */
abstract class NetworkAddr(val addr: Long, val width: Int) {
  val binaryAddr: Vector[Int]

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
  assert(this.width == 32)

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
  def apply(v4Addr: String): IPv4Addr = {
    val addrNum = v4Addr
      .split('.')
      .reverse
      .zipWithIndex
      .map { case (n, i) => math.pow(256, i).toLong * n.toLong }
      .sum
    assert(0L < addrNum && addrNum <= 4294967295L)
    new IPv4Addr(addrNum)
  }
}

/**
 * Class used to represent an IPv6 address (128 bits)
 */
class IPv6Addr(override val addr: Long) extends NetworkAddr(addr, 128) {
  override val binaryAddr: Vector[Int] = Vector.empty
}

object IPv6Addr {
  def apply(v6Addr: String): IPv6Addr = {
    new IPv6Addr(v6Addr.replace(":", "").toInt)
  }
}


