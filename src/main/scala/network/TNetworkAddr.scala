package network

/**
 * Trait for NetworkAddr companion objects
 */
trait TNetworkAddr[T <: NetworkAddr] {
  def MAX_NUM: Any

  def MIN_NUM: Any

  def apply(addr: String): T

  def apply(number: BigInt): T
}
