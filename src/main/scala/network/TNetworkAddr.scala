package network

/**
 * Trait for NetworkAddr companion objects
 */
trait TNetworkAddr[T <: NetworkAddr] {
  /**
   * MAX value for each address part
   */
  def MAX_NUM: Any

  /**
   * MIN value for each address part
   */
  def MIN_NUM: Any

  /**
   * Create NetworkAddr
   * @param addr address in String
   */
  def apply(addr: String): T

  /**
   * Create NetworkAddr
   * @param number address in BigInt
   */
  def apply(number: BigInt): T
}
