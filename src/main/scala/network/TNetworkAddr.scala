package network

/**
 * Trait for NetworkAddr companion objects
 */
trait TNetworkAddr[T, U <: NetworkAddr[T]] {
  def MAX_NUM: Any
  def MIN_NUM: Any

  def apply(addr: String): U
}
