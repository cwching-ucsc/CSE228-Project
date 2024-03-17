package network

/**
 * Base class used to represent a network address
 *
 * @param addr      Address parts in `Seq[Short]`
 * @param width     Width of whole address
 * @param separator Separator between address parts
 */
abstract class NetworkAddr(private val addr: Seq[Short], val width: Int, val separator: String) {
  /**
   * Compare two network addresses and make sure their width and address are equal.
   *
   * @param obj Other `NetworkAddr`
   */
  override def equals(obj: Any): Boolean = {
    obj match {
      case o: NetworkAddr => this.width == o.width && this.addr == o.addr
      case _ => false
    }
  }

  override def toString: String = {
    addr
      .map { i =>
        var max = 0
        val t = i.toString.toInt
        width match {
          case 32 => max = Byte.MaxValue.toInt
          case 128 => max = Short.MaxValue.toInt
        }
        if (t < 0) {
          (-t + max).toString
        } else {
          t.toString
        }
      }
      .mkString(separator)
  }
}
